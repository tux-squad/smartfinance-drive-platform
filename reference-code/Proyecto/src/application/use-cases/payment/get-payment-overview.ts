import type { PaymentScheduleOverview } from "@/domain/entities/payment-schedule";
import type { PaymentScheduleRepository } from "@/domain/repositories/payment-schedule-repository";

function sum(values: number[]): number {
  return values.reduce((total, value) => total + value, 0);
}

export async function getPaymentOverview(
  repository: PaymentScheduleRepository,
  simulationId?: string,
): Promise<PaymentScheduleOverview> {
  const source = simulationId
    ? await repository.getForSimulation(simulationId)
    : await repository.getLatestForCurrentUser();
  const periods = source?.periods ?? [];

  return {
    currency: source?.currency ?? "USD",
    totalLoanAmount: periods[0]?.openingBalance ?? 0,
    totalInterest: sum(periods.map((period) => period.interest)),
    totalInsurance: sum(periods.map((period) => period.insurance)),
    totalPaid: sum(periods.map((period) => period.totalPayment)),
    totalPrincipal: sum(periods.map((period) => period.principal)),
    periods,
  };
}