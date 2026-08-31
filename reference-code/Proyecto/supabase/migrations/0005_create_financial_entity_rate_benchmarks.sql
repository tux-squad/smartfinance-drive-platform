-- Migration: create_financial_entity_rate_benchmarks
-- Reference annual rates by financial institution for comparison in financial metrics.

create table public.financial_entity_rate_benchmarks (
  id uuid primary key default gen_random_uuid(),
  financial_entity_id uuid not null references public.financial_entities (id) on delete cascade,
  rate_type text not null default 'TCEA' check (rate_type in ('TCEA', 'TEA')),
  annual_rate numeric(9, 6) not null check (annual_rate >= 0),
  currency text not null check (currency in ('PEN', 'USD')),
  source_label text not null,
  source_url text,
  effective_from date not null,
  created_at timestamptz not null default now(),

  constraint financial_entity_rate_benchmarks_unique unique (
    financial_entity_id,
    rate_type,
    currency,
    effective_from
  )
);

comment on table public.financial_entity_rate_benchmarks is 'Reference annual rates by financial institution used for comparison in financial metrics.';

create index financial_entity_rate_benchmarks_entity_id_idx
  on public.financial_entity_rate_benchmarks (financial_entity_id);

alter table public.financial_entity_rate_benchmarks enable row level security;

create policy "Financial entity rate benchmarks are viewable by authenticated users"
  on public.financial_entity_rate_benchmarks for select
  to authenticated
  using (true);

grant select on public.financial_entity_rate_benchmarks to authenticated;

insert into public.financial_entity_rate_benchmarks (
  financial_entity_id,
  rate_type,
  annual_rate,
  currency,
  source_label,
  effective_from
)
select id, 'TCEA', 0.225000, 'PEN', 'Reference benchmark', date '2026-06-01'
from public.financial_entities
where name = 'Banco de Crédito del Perú (BCP)';

insert into public.financial_entity_rate_benchmarks (
  financial_entity_id,
  rate_type,
  annual_rate,
  currency,
  source_label,
  effective_from
)
select id, 'TCEA', 0.182000, 'PEN', 'Reference benchmark', date '2026-06-01'
from public.financial_entities
where name = 'BBVA Perú';

insert into public.financial_entity_rate_benchmarks (
  financial_entity_id,
  rate_type,
  annual_rate,
  currency,
  source_label,
  effective_from
)
select id, 'TCEA', 0.201000, 'PEN', 'Reference benchmark', date '2026-06-01'
from public.financial_entities
where name = 'Interbank';

insert into public.financial_entity_rate_benchmarks (
  financial_entity_id,
  rate_type,
  annual_rate,
  currency,
  source_label,
  effective_from
)
select id, 'TCEA', 0.194000, 'PEN', 'Reference benchmark', date '2026-06-01'
from public.financial_entities
where name = 'Scotiabank Perú';
