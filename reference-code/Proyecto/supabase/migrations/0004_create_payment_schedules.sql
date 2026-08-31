-- Migration: create_payment_schedules
-- Detailed amortization schedule rows generated for each financing simulation.

create table public.payment_schedules (
  id uuid primary key default gen_random_uuid(),
  simulation_id uuid not null references public.simulations (id) on delete cascade,
  period_number integer not null check (period_number > 0),
  due_date date not null,
  opening_balance numeric(12, 2) not null check (opening_balance >= 0),
  interest numeric(12, 2) not null check (interest >= 0),
  principal numeric(12, 2) not null check (principal >= 0),
  insurance numeric(12, 2) not null default 0 check (insurance >= 0),
  commission numeric(12, 2) not null default 0 check (commission >= 0),
  total_payment numeric(12, 2) not null check (total_payment >= 0),
  cash_flow numeric(12, 2) not null,
  closing_balance numeric(12, 2) not null check (closing_balance >= 0),
  created_at timestamptz not null default now(),

  constraint payment_schedules_simulation_period_unique unique (
    simulation_id,
    period_number
  )
);

comment on table public.payment_schedules is 'Detailed amortization schedule rows generated for each financing simulation.';

create index payment_schedules_simulation_id_idx
  on public.payment_schedules (simulation_id);

create index payment_schedules_due_date_idx
  on public.payment_schedules (due_date);

alter table public.payment_schedules enable row level security;

create policy "Payment schedules are viewable by simulation owner"
  on public.payment_schedules for select
  to authenticated
  using (
    exists (
      select 1
      from public.simulations
      where simulations.id = payment_schedules.simulation_id
        and simulations.user_id = (select auth.uid())
    )
  );

create policy "Payment schedules are insertable by simulation owner"
  on public.payment_schedules for insert
  to authenticated
  with check (
    exists (
      select 1
      from public.simulations
      where simulations.id = payment_schedules.simulation_id
        and simulations.user_id = (select auth.uid())
    )
  );

create policy "Payment schedules are updatable by simulation owner"
  on public.payment_schedules for update
  to authenticated
  using (
    exists (
      select 1
      from public.simulations
      where simulations.id = payment_schedules.simulation_id
        and simulations.user_id = (select auth.uid())
    )
  )
  with check (
    exists (
      select 1
      from public.simulations
      where simulations.id = payment_schedules.simulation_id
        and simulations.user_id = (select auth.uid())
    )
  );

create policy "Payment schedules are deletable by simulation owner"
  on public.payment_schedules for delete
  to authenticated
  using (
    exists (
      select 1
      from public.simulations
      where simulations.id = payment_schedules.simulation_id
        and simulations.user_id = (select auth.uid())
    )
  );

grant select, insert, update, delete on public.payment_schedules to authenticated;