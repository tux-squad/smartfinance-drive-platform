export interface VehicleFormState {
  error: string | null;
  success: boolean;
  mode?: "created" | "updated";
}

export type VehicleFormAction = (
  prevState: VehicleFormState,
  formData: FormData,
) => Promise<VehicleFormState>;

export interface VehicleDeleteState {
  error: string | null;
  success: boolean;
}

export type VehicleDeleteAction = (
  prevState: VehicleDeleteState,
  formData: FormData,
) => Promise<VehicleDeleteState>;
