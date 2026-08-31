import type { VehicleCurrency } from "@/domain/entities/vehicle";

export type SimulationStatus = "approved" | "pending" | "rejected";

/**
 * Resumen de una simulación tal como se lista y se muestra en la app.
 *
 * Además de los indicadores calculados (van, tir, tcea, cuota), incluye los
 * PARÁMETROS DE ENTRADA con los que se creó y los datos del vehículo, para poder
 * mostrar en la vista expandible de Simulaciones "cómo se creó" cada escenario.
 */
export interface SimulationSummary {
  id: string;
  clientName: string;
  vehicleLabel: string;
  currency: VehicleCurrency;
  amountFinanced: number;
  monthlyPayment: number;
  tcea: number;
  tir: number;
  van: number;
  status: SimulationStatus;
  simulatedAt: string;

  // -- Parámetros de entrada originales --------------------------------------
  downPayment: number;
  balloonFee: number;
  gracePeriod: number;
  term: number;
  isCapital: boolean;
  annualRate: number; // porcentaje ingresado (ej. 14.00)
  rateType: "effective" | "nominal";
  compounding: number;
  insuranceRate: number; // fracción mensual del saldo (decimal)

  // -- Datos del vehículo (para la tarjeta expandible) -----------------------
  vehicleId: string | null;
  vehiclePrice: number;
  vehicleImageUrl: string | null;
}

export interface DashboardOverview {
  totalFundedAmount: number;
  averageMonthlyPayment: number;
  averageTcea: number;
  portfolioVan: number;
  averageTir: number;
  recentSimulations: SimulationSummary[];
}
