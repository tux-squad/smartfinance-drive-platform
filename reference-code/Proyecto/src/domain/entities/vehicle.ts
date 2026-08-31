export type VehicleCondition = "new" | "used";
export type VehicleCurrency = "PEN" | "USD";

/**
 * Vehicle registered by the current user for a financing simulation workflow.
 */
export interface Vehicle {
  id: string;
  financialEntityId: string;
  financialEntityName: string;
  brand: string;
  model: string;
  manufactureYear: number;
  condition: VehicleCondition;
  currency: VehicleCurrency;
  price: number;
  imageUrl: string | null;
  createdAt: string;
}
