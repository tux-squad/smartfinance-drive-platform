"use client";

import { Car, SlidersHorizontal } from "lucide-react";
import { useMemo, useState, useActionState } from "react";

import {
  buildFinancingPlan,
  resolveGraceType,
} from "@/domain/services/financing-plan-builder";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/presentation/components/ui/select";
import { useLanguage } from "@/presentation/i18n/language-context";
import { portalDictionary } from "@/presentation/i18n/portal-dictionary";
import {
  COMPOUNDING_PER_YEAR,
  DEFAULT_DISCOUNT_MONTHLY_RATE,
  DEFAULT_MONTHLY_COMMISSION,
} from "@/shared/config/financing";
import type { VehicleFormState } from "@/shared/types/vehicle-form-state";

interface VehicleOption {
  id: string;
  brand: string;
  model: string;
  price: number;
  currency: "PEN" | "USD";
  imageUrl: string | null;
}

interface CreditConfigurationViewProps {
  action: (
    prevState: VehicleFormState,
    formData: FormData,
  ) => Promise<VehicleFormState>;
  vehicles: VehicleOption[];
  initialVehicleId?: string;
}

const initialState: VehicleFormState = { error: null, success: false };

const cardClass =
  "rounded-xl border border-slate-200/80 bg-white p-5 shadow-sm";
const labelClass =
  "text-xs font-semibold uppercase tracking-wide text-slate-500";
const helpClass = "mt-1 text-xs leading-5 text-slate-400";
const fieldClass =
  "rounded-lg border border-slate-200 bg-slate-50 px-3 py-2.5 transition focus-within:border-slate-400 focus-within:bg-white focus-within:ring-2 focus-within:ring-slate-900/10";

function formatAmount(value: number): string {
  return value.toLocaleString("en-US", {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  });
}

export function CreditConfigurationView({
  action,
  vehicles = [],
  initialVehicleId,
}: CreditConfigurationViewProps) {
  const { lang } = useLanguage();
  const c = portalDictionary[lang].credit;

  const [selectedVehicleId, setSelectedVehicleId] = useState<string>(
    initialVehicleId || (vehicles.length > 0 ? vehicles[0].id : ""),
  );

  const activeVehicle = vehicles.find((v) => v.id === selectedVehicleId) || {
    id: "",
    brand: "Select",
    model: "Vehicle",
    price: 0,
    currency: "USD" as const,
    imageUrl: null,
  };

  const [downPaymentPercent, setDownPaymentPercent] = useState("20.00");
  const [balloonPercent, setBalloonPercent] = useState("40.00");
  const [selectedTerm, setSelectedTerm] = useState<number>(36);
  const [interestRate, setInterestRate] = useState("14.00");
  const [rateType, setRateType] = useState<"effective" | "nominal">("effective");
  const [compounding, setCompounding] = useState<number>(
    COMPOUNDING_PER_YEAR.monthly,
  );
  const [insuranceRate, setInsuranceRate] = useState("0.05");
  const [gracePeriod, setGracePeriod] = useState("0");
  const [isCapital, setIsCapital] = useState(false);

  const currencySymbol = activeVehicle.currency === "USD" ? "$" : "S/";
  const calculatedDownPaymentAmount =
    (activeVehicle.price * Number(downPaymentPercent)) / 100;
  const calculatedBalloonAmount =
    (activeVehicle.price * Number(balloonPercent)) / 100;
  const amountFinanced = activeVehicle.price - calculatedDownPaymentAmount;

  // VISTA PREVIA EN TIEMPO REAL: se calcula en el navegador con EXACTAMENTE el
  // mismo motor de dominio que usa el servidor al guardar. Cero valores "mock".
  const preview = useMemo(() => {
    try {
      if (!(activeVehicle.price > 0) || Number(interestRate) <= 0) {
        return null;
      }
      return buildFinancingPlan({
        vehiclePrice: activeVehicle.price,
        downPayment: calculatedDownPaymentAmount,
        balloonPayment: calculatedBalloonAmount,
        termMonths: selectedTerm,
        gracePeriodMonths: Number(gracePeriod) || 0,
        graceType: resolveGraceType(Number(gracePeriod) || 0, isCapital),
        annualRate: {
          type: rateType,
          annualRate: Number(interestRate) / 100,
          compoundingPeriodsPerYear: compounding,
        },
        monthlyInsuranceRate: (Number(insuranceRate) || 0) / 100,
        monthlyCommission: DEFAULT_MONTHLY_COMMISSION,
        discountMonthlyRate: DEFAULT_DISCOUNT_MONTHLY_RATE,
      });
    } catch {
      return null;
    }
  }, [
    activeVehicle.price,
    calculatedDownPaymentAmount,
    calculatedBalloonAmount,
    selectedTerm,
    gracePeriod,
    isCapital,
    rateType,
    interestRate,
    compounding,
    insuranceRate,
  ]);

  const monthlyPaymentLabel = preview
    ? `${currencySymbol}${formatAmount(preview.monthlyPayment)}`
    : "—";
  const tceaLabel = preview ? `${(preview.tcea * 100).toFixed(2)}%` : "—";
  const vanLabel = preview
    ? `${currencySymbol}${formatAmount(preview.van)}`
    : "—";
  const tirLabel = preview ? `${(preview.tir * 100).toFixed(2)}%` : "—";

  const inactiveBtn =
    "flex h-11 w-14 items-center justify-center rounded-lg border border-slate-200 bg-white font-semibold text-slate-700 transition hover:bg-slate-50";
  const activeBtn =
    "flex h-11 w-14 items-center justify-center rounded-lg bg-slate-900 font-semibold text-white shadow-sm";

  const [state, formAction, isPending] = useActionState(action, initialState);

  return (
    <div className="mx-auto flex w-full max-w-6xl flex-col gap-6 px-6 py-8">
      <header className="flex items-center gap-4">
        <span className="flex h-12 w-12 shrink-0 items-center justify-center rounded-xl bg-slate-900 text-white shadow-sm">
          <SlidersHorizontal className="h-6 w-6" />
        </span>
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-slate-900 sm:text-3xl">
            {c.title}
          </h1>
          <p className="mt-1 text-sm text-slate-500">{c.subtitle}</p>
        </div>
      </header>

      <form
        action={formAction}
        className="grid gap-6 xl:grid-cols-[minmax(0,1fr)_340px] xl:items-start"
      >
        <input type="hidden" name="vehicleId" value={activeVehicle.id} />
        <input type="hidden" name="term" value={selectedTerm} />
        <input type="hidden" name="isCapital" value={String(isCapital)} />
        <input type="hidden" name="rateType" value={rateType} />
        <input type="hidden" name="compounding" value={compounding} />
        <input type="hidden" name="insuranceRate" value={insuranceRate} />

        <div className="flex flex-col gap-6">
          {/* Vehículo objetivo */}
          <div className={`flex flex-col gap-5 sm:flex-row sm:items-center ${cardClass}`}>
            <div className="flex h-36 w-full shrink-0 items-center justify-center overflow-hidden rounded-lg bg-gradient-to-br from-slate-100 to-slate-200 sm:w-56">
              {activeVehicle.imageUrl ? (
                // eslint-disable-next-line @next/next/no-img-element
                <img
                  className="h-full w-full object-contain p-2"
                  src={activeVehicle.imageUrl}
                  alt={`${activeVehicle.brand} ${activeVehicle.model}`}
                />
              ) : (
                <Car className="h-16 w-16 text-slate-400" strokeWidth={1.2} />
              )}
            </div>
            <div className="flex-1">
              <label htmlFor="vehicleSelect" className={labelClass}>
                {c.targetAsset}
              </label>
              <Select
                value={selectedVehicleId || undefined}
                onValueChange={setSelectedVehicleId}
              >
                <SelectTrigger
                  id="vehicleSelect"
                  className="mt-1 h-auto w-full rounded-none border-0 border-b border-slate-200 bg-transparent px-0 py-1 text-2xl font-bold text-slate-900 shadow-none focus-visible:border-slate-900 focus-visible:ring-0"
                >
                  <SelectValue placeholder={c.selectVehicle} />
                </SelectTrigger>
                <SelectContent>
                  {vehicles.map((v) => (
                    <SelectItem key={v.id} value={v.id}>
                      {v.brand} {v.model}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              <div className="mt-4 flex items-center justify-between gap-4">
                <div>
                  <p className="text-[11px] font-medium uppercase tracking-wide text-slate-400">
                    {c.msrp}
                  </p>
                  <p className="text-lg font-bold text-slate-900">
                    {currencySymbol}
                    {formatAmount(activeVehicle.price)}
                  </p>
                </div>
                <span className="rounded-full bg-emerald-50 px-3 py-1 text-xs font-semibold uppercase tracking-wide text-emerald-600 ring-1 ring-emerald-600/20">
                  {c.preApproved}
                </span>
              </div>
            </div>
          </div>

          <div className="grid grid-cols-1 items-start gap-6 md:grid-cols-2">
            {/* Cuota inicial */}
            <div className={`flex flex-col ${cardClass}`}>
              <label htmlFor="DownPaymentPercent" className={labelClass}>
                {c.downPayment}
              </label>
              <p className={helpClass}>{c.downPaymentHelp}</p>
              <div className="mt-4 flex items-center gap-3 text-xs font-medium text-slate-400">
                <span>10%</span>
                <div className={`flex-1 ${fieldClass}`}>
                  <input
                    type="number"
                    id="DownPaymentPercent"
                    name="downPayment"
                    min="10.00"
                    max="60.00"
                    step="0.01"
                    value={downPaymentPercent}
                    onChange={(e) => setDownPaymentPercent(e.target.value)}
                    className="w-full bg-transparent font-semibold text-slate-900 focus:outline-none"
                  />
                </div>
                <span>60%</span>
              </div>
              <p className="mt-auto border-t border-slate-100 pt-3 text-sm text-slate-500">
                {c.amount}:{" "}
                <span className="font-bold text-slate-900">
                  {currencySymbol}
                  {formatAmount(calculatedDownPaymentAmount)}
                </span>
              </p>
            </div>

            {/* Cuota balón */}
            <div className={`flex flex-col ${cardClass}`}>
              <label htmlFor="BalloonPaymentPercent" className={labelClass}>
                {c.balloon}
              </label>
              <p className={helpClass}>{c.balloonHelp}</p>
              <div className="mt-4 flex items-center gap-3 text-xs font-medium text-slate-400">
                <span>0%</span>
                <div className={`flex-1 ${fieldClass}`}>
                  <input
                    type="number"
                    id="BalloonPaymentPercent"
                    name="balloonFee"
                    min="0.00"
                    max="40.00"
                    step="0.01"
                    value={balloonPercent}
                    onChange={(e) => setBalloonPercent(e.target.value)}
                    className="w-full bg-transparent font-semibold text-slate-900 focus:outline-none"
                  />
                </div>
                <span>40%</span>
              </div>
              <p className="mt-auto border-t border-slate-100 pt-3 text-sm text-slate-500">
                {c.amount}:{" "}
                <span className="font-bold text-slate-900">
                  {currencySymbol}
                  {formatAmount(calculatedBalloonAmount)}
                </span>
              </p>
            </div>

            {/* Plazo */}
            <div className={`md:col-span-2 ${cardClass}`}>
              <span className={labelClass}>{c.term}</span>
              <p className={helpClass}>{c.termHelp}</p>
              <div className="mt-4 flex flex-wrap gap-2">
                {[12, 24, 36, 48].map((term) => (
                  <button
                    key={term}
                    type="button"
                    className={selectedTerm === term ? activeBtn : inactiveBtn}
                    onClick={() => setSelectedTerm(term)}
                  >
                    {term}
                  </button>
                ))}
              </div>
            </div>

            {/* Tasa de interés */}
            <div className={`md:col-span-2 flex flex-col ${cardClass}`}>
              <label htmlFor="InterestRate" className={labelClass}>
                {c.interestRate}
              </label>
              <p className={helpClass}>{c.interestRateHelp}</p>
              <div className={`relative mt-4 flex items-center ${fieldClass}`}>
                <input
                  type="number"
                  id="InterestRate"
                  name="annualRate"
                  min="0.01"
                  max="99.00"
                  step="0.01"
                  value={interestRate}
                  onChange={(e) => setInterestRate(e.target.value)}
                  className="w-full bg-transparent pr-6 text-lg font-bold text-slate-900 focus:outline-none"
                />
                <span className="pointer-events-none absolute right-3 font-semibold text-slate-400">
                  %
                </span>
              </div>

              {/* Tipo de tasa: efectiva o nominal (con capitalización) */}
              <div className="mt-4 flex flex-col gap-3">
                <div>
                  <span className={labelClass}>{c.rateTypeLabel}</span>
                  <div className="mt-2 flex gap-2">
                    <button
                      type="button"
                      onClick={() => setRateType("effective")}
                      className={
                        rateType === "effective" ? activeBtn : inactiveBtn
                      }
                      style={{ width: "auto", paddingInline: "0.75rem" }}
                    >
                      {c.effectiveOption}
                    </button>
                    <button
                      type="button"
                      onClick={() => setRateType("nominal")}
                      className={
                        rateType === "nominal" ? activeBtn : inactiveBtn
                      }
                      style={{ width: "auto", paddingInline: "0.75rem" }}
                    >
                      {c.nominalOption}
                    </button>
                  </div>
                </div>

                {rateType === "nominal" ? (
                  <label className="flex flex-col">
                    <span className={labelClass}>{c.compoundingLabel}</span>
                    <Select
                      value={String(compounding)}
                      onValueChange={(value) => setCompounding(Number(value))}
                    >
                      <SelectTrigger
                        className={`mt-2 h-auto w-full justify-between ${fieldClass} font-semibold text-slate-900`}
                      >
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value={String(COMPOUNDING_PER_YEAR.monthly)}>
                          {c.compMonthly}
                        </SelectItem>
                        <SelectItem value={String(COMPOUNDING_PER_YEAR.bimonthly)}>
                          {c.compBimonthly}
                        </SelectItem>
                        <SelectItem value={String(COMPOUNDING_PER_YEAR.quarterly)}>
                          {c.compQuarterly}
                        </SelectItem>
                        <SelectItem value={String(COMPOUNDING_PER_YEAR.semiannual)}>
                          {c.compSemiannual}
                        </SelectItem>
                        <SelectItem value={String(COMPOUNDING_PER_YEAR.annual)}>
                          {c.compAnnual}
                        </SelectItem>
                      </SelectContent>
                    </Select>
                  </label>
                ) : null}

                <label className="flex flex-col">
                  <span className={labelClass}>{c.insuranceLabel}</span>
                  <p className={helpClass}>{c.insuranceHelp}</p>
                  <input
                    type="number"
                    name="insuranceRateDisplay"
                    min="0"
                    max="5"
                    step="0.01"
                    value={insuranceRate}
                    onChange={(e) => setInsuranceRate(e.target.value)}
                    className={`mt-2 ${fieldClass} font-semibold text-slate-900`}
                  />
                </label>
              </div>
            </div>

            {/* Periodo de gracia */}
            <div className={`md:col-span-2 ${cardClass}`}>
              <label htmlFor="GracePeriod" className={labelClass}>
                {c.gracePeriod}
              </label>
              <p className={helpClass}>{c.gracePeriodHelp}</p>
              <div className="mt-4 flex flex-col items-stretch gap-4 sm:flex-row sm:items-center">
                <div className={`flex flex-1 items-center justify-between ${fieldClass}`}>
                  <input
                    type="number"
                    id="GracePeriod"
                    name="gracePeriod"
                    min="0"
                    max="6"
                    step="1"
                    value={gracePeriod}
                    onChange={(e) => setGracePeriod(e.target.value)}
                    className="w-full bg-transparent font-semibold text-slate-900 focus:outline-none"
                  />
                  <span className="whitespace-nowrap text-sm font-medium text-slate-400">
                    {c.months}
                  </span>
                </div>
                <label className="flex cursor-pointer select-none items-center justify-center gap-2.5 rounded-lg border border-slate-200 px-4 py-2.5 text-sm font-medium text-slate-700 transition hover:bg-slate-50">
                  <input
                    type="checkbox"
                    checked={isCapital}
                    onChange={(e) => setIsCapital(e.target.checked)}
                    className="h-4 w-4 cursor-pointer rounded border-slate-300"
                  />
                  <span>{c.capitalGraceOnly}</span>
                </label>
              </div>
            </div>
          </div>
        </div>

        {/* Resumen de simulación */}
        <aside className="flex flex-col self-start rounded-xl bg-slate-900 p-6 shadow-sm xl:sticky xl:top-24">
          <div>
            <h2 className="text-xl font-semibold text-white">
              {c.summaryTitle}
            </h2>
            <p className="text-sm text-slate-400">{c.summarySubtitle}</p>
          </div>

          <div className="mt-6 space-y-3 text-sm">
            <div className="flex justify-between">
              <span className="font-medium text-slate-400">
                {c.amountFinanced}
              </span>
              <span className="font-bold text-white">
                {currencySymbol}
                {formatAmount(amountFinanced)}
              </span>
            </div>
            <div className="flex justify-between">
              <span className="font-medium text-slate-400">{c.termLabel}</span>
              <span className="font-bold text-white">
                {selectedTerm} {c.monthsSuffix}
              </span>
            </div>
            <div className="flex justify-between">
              <span className="font-medium text-slate-400">
                {rateType === "nominal" ? c.nominalOption : c.effectiveOption}
              </span>
              <span className="font-bold text-white">{interestRate}%</span>
            </div>
            <div className="flex justify-between">
              <span className="font-medium text-slate-400">{c.vanLabel}</span>
              <span className="font-bold text-white">{vanLabel}</span>
            </div>
            <div className="flex justify-between">
              <span className="font-medium text-slate-400">{c.tirLabel}</span>
              <span className="font-bold text-white">{tirLabel}</span>
            </div>
            <div className="flex justify-between border-t border-slate-700 pt-3 text-base">
              <span className="font-medium text-slate-400">
                {c.calculatedTcea}
              </span>
              <span className="font-bold text-emerald-300">{tceaLabel}</span>
            </div>
          </div>

          <div className="mt-8">
            <p className="text-xs font-semibold uppercase tracking-wider text-slate-400">
              {c.monthlyPayment}
            </p>
            <p className="mt-1 text-4xl font-bold tracking-tight text-emerald-400">
              {monthlyPaymentLabel}
            </p>
            <div className="mt-3 rounded-lg border border-slate-700 bg-slate-800/50 p-2 text-center text-xs text-slate-400">
              {c.projectedBase}
            </div>
          </div>

          {state.error ? (
            <p className="mt-4 rounded-lg border border-red-800 bg-red-900/40 p-3 text-center text-xs font-medium text-red-200">
              {c.errorPrefix}
              {state.error}
            </p>
          ) : null}
          {state.success ? (
            <p className="mt-4 rounded-lg border border-emerald-800 bg-emerald-900/40 p-3 text-center text-xs font-medium text-emerald-200">
              {c.savedOk}
            </p>
          ) : null}

          <div className="mt-auto space-y-3 border-t border-slate-700 pt-6">
            <button
              type="submit"
              disabled={isPending}
              className="w-full rounded-lg bg-white p-3.5 text-center text-sm font-bold text-slate-900 shadow-sm transition hover:bg-slate-100 disabled:cursor-not-allowed disabled:opacity-50"
            >
              {isPending ? c.saving : c.saveScenario}
            </button>
          </div>
        </aside>
      </form>
    </div>
  );
}
