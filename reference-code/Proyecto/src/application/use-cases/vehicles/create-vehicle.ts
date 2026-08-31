import type { Vehicle } from "@/domain/entities/vehicle";
import type { VehicleRepository } from "@/domain/repositories/vehicle-repository";
import {
  VEHICLE_CONDITION_VALUES,
  VEHICLE_CURRENCY_VALUES,
} from "@/shared/constants/vehicle";
import type { Result } from "@/shared/types/result";

export interface CreateVehicleInput {
  financialEntityId: string;
  brand: string;
  model: string;
  manufactureYear: string;
  condition: string;
  currency: string;
  price: string;
}

function clean(value: string): string {
  return value.trim();
}

export async function createVehicle(
  repository: VehicleRepository,
  input: CreateVehicleInput,
): Promise<Result<Vehicle>> {
  const financialEntityId = clean(input.financialEntityId);
  const brand = clean(input.brand);
  const model = clean(input.model);
  const condition = clean(input.condition);
  const currency = clean(input.currency);

  if (!financialEntityId) {
    return { ok: false, error: "Select a financial entity." };
  }

  if (!brand || !model) {
    return { ok: false, error: "Brand and model are required." };
  }

  const manufactureYear = Number(clean(input.manufactureYear));
  const maxYear = new Date().getFullYear() + 1;
  if (
    !Number.isInteger(manufactureYear) ||
    manufactureYear < 1900 ||
    manufactureYear > maxYear
  ) {
    return {
      ok: false,
      error: `Year of manufacture must be between 1900 and ${maxYear}.`,
    };
  }

  if (!VEHICLE_CONDITION_VALUES.includes(condition as never)) {
    return { ok: false, error: "Select a valid vehicle condition." };
  }

  if (!VEHICLE_CURRENCY_VALUES.includes(currency as never)) {
    return { ok: false, error: "Select a valid currency." };
  }

  const price = Number(clean(input.price));
  if (Number.isNaN(price) || price <= 0) {
    return { ok: false, error: "Vehicle price must be greater than zero." };
  }

  try {
    const vehicle = await repository.create({
      financialEntityId,
      brand,
      model,
      manufactureYear,
      condition: condition as "new" | "used",
      currency: currency as "PEN" | "USD",
      price,
    });

    return { ok: true, value: vehicle };
  } catch {
    return {
      ok: false,
      error: "Could not register the vehicle. Please try again.",
    };
  }
}
