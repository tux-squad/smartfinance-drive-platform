import type { VehicleRepository } from "@/domain/repositories/vehicle-repository";
import type { Result } from "@/shared/types/result";

export async function deleteVehicle(
  repository: VehicleRepository,
  id: string,
): Promise<Result<null>> {
  const vehicleId = id.trim();

  if (!vehicleId) {
    return { ok: false, error: "Select a vehicle to delete." };
  }

  try {
    await repository.delete(vehicleId);
    return { ok: true, value: null };
  } catch {
    return {
      ok: false,
      error: "Could not delete the vehicle. Please try again.",
    };
  }
}
