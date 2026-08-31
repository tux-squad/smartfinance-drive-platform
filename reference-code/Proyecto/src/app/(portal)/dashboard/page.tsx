import { redirect } from "next/navigation";

import { getDashboardOverview } from "@/application/use-cases/dashboard/get-dashboard-overview";
import { getCurrentUser } from "@/application/use-cases/auth/get-current-user";
import { createSupabaseAuthRepository } from "@/infrastructure/repositories/supabase-auth-repository";
import { createSupabaseSimulationRepository } from "@/infrastructure/repositories/supabase-simulation-repository";
import { DashboardView } from "@/presentation/views/dashboard-view";

export default async function DashboardPage() {
  const user = await getCurrentUser(createSupabaseAuthRepository());

  if (!user) {
    redirect("/login");
  }

  const overview = await getDashboardOverview(createSupabaseSimulationRepository());

  return <DashboardView email={user.email} overview={overview} />;
}
