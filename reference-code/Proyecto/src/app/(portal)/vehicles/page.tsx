import { listCurrentUserVehicles } from "@/application/use-cases/vehicles/list-current-user-vehicles";
import { listFinancialEntities } from "@/application/use-cases/vehicles/list-financial-entities";
import { createSupabaseVehicleRepository } from "@/infrastructure/repositories/supabase-vehicle-repository";
import { VehicleRegistrationView } from "@/presentation/views/vehicle-registration-view";

import { deleteVehicleAction, saveVehicleAction } from "./actions";
import { createSupabaseServerClient } from "@/infrastructure/services/supabase/server";

export default async function VehiclesPage() {
  const supabase = await createSupabaseServerClient();
  const repository = createSupabaseVehicleRepository(supabase);
  const [financialEntities, vehicles] = await Promise.all([
    listFinancialEntities(repository),
    listCurrentUserVehicles(repository),
  ]);

  return (
    <VehicleRegistrationView
      action={saveVehicleAction}
      deleteAction={deleteVehicleAction}
      financialEntities={financialEntities}
      vehicles={vehicles}
    />
  );
}
