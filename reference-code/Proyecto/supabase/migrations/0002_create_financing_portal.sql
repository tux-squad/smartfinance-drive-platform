-- Migration: create_financing_portal
-- Catalogs and user-owned records for Autify vehicle financing simulations.

create extension if not exists pgcrypto;

create table public.financial_entities (
  id uuid primary key default gen_random_uuid(),
  name text not null unique,
  created_at timestamptz not null default now()
);

comment on table public.financial_entities is 'Financial institutions available for vehicle financing simulations.';

insert into public.financial_entities (name)
values
  ('Banco de Crédito del Perú (BCP)'),
  ('BBVA Perú'),
  ('Interbank'),
  ('Scotiabank Perú')
on conflict (name) do nothing;

create table public.vehicles (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references auth.users (id) on delete cascade,
  financial_entity_id uuid not null references public.financial_entities (id),
  brand text not null,
  model text not null,
  manufacture_year integer not null check (manufacture_year between 1900 and 2100),
  condition text not null check (condition in ('new', 'used')),
  currency text not null check (currency in ('PEN', 'USD')),
  price numeric(12, 2) not null check (price > 0),
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

comment on table public.vehicles is 'Vehicles registered by authenticated users for financing workflows.';

create trigger vehicles_set_updated_at
  before update on public.vehicles
  for each row execute function public.set_updated_at();

create table public.simulations (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references auth.users (id) on delete cascade,
  vehicle_id uuid references public.vehicles (id) on delete set null,
  client_name text not null,
  vehicle_label text not null,
  currency text not null check (currency in ('PEN', 'USD')),
  amount_financed numeric(12, 2) not null check (amount_financed >= 0),
  monthly_payment numeric(12, 2) not null check (monthly_payment >= 0),
  tcea numeric(9, 6) not null check (tcea >= 0),
  tir numeric(9, 6) not null check (tir >= 0),
  van numeric(12, 2) not null,
  status text not null check (status in ('approved', 'pending', 'rejected')),
  simulated_at timestamptz not null default now(),
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

comment on table public.simulations is 'Summarized financing simulation results used by the dashboard overview.';

create trigger simulations_set_updated_at
  before update on public.simulations
  for each row execute function public.set_updated_at();

alter table public.financial_entities enable row level security;
alter table public.vehicles enable row level security;
alter table public.simulations enable row level security;

create policy "Financial entities are viewable by authenticated users"
  on public.financial_entities for select
  to authenticated
  using (true);

create policy "Vehicles are viewable by owner"
  on public.vehicles for select
  to authenticated
  using ((select auth.uid()) = user_id);

create policy "Vehicles are insertable by owner"
  on public.vehicles for insert
  to authenticated
  with check ((select auth.uid()) = user_id);

create policy "Vehicles are updatable by owner"
  on public.vehicles for update
  to authenticated
  using ((select auth.uid()) = user_id)
  with check ((select auth.uid()) = user_id);

create policy "Vehicles are deletable by owner"
  on public.vehicles for delete
  to authenticated
  using ((select auth.uid()) = user_id);

create policy "Simulations are viewable by owner"
  on public.simulations for select
  to authenticated
  using ((select auth.uid()) = user_id);

create policy "Simulations are insertable by owner"
  on public.simulations for insert
  to authenticated
  with check ((select auth.uid()) = user_id);

create policy "Simulations are updatable by owner"
  on public.simulations for update
  to authenticated
  using ((select auth.uid()) = user_id)
  with check ((select auth.uid()) = user_id);

create policy "Simulations are deletable by owner"
  on public.simulations for delete
  to authenticated
  using ((select auth.uid()) = user_id);

grant select on public.financial_entities to authenticated;
grant select, insert, update, delete on public.vehicles to authenticated;
grant select, insert, update, delete on public.simulations to authenticated;
