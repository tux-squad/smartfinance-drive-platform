"use server"

import { createCreditSimulation } from "@/application/use-cases/credit/create-credit-simulation";
import { listCurrentUserVehicles } from "@/application/use-cases/vehicles/list-current-user-vehicles";
import { createSupabaseCreditRepository } from "@/infrastructure/repositories/supabase-credit-simulation-repository";
import { createSupabaseVehicleRepository } from "@/infrastructure/repositories/supabase-vehicle-repository";
import { createSupabaseServerClient } from "@/infrastructure/services/supabase/server";
import {
  DEFAULT_MONTHLY_COMMISSION,
  DEFAULT_MONTHLY_INSURANCE_RATE,
} from "@/shared/config/financing";
import { VehicleFormState } from "@/shared/types/vehicle-form-state";
import { revalidatePath } from "next/cache";

export async function createSimulationAction(
  _prevState: VehicleFormState,
  formData: FormData,
): Promise<VehicleFormState> {
  const supabase = await createSupabaseServerClient();

  const vehicleId = String(formData.get("vehicleId") ?? "");

  // Precio y moneda AUTORITATIVOS: se leen del vehículo en BD, nunca del cliente,
  // para que el cálculo no dependa de valores manipulables en el navegador.
  const vehicles = await listCurrentUserVehicles(
    createSupabaseVehicleRepository(supabase),
  );
  const vehicle = vehicles.find((item) => item.id === vehicleId);

  if (!vehicle) {
    return { error: "Selecciona un vehículo válido.", success: false };
  }

  // La UI envía cuota inicial y balón como PORCENTAJE del precio; aquí se
  // convierten a monto usando el precio autoritativo.
  const downPaymentPercent = Number(formData.get("downPayment") ?? 0);
  const balloonPercent = Number(formData.get("balloonFee") ?? 0);
  const rateTypeRaw = String(formData.get("rateType") ?? "effective");
  const rateType = rateTypeRaw === "nominal" ? "nominal" : "effective";

  const simulation = await createCreditSimulation(
    createSupabaseCreditRepository(supabase),
    {
      vehicleId,
      vehiclePrice: vehicle.price,
      currency: vehicle.currency,
      vehicleLabel: `${vehicle.brand} ${vehicle.model}`,
      downPayment: (vehicle.price * downPaymentPercent) / 100,
      balloonFee: (vehicle.price * balloonPercent) / 100,
      gracePeriod: Number(formData.get("gracePeriod") ?? 0),
      term: Number(formData.get("term") ?? 0),
      rateType,
      annualRate: Number(formData.get("annualRate") ?? 0),
      compoundingPeriodsPerYear: Number(
        formData.get("compounding") ?? 12,
      ),
      monthlyInsuranceRate:
        formData.get("insuranceRate") != null
          ? Number(formData.get("insuranceRate")) / 100
          : DEFAULT_MONTHLY_INSURANCE_RATE,
      monthlyCommission:
        formData.get("commission") != null
          ? Number(formData.get("commission"))
          : DEFAULT_MONTHLY_COMMISSION,
      isCapital: String(formData.get("isCapital") ?? "false") === "true",
    },
  );

  if (!simulation.ok) {
    return { error: simulation.error, success: false };
  }

  revalidatePath("/credit");
  revalidatePath("/dashboard");
  revalidatePath("/payment");
  revalidatePath("/financial-metrics");
  return { error: null, success: true };
}
