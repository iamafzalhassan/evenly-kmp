alter table public.groups
    add constraint groups_name_present check (btrim(name) <> ''),
    add constraint groups_currency_code_format check (currency_code ~ '^[A-Z]{3}$');

alter table public.members
    add constraint members_name_present check (btrim(name) <> ''),
    add constraint members_position_non_negative check (position >= 0);

alter table public.expenses
    add constraint expenses_title_present check (btrim(title) <> ''),
    add constraint expenses_amount_positive check (amount_minor > 0),
    add constraint expenses_currency_code_format check (currency_code ~ '^[A-Z]{3}$'),
    add constraint expenses_split_kind_known check (split_kind in ('EQUAL', 'EXACT', 'PERCENTAGE')),
    add constraint expenses_shares_present check (case when jsonb_typeof(shares) = 'array' then jsonb_array_length(shares) > 0 else false end),
    add constraint expenses_exchange_rate_complete check ((exchange_rate_micros is null) = (exchange_rate_currency_code is null)),
    add constraint expenses_exchange_rate_positive check (exchange_rate_micros is null or exchange_rate_micros > 0),
    add constraint expenses_exchange_rate_currency_format check (exchange_rate_currency_code is null or exchange_rate_currency_code ~ '^[A-Z]{3}$');

alter table public.settlements
    add constraint settlements_amount_positive check (amount_minor > 0),
    add constraint settlements_members_differ check (from_member_id <> to_member_id),
    add constraint settlements_currency_code_format check (currency_code ~ '^[A-Z]{3}$');

create or replace function public.serialize_sync_writes()
returns trigger
language plpgsql
set search_path = ''
as $$
begin
    perform pg_advisory_xact_lock(hashtext('evenly.sync_seq'));
    return null;
end;
$$;

revoke execute on function public.serialize_sync_writes() from public, anon, authenticated;

create trigger groups_serialize before insert or update on public.groups
for each statement execute function public.serialize_sync_writes();

create trigger members_serialize before insert or update on public.members
for each statement execute function public.serialize_sync_writes();

create trigger expenses_serialize before insert or update on public.expenses
for each statement execute function public.serialize_sync_writes();

create trigger settlements_serialize before insert or update on public.settlements
for each statement execute function public.serialize_sync_writes();

create table private.join_attempts (
    user_id uuid not null,
    attempted_at timestamptz not null default now()
);

create index join_attempts_user_time_idx on private.join_attempts (user_id, attempted_at);

alter table private.join_attempts enable row level security;

revoke all on table private.join_attempts from public, anon, authenticated;

create or replace function public.join_group(code text)
returns uuid
language plpgsql
security definer
set search_path = ''
as $$
declare
    target uuid;
begin
    if auth.uid() is null then
        raise exception 'Not authenticated' using errcode = '28000';
    end if;
    if (select count(*) from private.join_attempts a where a.user_id = auth.uid() and a.attempted_at > now() - interval '10 minutes') >= 20 then
        raise exception 'Too many invite code attempts' using errcode = 'EV429';
    end if;
    select g.id into target
    from public.groups g
    where g.invite_code = upper(trim(code)) and g.deleted = false;
    if target is null then
        insert into private.join_attempts (user_id) values (auth.uid());
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
