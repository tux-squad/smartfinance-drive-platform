import type { FinancialEntity } from "@/domain/entities/financial-entity";
import type { VehicleRepository } from "@/domain/repositories/vehicle-repository";

export async function listFinancialEntities(
  repository: VehicleRepository,
): Promise<FinancialEntity[]> {
  return repository.listFinancialEntities();
}
