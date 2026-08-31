import { CreditRepository } from "@/domain/repositories/credit-repository";
import { Result } from "@/shared/types/result";

/**
 * Caso de uso: eliminar una simulación del usuario actual.
 * El cronograma asociado se borra en cascada a nivel de base de datos.
 */
export async function deleteSimulation(
  repository: CreditRepository,
  simulationId: string,
): Promise<Result<null>> {
  if (!simulationId) {
    return { ok: false, error: "A valid simulation id is required." };
  }

  try {
    await repository.delete(simulationId);
    return { ok: true, value: null };
  } catch {
    return {
      ok: false,
      error: "Could not delete the simulation. Please try again.",
    };
  }
}
