import type { DashboardOverview } from "@/domain/entities/simulation";
import type { SimulationRepository } from "@/domain/repositories/simulation-repository";

function average(values: number[]): number {
  if (values.length === 0) {
    return 0;
  }

  return values.reduce((total, value) => total + value, 0) / values.length;
}

export async function getDashboardOverview(
  repository: SimulationRepository,
): Promise<DashboardOverview> {
  const simulations = await repository.listForCurrentUser();

  return {
    totalFundedAmount: simulations.reduce(
      (total, simulation) => total + simulation.amountFinanced,
      0,
    ),
    averageMonthlyPayment: average(
      simulations.map((simulation) => simulation.monthlyPayment),
    ),
    averageTcea: average(simulations.map((simulation) => simulation.tcea)),
    portfolioVan: simulations.reduce(
      (total, simulation) => total + simulation.van,
      0,
    ),
    averageTir: average(simulations.map((simulation) => simulation.tir)),
    recentSimulations: simulations.slice(0, 5),
  };
}
