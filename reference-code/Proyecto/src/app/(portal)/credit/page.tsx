import { listCurrentUserVehicles } from "@/application/use-cases/vehicles/list-current-user-vehicles";
import { createSupabaseVehicleRepository } from "@/infrastructure/repositories/supabase-vehicle-repository";
import { createSupabaseCreditRepository } from "@/infrastructure/repositories/supabase-credit-simulation-repository";
import { createSupabaseServerClient } from "@/infrastructure/services/supabase/server";
import { CreditConfigurationView } from "@/presentation/views/credit-configuration-view";
import { createSimulationAction } from "./actions";

export default async function CreditPage() {
  const supabase = await createSupabaseServerClient();

  const vehicleRepository = createSupabaseVehicleRepository(supabase);

  const vehicles = await listCurrentUserVehicles(vehicleRepository);

  return (
    <CreditConfigurationView 
      action={createSimulationAction}
      vehicles={vehicles}
    />
  );
}
