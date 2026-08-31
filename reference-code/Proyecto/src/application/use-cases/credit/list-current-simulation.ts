import { SimulationSummary } from "@/domain/entities/simulation";
import { SimulationRepository } from "@/domain/repositories/simulation-repository";

export async function listCurrentSimulations(
    repository:SimulationRepository,
):Promise<SimulationSummary[]> {
    return repository.listForCurrentUser();
}