import type { SimulationSummary } from "@/domain/entities/simulation";

/**
 * Port for summarized financing simulation results owned by the current user.
 */
export interface SimulationRepository {
  listForCurrentUser(): Promise<SimulationSummary[]>;
}
