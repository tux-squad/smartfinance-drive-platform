"use server";

import { deleteSimulation } from "@/application/use-cases/credit/delete-simulation";
import { createSupabaseCreditRepository } from "@/infrastructure/repositories/supabase-credit-simulation-repository";
import { revalidatePath } from "next/cache";

export interface DeleteSimulationState {
  error: string | null;
  success: boolean;
}

export async function deleteSimulationAction(
  _prevState: DeleteSimulationState,
  formData: FormData,
): Promise<DeleteSimulationState> {
  const simulationId = String(formData.get("simulationId") ?? "");
  const result = await deleteSimulation(
    createSupabaseCreditRepository(),
    simulationId,
  );

  if (!result.ok) {
    return { error: result.error, success: false };
  }

  revalidatePath("/simulations");
  revalidatePath("/dashboard");
  revalidatePath("/payment");
  revalidatePath("/financial-metrics");
  return { error: null, success: true };
}
