import type { PaymentSchedulePeriod } from "@/domain/entities/payment-schedule";
import type { SimulationSummary } from "@/domain/entities/simulation";
import type { FinancialEntityRateBenchmark } from "@/domain/entities/financial-metric";

export interface FinancialMetricSource {
  simulation: SimulationSummary;
  financialEntityName: string | null;
  rateBenchmarks: FinancialEntityRateBenchmark[];
  periods: PaymentSchedulePeriod[];
}

export interface FinancialMetricRepository {
  getLatestForCurrentUser(): Promise<FinancialMetricSource | null>;
}
