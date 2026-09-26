create sequence public.sync_seq;

grant usage on sequence public.sync_seq to authenticated;

create table public.groups (
    id uuid primary key,
    name text not null,
    currency_code text not null,
    created_at_ms bigint not null,
    created_by uuid not null default auth.uid(),
    invite_code text not null unique default upper(substr(replace(gen_random_uuid()::text, '-', ''), 1, 8)),
    modified_at_ms bigint not null,
    deleted boolean not null default false,
    sync_seq bigint not null default nextval('public.sync_seq')
);

create table public.group_participants (
    group_id uuid not null references public.groups (id) on delete cascade,
    user_id uuid not null,
    joined_at timestamptz not null default now(),
    primary key (group_id, user_id)
);

create table public.members (
    id uuid primary key,
    group_id uuid not null references public.groups (id) on delete cascade,
    name text not null,
    position integer not null,
    modified_at_ms bigint not null,
    deleted boolean not null default false,
    sync_seq bigint not null default nextval('public.sync_seq')
);

create table public.expenses (
    id uuid primary key,
    group_id uuid not null references public.groups (id) on delete cascade,
    title text not null,
    amount_minor bigint not null,
    currency_code text not null,
    exchange_rate_micros bigint,
    exchange_rate_currency_code text,
    paid_by_member_id uuid not null,
    split_kind text not null,
    shares jsonb not null,
    spent_at_ms bigint not null,
    modified_at_ms bigint not null,
    deleted boolean not null default false,
    sync_seq bigint not null default nextval('public.sync_seq')
);

create table public.settlements (
    id uuid primary key,
    group_id uuid not null references public.groups (id) on delete cascade,
    from_member_id uuid not null,
    to_member_id uuid not null,
    amount_minor bigint not null,
    currency_code text not null,
    settled_at_ms bigint not null,
    modified_at_ms bigint not null,
    deleted boolean not null default false,
    sync_seq bigint not null default nextval('public.sync_seq')
);

create index groups_sync_seq_idx on public.groups (sync_seq);
create index group_participants_user_idx on public.group_participants (user_id);
create index members_group_idx on public.members (group_id);
create index members_sync_seq_idx on public.members (sync_seq);
create index expenses_group_idx on public.expenses (group_id);
create index expenses_sync_seq_idx on public.expenses (sync_seq);
create index settlements_group_idx on public.settlements (group_id);
create index settlements_sync_seq_idx on public.settlements (sync_seq);

create or replace function public.is_group_participant(target_group_id uuid)
returns boolean
language sql
stable
security definer
set search_path = ''
as $$
    select exists (
        select 1 from public.group_participants p
        where p.group_id = target_group_id and p.user_id = (select auth.uid())
    );
$$;

revoke execute on function public.is_group_participant(uuid) from public, anon;
grant execute on function public.is_group_participant(uuid) to authenticated;

create or replace function public.stamp_group_write()
returns trigger
language plpgsql
set search_path = ''
as $$
begin
    if tg_op = 'INSERT' then
        new.created_by := auth.uid();
    else
        if new.modified_at_ms < old.modified_at_ms then
            return old;
        end if;
        new.created_by := old.created_by;
        new.invite_code := old.invite_code;
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
begin
    if tg_op = 'UPDATE' then
        if new.modified_at_ms < old.modified_at_ms then
            return old;
        end if;
        new.group_id := old.group_id;
    end if;
    new.sync_seq := nextval('public.sync_seq');
    return new;
end;
$$;

create or replace function public.add_group_creator()
returns trigger
language plpgsql
security definer
set search_path = ''
as $$
begin
    insert into public.group_participants (group_id, user_id)
    values (new.id, new.created_by)
    on conflict do nothing;
    return new;
end;
$$;

revoke execute on function public.add_group_creator() from public, anon, authenticated;

create trigger groups_stamp before insert or update on public.groups
for each row execute function public.stamp_group_write();

create trigger groups_add_creator after insert on public.groups
for each row execute function public.add_group_creator();

create trigger members_stamp before insert or update on public.members
for each row execute function public.stamp_child_write();

create trigger expenses_stamp before insert or update on public.expenses
for each row execute function public.stamp_child_write();

create trigger settlements_stamp before insert or update on public.settlements
for each row execute function public.stamp_child_write();

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
    select g.id into target
    from public.groups g
    where g.invite_code = upper(trim(code)) and g.deleted = false;
    if target is null then
        raise exception 'Invite code not found' using errcode = 'P0002';
    end if;
    insert into public.group_participants (group_id, user_id)
    values (target, auth.uid())
    on conflict do nothing;
    return target;
end;
$$;

revoke execute on function public.join_group(text) from public, anon;
grant execute on function public.join_group(text) to authenticated;

alter table public.groups enable row level security;
alter table public.group_participants enable row level security;
alter table public.members enable row level security;
alter table public.expenses enable row level security;
alter table public.settlements enable row level security;

create policy "Participants and creators read groups" on public.groups
for select to authenticated
using (created_by = (select auth.uid()) or public.is_group_participant(id));

create policy "Users create their own groups" on public.groups
for insert to authenticated
with check (created_by = (select auth.uid()));

create policy "Participants update groups" on public.groups
for update to authenticated
using (public.is_group_participant(id))
with check (public.is_group_participant(id));

create policy "Participants read participants" on public.group_participants
for select to authenticated
using (user_id = (select auth.uid()) or public.is_group_participant(group_id));

create policy "Participants read members" on public.members
for select to authenticated using (public.is_group_participant(group_id));

create policy "Participants add members" on public.members
for insert to authenticated with check (public.is_group_participant(group_id));

create policy "Participants update members" on public.members
for update to authenticated using (public.is_group_participant(group_id)) with check (public.is_group_participant(group_id));

create policy "Participants read expenses" on public.expenses
for select to authenticated using (public.is_group_participant(group_id));

create policy "Participants add expenses" on public.expenses
for insert to authenticated with check (public.is_group_participant(group_id));

create policy "Participants update expenses" on public.expenses
for update to authenticated using (public.is_group_participant(group_id)) with check (public.is_group_participant(group_id));

create policy "Participants read settlements" on public.settlements
for select to authenticated using (public.is_group_participant(group_id));

create policy "Participants add settlements" on public.settlements
for insert to authenticated with check (public.is_group_participant(group_id));

create policy "Participants update settlements" on public.settlements
for update to authenticated using (public.is_group_participant(group_id)) with check (public.is_group_participant(group_id));
