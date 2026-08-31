-- Guarda los parámetros de ENTRADA originales de cada simulación, para poder
-- reconstruir/mostrar "cómo se creó" en la vista expandible de Simulaciones.
alter table public.simulations
  add column if not exists annual_rate numeric(9, 6) not null default 0 check (annual_rate >= 0),
  add column if not exists rate_type text not null default 'effective' check (rate_type in ('effective', 'nominal')),
  add column if not exists compounding integer not null default 12 check (compounding > 0),
  add column if not exists insurance_rate numeric(9, 6) not null default 0 check (insurance_rate >= 0);

comment on column public.simulations.annual_rate is 'Tasa anual ingresada por el usuario, en porcentaje (ej. 14.00).';
comment on column public.simulations.rate_type is 'Tipo de tasa ingresada: effective (TEA) o nominal (TNA).';
comment on column public.simulations.compounding is 'Capitalizaciones por año (solo aplica si rate_type = nominal).';
comment on column public.simulations.insurance_rate is 'Seguro de desgravamen mensual como fracción del saldo (decimal).';

notify pgrst, 'reload schema';
