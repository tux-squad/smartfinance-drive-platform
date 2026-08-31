-- Alinea la tabla simulations con los campos que usa la app.
alter table public.simulations
  add column if not exists down_payment numeric(12, 2) not null default 0 check (down_payment >= 0),
  add column if not exists grace_period_months integer not null default 0 check (grace_period_months >= 0),
  add column if not exists term_months integer not null default 1 check (term_months > 0),
  add column if not exists is_capitalized boolean not null default false;

do $$
begin
  if exists (
    select 1
    from information_schema.columns
    where table_schema = 'public'
      and table_name = 'simulations'
      and column_name = 'balloon_fee'
      and data_type = 'text'
  ) then
    alter table public.simulations
      alter column balloon_fee drop default,
      alter column balloon_fee type numeric(12, 2) using coalesce(nullif(balloon_fee, ''), '0')::numeric,
      alter column balloon_fee set default 0,
      alter column balloon_fee set not null;
  elsif not exists (
    select 1
    from information_schema.columns
    where table_schema = 'public'
      and table_name = 'simulations'
      and column_name = 'balloon_fee'
  ) then
    alter table public.simulations
      add column balloon_fee numeric(12, 2) not null default 0 check (balloon_fee >= 0);
  end if;

  if exists (
    select 1
    from information_schema.columns
    where table_schema = 'public'
      and table_name = 'simulations'
      and column_name = 'annual_effective_rate'
  ) then
    alter table public.simulations
      drop column annual_effective_rate;
  end if;

  alter table public.simulations
    alter column client_name set default 'Client',
    alter column vehicle_label set default 'Unknown Vehicle',
    alter column currency set default 'PEN',
    alter column amount_financed set default 0,
    alter column monthly_payment set default 0,
    alter column tcea set default 0,
    alter column tir set default 0,
    alter column van set default 0,
    alter column status set default 'pending';
end $$;

notify pgrst, 'reload schema';
