import { SimulationSummary } from "../entities/simulation";
import type { ComputedSchedulePeriod } from "../services/financing-plan-builder";
import type { VehicleCurrency } from "../entities/vehicle";

/**
 * Datos ya CALCULADOS listos para persistir. El caso de uso arma este objeto
 * tras construir el plan de pagos con el dominio; el repositorio solo escribe:
 * inserta la simulación y todas las filas de su cronograma.
 */
export interface SaveSimulationData {
  vehicleId: string;
  vehicleLabel: string;
  currency: VehicleCurrency;
  downPayment: number; // monto
  balloonFee: number; // monto
  gracePeriod: number;
  term: number;
  isCapital: boolean;
  annualRate: number;
  rateType: "effective" | "nominal";
  compounding: number;
  insuranceRate: number;
  amountFinanced: number;
  monthlyPayment: number;
  tcea: number;
  tir: number;
  van: number;
  /** Filas del cronograma a insertar en payment_schedules. */
  periods: ComputedSchedulePeriod[];
}

export interface CreditRepository {
  create(data: SaveSimulationData): Promise<SimulationSummary>;
  listForCurrentUser(): Promise<SimulationSummary[]>;
  /** Elimina una simulación del usuario actual (y su cronograma en cascada). */
  delete(simulationId: string): Promise<void>;
}
