create or replace function private.max_modified_at_ms()
returns bigint
language sql
stable
set search_path = ''
as $$
    select (extract(epoch from now()) * 1000)::bigint + 300000;
$$;

revoke execute on function private.max_modified_at_ms() from public, anon;
grant execute on function private.max_modified_at_ms() to authenticated;

create or replace function public.stamp_group_write()
returns trigger
language plpgsql
set search_path = ''
as $$
declare
    latest_allowed bigint := private.max_modified_at_ms();
begin
    new.modified_at_ms := least(new.modified_at_ms, latest_allowed);
    if tg_op = 'INSERT' then
        new.created_by := auth.uid();
    else
        if new.modified_at_ms < least(old.modified_at_ms, latest_allowed) then
            new := old;
            new.sync_seq := nextval('public.sync_seq');
            return new;
        end if;
        new.created_by := old.created_by;
        if coalesce(current_setting('evenly.rotating_invite_code', true), '') <> 'on' then
            new.invite_code := old.invite_code;
        end if;
        new.created_at_ms := old.created_at_ms;
    end if;
    new.sync_seq := nextval('public.sync_seq');
    return new;
end;
$$;

create or replace function public.stamp_child_write()
returns trigger
language plpgsql
set search_path = ''
as $$
declare
    latest_allowed bigint := private.max_modified_at_ms();
begin
    new.modified_at_ms := least(new.modified_at_ms, latest_allowed);
    if tg_op = 'UPDATE' then
        if new.modified_at_ms < least(old.modified_at_ms, latest_allowed) then
            new := old;
            new.sync_seq := nextval('public.sync_seq');
            return new;
        end if;
        new.group_id := old.group_id;
    end if;
    new.sync_seq := nextval('public.sync_seq');
    return new;
end;
$$;

update public.groups set modified_at_ms = private.max_modified_at_ms() where modified_at_ms > private.max_modified_at_ms();
update public.members set modified_at_ms = private.max_modified_at_ms() where modified_at_ms > private.max_modified_at_ms();
update public.expenses set modified_at_ms = private.max_modified_at_ms() where modified_at_ms > private.max_modified_at_ms();
update public.settlements set modified_at_ms = private.max_modified_at_ms() where modified_at_ms > private.max_modified_at_ms();

create or replace function private.shares_are_valid(shares jsonb)
returns boolean
language plpgsql
immutable
set search_path = ''
as $$
begin
    if jsonb_typeof(shares) is distinct from 'array' or jsonb_array_length(shares) not between 1 and 500 then
        return false;
    end if;
    return not exists (
        select 1
        from jsonb_array_elements(shares) as share
        where jsonb_typeof(share) <> 'object'
            or jsonb_typeof(share -> 'position') is distinct from 'number'
            or jsonb_typeof(share -> 'value') is distinct from 'number'
            or jsonb_typeof(share -> 'member_id') is distinct from 'string'
            or (share ->> 'position') !~ '^[0-9]{1,9}$'
            or (share ->> 'value') !~ '^[0-9]{1,18}$'
            or (share ->> 'member_id') !~ '^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$'
    );
end;
$$;

revoke execute on function private.shares_are_valid(jsonb) from public, anon;
grant execute on function private.shares_are_valid(jsonb) to authenticated;

alter table public.expenses drop constraint expenses_shares_present;

alter table public.groups
    add constraint groups_name_length check (char_length(name) <= 100) not valid;

alter table public.members
    add constraint members_name_length check (char_length(name) <= 100) not valid;

alter table public.expenses
    add constraint expenses_shares_valid check (private.shares_are_valid(shares)) not valid,
    add constraint expenses_title_length check (char_length(title) <= 200) not valid,
    add constraint expenses_amount_bounded check (amount_minor < 10000000000000) not valid,
    add constraint expenses_spent_at_bounded check (spent_at_ms between -5364662400000 and 4102444799999) not valid;

alter table public.settlements
    add constraint settlements_amount_bounded check (amount_minor < 10000000000000) not valid,
    add constraint settlements_settled_at_bounded check (settled_at_ms between -5364662400000 and 4102444799999) not valid;

create or replace function private.new_invite_code()
returns text
language plpgsql
volatile
set search_path = ''
as $$
declare
    alphabet constant text := '0123456789ABCDEFGHJKMNPQRSTVWXYZ';
    byte_indexes constant int[] := array[0, 1, 2, 3, 4, 5, 7, 9, 10, 11, 12, 13];
    random_bytes bytea := uuid_send(gen_random_uuid());
    code text := '';
    byte_index int;
begin
    foreach byte_index in array byte_indexes loop
        code := code || substr(alphabet, (get_byte(random_bytes, byte_index) % 32) + 1, 1);
    end loop;
    return code;
end;
$$;

revoke execute on function private.new_invite_code() from public, anon;
grant execute on function private.new_invite_code() to authenticated;

alter table public.groups alter column invite_code set default private.new_invite_code();

alter table private.join_attempts add column ip text;

create index join_attempts_ip_time_idx on private.join_attempts (ip, attempted_at);

create or replace function public.join_group(code text)
returns uuid
language plpgsql
security definer
set search_path = ''
as $$
declare
    headers json := nullif(current_setting('request.headers', true), '')::json;
    caller_ip text := nullif(coalesce(headers ->> 'cf-connecting-ip', btrim(split_part(headers ->> 'x-forwarded-for', ',', 1))), '');
    normalized_code text := translate(upper(code), 'OIL -', '011');
    target uuid;
begin
    if auth.uid() is null then
        raise exception 'Not authenticated' using errcode = '28000';
    end if;
    perform pg_advisory_xact_lock(hashtext('evenly.join_group'));
    if (select count(*) from private.join_attempts a where a.user_id = auth.uid() and a.attempted_at > now() - interval '10 minutes') >= 20
        or (caller_ip is not null and (select count(*) from private.join_attempts a where a.ip = caller_ip and a.attempted_at > now() - interval '10 minutes') >= 60) then
        raise exception 'Too many invite code attempts' using errcode = 'EV429';
    end if;
    select g.id into target
    from public.groups g
    where g.invite_code = normalized_code and g.deleted = false;
    if target is null then
        insert into private.join_attempts (user_id, ip) values (auth.uid(), caller_ip);
        delete from private.join_attempts a where a.attempted_at < now() - interval '1 day';
        return null;
    end if;
    insert into public.group_participants (group_id, user_id)
    values (target, auth.uid())
    on conflict do nothing;
    return target;
end;
$$;

revoke execute on function public.join_group(text) from public, anon;
grant execute on function public.join_group(text) to authenticated;

create or replace function public.rotate_invite_code(target_group_id uuid)
returns text
language plpgsql
security definer
set search_path = ''
as $$
declare
    new_code text;
begin
    if auth.uid() is null then
        raise exception 'Not authenticated' using errcode = '28000';
    end if;
    if not private.is_group_participant(target_group_id) then
        raise exception 'Not a participant of this group' using errcode = '42501';
    end if;
    perform set_config('evenly.rotating_invite_code', 'on', true);
    update public.groups g
    set invite_code = private.new_invite_code()
    where g.id = target_group_id and g.deleted = false
    returning g.invite_code into new_code;
    perform set_config('evenly.rotating_invite_code', 'off', true);
    if new_code is null then
        raise exception 'Group not found' using errcode = 'P0002';
    end if;
    return new_code;
end;
$$;

revoke execute on function public.rotate_invite_code(uuid) from public, anon;
grant execute on function public.rotate_invite_code(uuid) to authenticated;
