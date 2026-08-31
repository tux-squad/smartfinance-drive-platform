export interface QuarterlyCashFlow {
  label: string;
  value: number;
}

export interface RateComparison {
  label: string;
  value: number;
}

export interface FinancialEntityRateBenchmark {
  financialEntityName: string;
  annualRate: number;
}

export interface FinancialMetricsOverview {
  simulationId: string | null;
  financialEntityName: string | null;
  currency: "PEN" | "USD";
  van: number;
  tir: number;
  tcea: number;
  tea: number;
  tem: number;
  baselineRate: number;
  tirExcess: number;
  quarterlyCashFlows: QuarterlyCashFlow[];
  rateComparisons: RateComparison[];
  interpretation: string[];
}
