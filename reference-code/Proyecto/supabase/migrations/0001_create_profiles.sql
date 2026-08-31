-- Migration: create_profiles
-- Tabla de perfiles (informacion de cuenta) ligada 1:1 a auth.users.
-- Aplicada al proyecto Supabase via MCP; esta copia es la fuente de verdad
-- versionada para el equipo.

create table public.profiles (
  id uuid primary key references auth.users (id) on delete cascade,
  email text not null,
  national_id text,
  full_legal_names text,
  date_of_birth date,
  phone_country_code text default '+51',
  mobile_phone text,
  monthly_income numeric(12, 2),
  employment_status text,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

comment on table public.profiles is 'Account/settings information for each authenticated user.';

-- Mantiene updated_at al dia
create function public.set_updated_at()
returns trigger
language plpgsql
set search_path = ''
as $$
begin
  new.updated_at = now();
  return new;
end;
$$;

create trigger profiles_set_updated_at
  before update on public.profiles
  for each row execute function public.set_updated_at();

-- Row Level Security: cada usuario solo ve y edita su propio perfil
alter table public.profiles enable row level security;

create policy "Profiles are viewable by owner"
  on public.profiles for select
  to authenticated
  using ((select auth.uid()) = id);

create policy "Profiles are insertable by owner"
  on public.profiles for insert
  to authenticated
  with check ((select auth.uid()) = id);

create policy "Profiles are updatable by owner"
  on public.profiles for update
  to authenticated
  using ((select auth.uid()) = id)
  with check ((select auth.uid()) = id);

-- Crea automaticamente el perfil cuando se registra un usuario nuevo
create function public.handle_new_user()
returns trigger
language plpgsql
security definer set search_path = ''
as $$
begin
  insert into public.profiles (id, email)
  values (new.id, new.email);
  return new;
end;
$$;

create trigger on_auth_user_created
  after insert on auth.users
  for each row execute function public.handle_new_user();

-- Las funciones de trigger no deben ser invocables como RPC por la API.
revoke execute on function public.handle_new_user() from anon, authenticated, public;
revoke execute on function public.set_updated_at() from anon, authenticated, public;

-- "Automatically expose new tables" esta OFF: concedemos acceso manualmente.
-- RLS sigue limitando cada rol a su propia fila.
grant select, insert, update on public.profiles to authenticated;
