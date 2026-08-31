import { listCurrentSimulations } from "@/application/use-cases/credit/list-current-simulation";
import { createSupabaseCreditRepository } from "@/infrastructure/repositories/supabase-credit-simulation-repository";
import { SimulationRepositoryView } from "@/presentation/views/simulation-repository-view";

import { deleteSimulationAction } from "./actions";

export default async function SimulationPage() {
  const repository = createSupabaseCreditRepository();
  const simulations = await listCurrentSimulations(repository);

  return (
    <SimulationRepositoryView
      simulations={simulations}
      deleteAction={deleteSimulationAction}
    />
  );
}
