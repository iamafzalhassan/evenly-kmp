create schema if not exists private;

revoke all on schema private from public, anon;
grant usage on schema private to authenticated;

alter function public.is_group_participant(uuid) set schema private;
