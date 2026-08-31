"use server";

import { revalidatePath } from "next/cache";

import { createVehicle } from "@/application/use-cases/vehicles/create-vehicle";
import { deleteVehicle } from "@/application/use-cases/vehicles/delete-vehicle";
import { updateVehicle } from "@/application/use-cases/vehicles/update-vehicle";
import { createSupabaseVehicleRepository } from "@/infrastructure/repositories/supabase-vehicle-repository";
import type {
  VehicleDeleteState,
  VehicleFormState,
} from "@/shared/types/vehicle-form-state";
import { createSupabaseServerClient } from "@/infrastructure/services/supabase/server";

export async function saveVehicleAction(
  _prevState: VehicleFormState,
  formData: FormData,
): Promise<VehicleFormState> {
  const supabase = await createSupabaseServerClient();
  const repository = createSupabaseVehicleRepository(supabase);
  const vehicleId = String(formData.get("vehicleId") ?? "").trim();
  const shared = {
    financialEntityId: String(formData.get("financialEntityId") ?? ""),
    brand: String(formData.get("brand") ?? ""),
    model: String(formData.get("model") ?? ""),
    manufactureYear: String(formData.get("manufactureYear") ?? ""),
    condition: String(formData.get("condition") ?? ""),
    currency: String(formData.get("currency") ?? ""),
    price: String(formData.get("price") ?? ""),
  };

  if (vehicleId) {
    const result = await updateVehicle(repository, { id: vehicleId, ...shared });
    if (!result.ok) {
      return { error: result.error, success: false };
    }

    revalidatePath("/vehicles");
    revalidatePath("/dashboard");
    return { error: null, success: true, mode: "updated" };
  }

  const result = await createVehicle(repository, shared);
  if (!result.ok) {
    return { error: result.error, success: false };
  }

  revalidatePath("/vehicles");
  revalidatePath("/dashboard");
  return { error: null, success: true, mode: "created" };
}

export async function deleteVehicleAction(
  _prevState: VehicleDeleteState,
  formData: FormData,
): Promise<VehicleDeleteState> {
  const supabase = await createSupabaseServerClient();
  const repository = createSupabaseVehicleRepository(supabase);
  const result = await deleteVehicle(
    repository,
    String(formData.get("vehicleId") ?? ""),
  );

  if (!result.ok) {
    return { error: result.error, success: false };
  }

  revalidatePath("/vehicles");
  revalidatePath("/dashboard");
  return { error: null, success: true };
}
