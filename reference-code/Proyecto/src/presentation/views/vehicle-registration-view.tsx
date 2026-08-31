"use client";

import {
  Car,
  Landmark,
  Pencil,
  Plus,
  Save,
  Sparkles,
  Trash2,
  X,
} from "lucide-react";
import { startTransition, useMemo, useState, useActionState } from "react";
import type { FormEvent } from "react";

import type { FinancialEntity } from "@/domain/entities/financial-entity";
import type { Vehicle } from "@/domain/entities/vehicle";
import { Button } from "@/presentation/components/ui/button";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/presentation/components/ui/select";
import { Toast } from "@/presentation/components/ui/toast";
import { useLanguage } from "@/presentation/i18n/language-context";
import { portalDictionary } from "@/presentation/i18n/portal-dictionary";
import {
  VehicleBrandOption,
  VEHICLE_BRAND_IMAGE,
  VEHICLE_BRANDS,
  VEHICLE_CONDITIONS,
  VEHICLE_CURRENCIES,
  VEHICLE_MODELS_BY_BRAND,
} from "@/shared/constants/vehicle";
import type {
  VehicleDeleteAction,
  VehicleFormAction,
  VehicleFormState,
} from "@/shared/types/vehicle-form-state";

const initialState: VehicleFormState = { error: null, success: false };
const initialDeleteState = { error: null, success: false };

const labelClass =
  "text-xs font-semibold uppercase tracking-wide text-muted-foreground";
const inputClass =
  "w-full rounded-md border border-input bg-background px-3.5 py-2.5 text-sm text-foreground shadow-xs outline-none transition placeholder:text-muted-foreground focus-visible:border-ring focus-visible:ring-[3px] focus-visible:ring-ring/50";
const selectTriggerClass =
  "h-auto w-full justify-between rounded-md border border-input bg-background px-3.5 py-2.5 text-sm text-foreground shadow-xs outline-none transition data-[placeholder]:text-muted-foreground focus-visible:border-ring focus-visible:ring-[3px] focus-visible:ring-ring/50";

type VehicleRegistrationViewProps = {
  action: VehicleFormAction;
  deleteAction: VehicleDeleteAction;
  financialEntities: FinancialEntity[];
  vehicles: Vehicle[];
};

const KNOWN_BRAND_VALUES = new Set(
  VEHICLE_BRANDS.map((option) => option.value),
);

type BrandModelSelection = {
  brandOption: VehicleBrandOption;
  modelOption: string;
};

function mapBrandModel(brand: string, model: string): BrandModelSelection {
  if (KNOWN_BRAND_VALUES.has(brand as VehicleBrandOption)) {
    const brandOption = brand as VehicleBrandOption;
    const models = VEHICLE_MODELS_BY_BRAND[brandOption];
    return {
      brandOption,
      modelOption: models.includes(model) ? model : models[0],
    };
  }

  return {
    brandOption: VehicleBrandOption.Toyota,
    modelOption: VEHICLE_MODELS_BY_BRAND[VehicleBrandOption.Toyota][0],
  };
}

function formatMoney(value: number, currency: string): string {
  return new Intl.NumberFormat("en-US", {
    currency: currency === "USD" ? "USD" : "PEN",
    maximumFractionDigits: 2,
    style: "currency",
  }).format(value);
}

function getModelsForBrand(brand: VehicleBrandOption): string[] {
  return VEHICLE_MODELS_BY_BRAND[brand];
}

export function VehicleRegistrationView({
  action,
  deleteAction,
  financialEntities,
  vehicles,
}: VehicleRegistrationViewProps) {
  const { lang } = useLanguage();
  const v = portalDictionary[lang].vehicles;
  const cancelLabel = portalDictionary[lang].cancel;

  const [state, formAction, isPending] = useActionState(action, initialState);
  const [deleteState, deleteFormAction, isDeleting] = useActionState(
    deleteAction,
    initialDeleteState,
  );

  const [selectedVehicleId, setSelectedVehicleId] = useState<string | null>(
    null,
  );
  const [pendingDelete, setPendingDelete] = useState<Vehicle | null>(null);
  const [brandOption, setBrandOption] = useState<VehicleBrandOption>(
    VehicleBrandOption.Toyota,
  );
  const [modelOption, setModelOption] = useState("Hilux");
  const [manufactureYear, setManufactureYear] = useState("");
  const [condition, setCondition] = useState("new");
  const [currency, setCurrency] = useState("PEN");
  const [price, setPrice] = useState("");
  const [financialEntityId, setFinancialEntityId] = useState(
    financialEntities[0]?.id ?? "",
  );

  const startNew = () => {
    setSelectedVehicleId(null);
    setBrandOption(VehicleBrandOption.Toyota);
    setModelOption("Hilux");
    setManufactureYear("");
    setCondition("new");
    setCurrency("PEN");
    setPrice("");
    setFinancialEntityId(financialEntities[0]?.id ?? "");
  };

  const applyVehicle = (vehicle: Vehicle) => {
    const selection = mapBrandModel(vehicle.brand, vehicle.model);
    setSelectedVehicleId(vehicle.id);
    setBrandOption(selection.brandOption);
    setModelOption(selection.modelOption);
    setManufactureYear(String(vehicle.manufactureYear));
    setCondition(vehicle.condition);
    setCurrency(vehicle.currency);
    setPrice(String(vehicle.price));
    setFinancialEntityId(vehicle.financialEntityId);
  };

  // Close the confirmation modal once a delete completes successfully, and
  // (re)arm the toast notifications whenever a new action result comes in.
  // Adjusting state during render (rather than in an effect) avoids an
  // extra commit + effect round-trip for this state sync.
  const [handledDeleteState, setHandledDeleteState] = useState(deleteState);
  const [deleteToastDismissed, setDeleteToastDismissed] = useState(false);
  if (deleteState !== handledDeleteState) {
    setHandledDeleteState(deleteState);
    setDeleteToastDismissed(false);
    if (deleteState.success && pendingDelete) {
      if (pendingDelete.id === selectedVehicleId) {
        startNew();
      }
      setPendingDelete(null);
    }
  }

  const [handledFormState, setHandledFormState] = useState(state);
  const [formToastDismissed, setFormToastDismissed] = useState(false);
  if (state !== handledFormState) {
    setHandledFormState(state);
    setFormToastDismissed(false);
  }

  const conditionText = (value: string): string =>
    v.conditions[value as "new" | "used"] ?? v.conditions.new;

  const selectedEntity = useMemo(
    () =>
      financialEntities.find((entity) => entity.id === financialEntityId)
        ?.name ?? v.selectInstitution,
    [financialEntities, financialEntityId, v.selectInstitution],
  );
  const parsedPrice = Number(price);
  const previewPrice =
    Number.isNaN(parsedPrice) || parsedPrice <= 0 ? 0 : parsedPrice;
  const modelOptions = getModelsForBrand(brandOption);
  // Radix's <Select> ignores the <SelectValue> children and renders an empty
  // placeholder whenever its `value` is "" or out of sync with the mounted
  // items. The model list is dynamic (it depends on the brand), so normalize
  // it to a value that always exists among the current options.
  const safeModelOption = modelOptions.includes(modelOption)
    ? modelOption
    : modelOptions[0] ?? "";
  const brand = brandOption;
  const model = safeModelOption;
  const isEditing = selectedVehicleId !== null;
  const brandLabel =
    VEHICLE_BRANDS.find((option) => option.value === brandOption)?.label ??
    brandOption;
  const currencyLabel =
    VEHICLE_CURRENCIES.find((option) => option.value === currency)?.label ??
    currency;

  const activeToast = !deleteToastDismissed && deleteState.success
    ? { variant: "success" as const, message: v.deleteSuccess, dismiss: () => setDeleteToastDismissed(true) }
    : !formToastDismissed && state.error
      ? { variant: "error" as const, message: state.error, dismiss: () => setFormToastDismissed(true) }
      : !formToastDismissed && state.success
        ? {
            variant: "success" as const,
            message: state.mode === "updated" ? v.updateSuccess : v.success,
            dismiss: () => setFormToastDismissed(true),
          }
        : null;
  const previewImageUrl = VEHICLE_BRAND_IMAGE[brandOption];

  return (
    <div className="mx-auto flex w-full max-w-7xl flex-col gap-6 px-6 py-8">
      {activeToast ? (
        <Toast
          variant={activeToast.variant}
          message={activeToast.message}
          onDismiss={activeToast.dismiss}
        />
      ) : null}
      <header className="flex items-center gap-4">
        <span className="flex h-12 w-12 shrink-0 items-center justify-center rounded-xl bg-slate-900 text-white shadow-sm">
          <Car className="h-6 w-6" />
        </span>
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-foreground sm:text-3xl">
            {v.title}
          </h1>
          <p className="mt-1 text-sm text-muted-foreground">{v.subtitle}</p>
        </div>
      </header>

      {/* Registered vehicles strip: pick one to edit or add a new one. */}
      <section className="rounded-xl border border-border bg-card p-4 shadow-sm">
        <div className="mb-3 flex items-center justify-between gap-3 px-1">
          <h2 className="text-base font-semibold text-foreground">
            {v.registeredTitle}
          </h2>
          <p className="hidden text-xs text-muted-foreground sm:block">
            {v.registeredHint}
          </p>
        </div>
        <div className="flex snap-x items-start gap-3 overflow-x-auto pb-2">
          <button
            type="button"
            onClick={startNew}
            className={`flex h-28 w-44 shrink-0 snap-start flex-col items-center justify-center gap-2 rounded-lg border-2 border-dashed p-4 text-center transition ${
              !isEditing
                ? "border-slate-900 bg-slate-900/5 text-foreground"
                : "border-border text-muted-foreground hover:border-ring hover:bg-accent"
            }`}
          >
            <span className="flex h-9 w-9 items-center justify-center rounded-full bg-slate-900 text-white">
              <Plus className="h-5 w-5" />
            </span>
            <span className="text-sm font-semibold">{v.addNew}</span>
          </button>

          {vehicles.map((vehicle) => {
            const isSelected = vehicle.id === selectedVehicleId;
            return (
              <div
                key={vehicle.id}
                className={`relative h-28 w-56 shrink-0 snap-start rounded-lg border p-3 text-left transition ${
                  isSelected
                    ? "border-slate-900 ring-2 ring-slate-900/20"
                    : "border-border hover:border-ring hover:shadow-sm"
                }`}
              >
                <button
                  type="button"
                  onClick={() => setPendingDelete(vehicle)}
                  aria-label={v.deleteAria}
                  className="absolute right-2 top-2 z-10 flex h-6 w-6 items-center justify-center rounded-full bg-card/90 text-muted-foreground shadow-sm backdrop-blur transition hover:bg-red-50 hover:text-red-600"
                >
                  <X className="h-3.5 w-3.5" />
                </button>
                <button
                  type="button"
                  onClick={() => applyVehicle(vehicle)}
                  className="flex h-full w-full items-center gap-3 pr-5 text-left"
                >
                  {vehicle.imageUrl ? (
                    // eslint-disable-next-line @next/next/no-img-element
                    <img
                      alt={`${vehicle.brand} ${vehicle.model}`}
                      className="h-12 w-16 shrink-0 rounded-md bg-muted object-contain"
                      src={vehicle.imageUrl}
                    />
                  ) : (
                    <span className="flex h-12 w-16 shrink-0 items-center justify-center rounded-md bg-muted text-muted-foreground">
                      <Car className="h-5 w-5" />
                    </span>
                  )}
                  <div className="min-w-0 flex-1">
                    <h3 className="truncate text-sm font-semibold text-foreground">
                      {vehicle.brand} {vehicle.model}
                    </h3>
                    <p className="truncate text-xs text-muted-foreground">
                      {vehicle.manufactureYear} ·{" "}
                      {conditionText(vehicle.condition)}
                    </p>
                    <p className="mt-0.5 truncate text-xs font-semibold text-foreground">
                      {formatMoney(vehicle.price, vehicle.currency)}
                    </p>
                  </div>
                </button>
              </div>
            );
          })}
        </div>
      </section>

      <div className="grid items-stretch gap-6 xl:grid-cols-[minmax(0,1fr)_360px]">
        <form
          onSubmit={(event: FormEvent<HTMLFormElement>) => {
            // Invoking the action manually (instead of via the `action` prop)
            // avoids React's automatic native form reset after a successful
            // submission, which Radix's <Select> listens to and uses to snap
            // its value back to whatever it was on first mount.
            event.preventDefault();
            const formData = new FormData(event.currentTarget);
            startTransition(() => {
              formAction(formData);
            });
          }}
          className="flex h-full flex-col rounded-xl border border-border bg-card p-6 shadow-sm"
        >
          <input name="vehicleId" type="hidden" value={selectedVehicleId ?? ""} />

          <div className="flex-1">
          <div className="mb-5 flex items-center justify-between gap-3">
            <span
              className={`inline-flex items-center gap-1.5 rounded-full px-3 py-1 text-xs font-semibold ${
                isEditing
                  ? "bg-amber-100 text-amber-700"
                  : "bg-emerald-100 text-emerald-700"
              }`}
            >
              {isEditing ? (
                <Pencil className="h-3.5 w-3.5" />
              ) : (
                <Sparkles className="h-3.5 w-3.5" />
              )}
              {isEditing ? v.editingBadge : v.newBadge}
            </span>
          </div>

          <p className="mb-4 text-xs font-semibold uppercase tracking-wider text-muted-foreground">
            {v.sectionSpecs}
          </p>
          <div className="grid gap-5 md:grid-cols-2">
            <div className="flex flex-col gap-1.5">
              <label htmlFor="brandOption" className={labelClass}>
                {v.brand}
              </label>
              <Select
                value={brandOption}
                onValueChange={(value) => {
                  const nextBrand = value as VehicleBrandOption;
                  const nextModels = getModelsForBrand(nextBrand);
                  setBrandOption(nextBrand);
                  setModelOption(nextModels[0]);
                }}
              >
                <SelectTrigger id="brandOption" className={selectTriggerClass}>
                  <SelectValue>{brandLabel}</SelectValue>
                </SelectTrigger>
                <SelectContent>
                  {VEHICLE_BRANDS.map((option) => (
                    <SelectItem key={option.value} value={option.value}>
                      {option.label}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            <div className="flex flex-col gap-1.5">
              <label htmlFor="modelOption" className={labelClass}>
                {v.model}
              </label>
              <Select value={safeModelOption} onValueChange={setModelOption}>
                <SelectTrigger id="modelOption" className={selectTriggerClass}>
                  <SelectValue placeholder={v.model}>
                    {safeModelOption}
                  </SelectValue>
                </SelectTrigger>
                <SelectContent>
                  {modelOptions.map((option) => (
                    <SelectItem key={option} value={option}>
                      {option}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            <input name="brand" type="hidden" value={brand} />
            <input name="model" type="hidden" value={model} />

            <div className="flex flex-col gap-1.5">
              <label htmlFor="manufactureYear" className={labelClass}>
                {v.year}
              </label>
              <input
                id="manufactureYear"
                name="manufactureYear"
                inputMode="numeric"
                value={manufactureYear}
                onChange={(event) => setManufactureYear(event.target.value)}
                placeholder="2025"
                className={inputClass}
              />
            </div>

            <div className="flex flex-col gap-1.5">
              <label htmlFor="condition" className={labelClass}>
                {v.condition}
              </label>
              <input type="hidden" name="condition" value={condition} />
              <Select value={condition} onValueChange={setCondition}>
                <SelectTrigger id="condition" className={selectTriggerClass}>
                  <SelectValue>{conditionText(condition)}</SelectValue>
                </SelectTrigger>
                <SelectContent>
                  {VEHICLE_CONDITIONS.map((option) => (
                    <SelectItem key={option.value} value={option.value}>
                      {conditionText(option.value)}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            <div className="flex flex-col gap-1.5">
              <label htmlFor="currency" className={labelClass}>
                {v.currency}
              </label>
              <input type="hidden" name="currency" value={currency} />
              <Select value={currency} onValueChange={setCurrency}>
                <SelectTrigger id="currency" className={selectTriggerClass}>
                  <SelectValue>{currencyLabel}</SelectValue>
                </SelectTrigger>
                <SelectContent>
                  {VEHICLE_CURRENCIES.map((option) => (
                    <SelectItem key={option.value} value={option.value}>
                      {option.label}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            <div className="flex flex-col gap-1.5">
              <label htmlFor="price" className={labelClass}>
                {v.price}
              </label>
              <input
                id="price"
                name="price"
                inputMode="decimal"
                value={price}
                onChange={(event) => setPrice(event.target.value)}
                placeholder="0.00"
                className={inputClass}
              />
            </div>

          </div>

          <p className="mb-4 mt-7 text-xs font-semibold uppercase tracking-wider text-muted-foreground">
            {v.sectionFinancing}
          </p>
          <div className="flex flex-col gap-5">
            <div className="flex flex-col gap-1.5">
              <label htmlFor="financialEntityId" className={labelClass}>
                {v.financialEntity}
              </label>
              <input
                type="hidden"
                name="financialEntityId"
                value={financialEntityId}
              />
              <Select
                value={financialEntityId || undefined}
                onValueChange={setFinancialEntityId}
              >
                <SelectTrigger
                  id="financialEntityId"
                  className={selectTriggerClass}
                >
                  <SelectValue placeholder={v.selectInstitution}>
                    {selectedEntity}
                  </SelectValue>
                </SelectTrigger>
                <SelectContent>
                  {financialEntities.map((entity) => (
                    <SelectItem key={entity.id} value={entity.id}>
                      {entity.name}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
          </div>
          </div>

          <div className="mt-6 flex justify-end gap-3 border-t border-border pt-6">
            <Button
              type="reset"
              variant="outline"
              onClick={startNew}
            >
              {cancelLabel}
            </Button>
            <Button type="submit" disabled={isPending}>
              <Save className="size-4" />
              {isPending
                ? isEditing
                  ? v.updating
                  : v.saving
                : isEditing
                  ? v.update
                  : v.save}
            </Button>
          </div>
        </form>

        <aside className="flex h-full flex-col overflow-hidden rounded-xl border border-border bg-card shadow-sm">
          <div className="relative flex h-56 shrink-0 items-center justify-center overflow-hidden border-b border-border bg-white">
            <span className="absolute right-3 top-3 z-10 rounded-md bg-card/90 px-2 py-1 text-[11px] font-bold uppercase tracking-wide text-muted-foreground shadow-sm backdrop-blur">
              {v.preview}
            </span>
            {/* eslint-disable-next-line @next/next/no-img-element */}
            <img
              alt={`${brand} ${model}`}
              className="h-full w-full object-contain p-3"
              src={previewImageUrl}
            />
          </div>
          <div className="p-5">
            <h2 className="text-xs font-semibold uppercase tracking-wide text-muted-foreground">
              {v.summaryTitle}
            </h2>
            <dl className="mt-4 divide-y divide-border text-sm">
              <div className="flex justify-between gap-4 py-3">
                <dt className="text-muted-foreground">{v.brand}</dt>
                <dd className="font-semibold text-foreground">{brand || "--"}</dd>
              </div>
              <div className="flex justify-between gap-4 py-3">
                <dt className="text-muted-foreground">{v.model}</dt>
                <dd className="font-semibold text-foreground">{model || "--"}</dd>
              </div>
              <div className="flex justify-between gap-4 py-3">
                <dt className="text-muted-foreground">{v.summaryYear}</dt>
                <dd className="font-semibold text-foreground">
                  {manufactureYear || "--"}
                </dd>
              </div>
              <div className="flex justify-between gap-4 py-3">
                <dt className="text-muted-foreground">{v.condition}</dt>
                <dd className="font-semibold text-foreground">
                  {conditionText(condition)}
                </dd>
              </div>
              <div className="flex justify-between gap-4 py-3">
                <dt className="text-muted-foreground">{v.estValue}</dt>
                <dd className="font-bold text-foreground">
                  {formatMoney(previewPrice, currency)}
                </dd>
              </div>
            </dl>
            <div className="mt-5 rounded-lg border border-border bg-muted p-4">
              <div className="flex gap-3">
                <Landmark className="mt-0.5 h-4 w-4 shrink-0 text-muted-foreground" />
                <p className="text-xs leading-5 text-muted-foreground">
                  {v.linkedInfoBefore}
                  <span className="font-medium text-foreground">
                    {selectedEntity}
                  </span>
                  {v.linkedInfoAfter}
                </p>
              </div>
            </div>
          </div>
        </aside>
      </div>

      {pendingDelete ? (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
          <button
            type="button"
            aria-label={cancelLabel}
            onClick={() => {
              if (!isDeleting) {
                setPendingDelete(null);
              }
            }}
            className="absolute inset-0 cursor-default bg-black/50 backdrop-blur-sm"
          />
          <div
            role="dialog"
            aria-modal="true"
            className="relative z-10 w-full max-w-md rounded-xl border border-border bg-card p-6 shadow-xl"
          >
            <div className="flex items-start gap-3">
              <span className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-red-100 text-red-600">
                <Trash2 className="h-5 w-5" />
              </span>
              <div className="min-w-0">
                <h3 className="text-base font-semibold text-foreground">
                  {v.deleteTitle}
                </h3>
                <p className="mt-1 truncate text-sm font-medium text-foreground">
                  {pendingDelete.manufactureYear} {pendingDelete.brand}{" "}
                  {pendingDelete.model}
                </p>
                <p className="mt-1 text-sm text-muted-foreground">
                  {v.deleteMessage}
                </p>
              </div>
            </div>

            {deleteState.error ? (
              <p
                role="alert"
                className="mt-4 rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700"
              >
                {deleteState.error}
              </p>
            ) : null}

            <form action={deleteFormAction} className="mt-6 flex justify-end gap-3">
              <input type="hidden" name="vehicleId" value={pendingDelete.id} />
              <Button
                type="button"
                variant="outline"
                disabled={isDeleting}
                onClick={() => setPendingDelete(null)}
              >
                {cancelLabel}
              </Button>
              <Button type="submit" variant="destructive" disabled={isDeleting}>
                <Trash2 className="size-4" />
                {isDeleting ? v.deleting : v.deleteCta}
              </Button>
            </form>
          </div>
        </div>
      ) : null}
    </div>
  );
}
