"use client";

import { Car, FileText, Plus } from "lucide-react";
import Link from "next/link";

import type { SimulationSummary } from "@/domain/entities/simulation";
import { toEffectiveAnnualRate } from "@/domain/services/credit-calculator";
import type { DeleteSimulationState } from "@/app/(portal)/simulations/actions";
import { DeleteSimulationButton } from "@/presentation/components/portal/delete-simulation-button";
import {
  Accordion,
  AccordionContent,
  AccordionItem,
  AccordionTrigger,
} from "@/presentation/components/ui/accordion";
import { Button } from "@/presentation/components/ui/button";
import { Separator } from "@/presentation/components/ui/separator";
import { useLanguage } from "@/presentation/i18n/language-context";
import { portalDictionary } from "@/presentation/i18n/portal-dictionary";
import {
  VEHICLE_BRAND_IMAGE,
  VehicleBrandOption,
} from "@/shared/constants/vehicle";

interface SimulationRepositoryViewProps {
  simulations: SimulationSummary[];
  deleteAction: (
    prevState: DeleteSimulationState,
    formData: FormData,
  ) => Promise<DeleteSimulationState>;
}

function money(value: number, currency: "PEN" | "USD"): string {
  return new Intl.NumberFormat("en-US", {
    style: "currency",
    currency,
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  }).format(value);
}

function percent(value: number): string {
  return `${(value * 100).toFixed(2)}%`;
}

const KNOWN_BRANDS = Object.values(VehicleBrandOption);

function resolveVehicleImageUrl(
  vehicleLabel: string,
  vehicleImageUrl: string | null,
): string | null {
  if (vehicleImageUrl) return vehicleImageUrl;

  const normalizedLabel = vehicleLabel.trim().toLowerCase();
  const brand = KNOWN_BRANDS.find((knownBrand) =>
    normalizedLabel.startsWith(knownBrand.toLowerCase()),
  );

  return brand ? VEHICLE_BRAND_IMAGE[brand] : null;
}

type MetricHelpCopy = {
  financedAmount: string;
  monthlyPayment: string;
  van: string;
  tir: string;
  tea: string;
  tcea: string;
};

const metricHelp: Record<"es" | "en", MetricHelpCopy> = {
  es: {
    financedAmount: "Capital que realmente se financia después de la inicial.",
    monthlyPayment: "Pago periódico estimado del crédito según el escenario.",
    van: "Valor actual neto de los flujos del crédito descontados hoy.",
    tir: "Rentabilidad mensual implícita del flujo de pagos del préstamo.",
    tea: "Tasa anual equivalente sin incluir el seguro mensual.",
    tcea: "Costo efectivo anual total considerando cargos del crédito.",
  },
  en: {
    financedAmount: "Net principal financed after the down payment.",
    monthlyPayment: "Estimated periodic payment for this financing scenario.",
    van: "Net present value of the loan cash flows discounted to today.",
    tir: "Implied monthly return rate from the payment cash flow.",
    tea: "Equivalent annual rate excluding monthly insurance charges.",
    tcea: "Total effective annual cost including financing charges.",
  },
};

/** Fila etiqueta/valor reutilizable para los paneles de detalle. */
function Row({
  label,
  value,
  description,
}: {
  label: string;
  value: string;
  description?: string;
}) {
  return (
    <div className="py-1.5 text-sm">
      <div className="flex items-center justify-between gap-4">
        <span className="text-muted-foreground">{label}</span>
        <span className="font-medium text-foreground">{value}</span>
      </div>
      {description ? (
        <p className="mt-0.5 pr-6 text-xs leading-5 text-muted-foreground/90">
          {description}
        </p>
      ) : null}
    </div>
  );
}

function MetricCard({
  label,
  value,
  description,
}: {
  label: string;
  value: string;
  description: string;
}) {
  return (
    <div className="rounded-lg border border-border bg-background/70 p-3">
      <div className="flex items-center justify-between gap-4">
        <span className="text-sm font-medium text-muted-foreground">{label}</span>
        <span className="text-base font-bold text-foreground">{value}</span>
      </div>
      <p className="mt-1 text-xs leading-5 text-muted-foreground/90">
        {description}
      </p>
    </div>
  );
}

export function SimulationRepositoryView({
  simulations,
  deleteAction,
}: SimulationRepositoryViewProps) {
  const { lang } = useLanguage();
  const s = portalDictionary[lang].simulations;
  const metricCopy = metricHelp[lang];

  return (
    <div className="mx-auto w-full max-w-5xl px-6 py-8">
      <header className="mb-6 flex flex-col justify-between gap-4 sm:flex-row sm:items-center">
        <div>
          <h1 className="text-3xl font-bold tracking-tight text-foreground">
            {s.title}
          </h1>
          <p className="mt-1 text-sm text-muted-foreground">{s.subtitle}</p>
        </div>
        <div className="flex items-center gap-3">
          <span className="text-sm text-muted-foreground">
            {simulations.length} {s.total}
          </span>
          <Button asChild>
            <Link href="/credit">
              <Plus className="size-4" />
              {s.newSimulation}
            </Link>
          </Button>
        </div>
      </header>

      {simulations.length === 0 ? (
        <div className="rounded-xl border border-dashed border-border bg-card p-12 text-center">
          <FileText className="mx-auto mb-3 size-10 text-muted-foreground" />
          <p className="text-sm text-muted-foreground">{s.empty}</p>
        </div>
      ) : (
        <Accordion
          type="single"
          collapsible
          className="overflow-hidden rounded-xl border border-border bg-card"
        >
          {simulations.map((sim) => {
            const tea =
              sim.rateType === "effective"
                ? sim.annualRate / 100
                : toEffectiveAnnualRate({
                    type: "nominal",
                    annualRate: sim.annualRate / 100,
                    compoundingPeriodsPerYear: sim.compounding,
                  });
            const graceLabel =
              sim.gracePeriod <= 0
                ? s.graceNone
                : sim.isCapital
                  ? s.graceTotal
                  : s.gracePartial;
            const vehicleImageUrl = resolveVehicleImageUrl(
              sim.vehicleLabel,
              sim.vehicleImageUrl,
            );

            return (
              <AccordionItem
                key={sim.id}
                value={sim.id}
                className="px-4 data-[state=open]:bg-muted/30"
              >
                <AccordionTrigger className="hover:no-underline">
                  <div className="flex flex-1 items-center gap-4 pr-2">
                    <div className="hidden h-12 w-16 shrink-0 items-center justify-center overflow-hidden rounded-md bg-muted sm:flex">
                      {vehicleImageUrl ? (
                        // eslint-disable-next-line @next/next/no-img-element
                        <img
                          src={vehicleImageUrl}
                          alt={sim.vehicleLabel}
                          className="h-full w-full object-contain p-1"
                        />
                      ) : (
                        <Car className="size-6 text-muted-foreground" />
                      )}
                    </div>
                    <div className="min-w-0 flex-1">
                      <p className="truncate font-semibold text-foreground">
                        {sim.vehicleLabel}
                      </p>
                      <p className="text-xs text-muted-foreground">
                        {new Date(sim.simulatedAt).toLocaleDateString(lang, {
                          year: "numeric",
                          month: "short",
                          day: "numeric",
                        })}
                      </p>
                    </div>
                    <div className="hidden text-right md:block">
                      <p className="text-xs text-muted-foreground">
                        {s.monthlyPayment}
                      </p>
                      <p className="font-semibold text-foreground">
                        {money(sim.monthlyPayment, sim.currency)}
                      </p>
                    </div>
                    <div className="hidden text-right sm:block">
                      <p className="text-xs text-muted-foreground">{s.tcea}</p>
                      <p className="font-semibold text-emerald-600">
                        {percent(sim.tcea)}
                      </p>
                    </div>
                  </div>
                </AccordionTrigger>

                <AccordionContent>
                  <div className="grid gap-6 pb-4 lg:grid-cols-2">
                    {/* Vehículo */}
                    <div>
                      <h3 className="mb-2 text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                        {s.vehicleTitle}
                      </h3>
                      <div className="mb-3 flex h-32 w-full items-center justify-center overflow-hidden rounded-lg bg-muted">
                        {vehicleImageUrl ? (
                          // eslint-disable-next-line @next/next/no-img-element
                          <img
                            src={vehicleImageUrl}
                            alt={sim.vehicleLabel}
                            className="h-full w-full object-contain p-2"
                          />
                        ) : (
                          <Car className="size-10 text-muted-foreground" />
                        )}
                      </div>
                      <p className="font-semibold text-foreground">
                        {sim.vehicleLabel}
                      </p>
                      <Row
                        label={s.price}
                        value={money(sim.vehiclePrice, sim.currency)}
                      />
                    </div>

                    {/* Configuración */}
                    <div>
                      <h3 className="mb-2 text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                        {s.configTitle}
                      </h3>
                      <Row
                        label={s.downPayment}
                        value={money(sim.downPayment, sim.currency)}
                      />
                      <Row
                        label={s.balloon}
                        value={money(sim.balloonFee, sim.currency)}
                      />
                      <Row
                        label={s.term}
                        value={`${sim.term} ${s.months}`}
                      />
                      <Row label={s.grace} value={graceLabel} />
                      <Row
                        label={s.rate}
                        value={`${sim.annualRate.toFixed(2)}% ${
                          sim.rateType === "effective" ? s.effective : s.nominal
                        }`}
                      />
                      <Row
                        label={s.insurance}
                        value={percent(sim.insuranceRate)}
                      />
                    </div>
                  </div>

                  {/* Indicadores */}
                  <div className="pb-2">
                    <h3 className="mb-2 text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                      {s.metricsTitle}
                    </h3>
                    <div className="grid gap-2 md:grid-cols-2 xl:grid-cols-3">
                        <MetricCard
                          label={s.financedAmount}
                          value={money(sim.amountFinanced, sim.currency)}
                          description={metricCopy.financedAmount}
                        />
                        <MetricCard
                          label={s.monthlyPayment}
                          value={money(sim.monthlyPayment, sim.currency)}
                          description={metricCopy.monthlyPayment}
                        />
                        <MetricCard
                          label={s.van}
                          value={money(sim.van, sim.currency)}
                          description={metricCopy.van}
                        />
                        <MetricCard
                          label={s.tir}
                          value={percent(sim.tir)}
                          description={metricCopy.tir}
                        />
                        <MetricCard
                          label={s.tea}
                          value={percent(tea)}
                          description={metricCopy.tea}
                        />
                        <MetricCard
                          label={s.tcea}
                          value={percent(sim.tcea)}
                          description={metricCopy.tcea}
                        />
                    </div>
                  </div>

                  <Separator className="my-4" />

                  <div className="flex flex-wrap items-center justify-end gap-3">
                    <Button asChild variant="outline" size="sm">
                      <Link href={`/payment?sim=${sim.id}`}>
                        <FileText className="size-4" />
                        {s.viewSchedule}
                      </Link>
                    </Button>
                    <DeleteSimulationButton
                      simulationId={sim.id}
                      action={deleteAction}
                    />
                  </div>
                </AccordionContent>
              </AccordionItem>
            );
          })}
        </Accordion>
      )}
    </div>
  );
}
