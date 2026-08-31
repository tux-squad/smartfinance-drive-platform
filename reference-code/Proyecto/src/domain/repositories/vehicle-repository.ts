import type { FinancialEntity } from "@/domain/entities/financial-entity";
import type {
  Vehicle,
  VehicleCondition,
  VehicleCurrency,
} from "@/domain/entities/vehicle";

export interface VehicleCreate {
  financialEntityId: string;
  brand: string;
  model: string;
  manufactureYear: number;
  condition: VehicleCondition;
  currency: VehicleCurrency;
  price: number;
}

export interface VehicleUpdate extends VehicleCreate {
  id: string;
}

/**
 * Port for user-owned vehicle records and the financing entity catalog.
 */
export interface VehicleRepository {
  listFinancialEntities(): Promise<FinancialEntity[]>;
  listCurrentUserVehicles(): Promise<Vehicle[]>;
  create(input: VehicleCreate): Promise<Vehicle>;
  update(input: VehicleUpdate): Promise<Vehicle>;
  delete(id: string): Promise<void>;
}
