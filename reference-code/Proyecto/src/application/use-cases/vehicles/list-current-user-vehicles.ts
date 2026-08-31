import type { Vehicle } from "@/domain/entities/vehicle";
import type { VehicleRepository } from "@/domain/repositories/vehicle-repository";

export async function listCurrentUserVehicles(
  repository: VehicleRepository,
): Promise<Vehicle[]> {
  return repository.listCurrentUserVehicles();
}
