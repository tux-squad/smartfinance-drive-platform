import type {
  PaymentSchedulePeriod,
} from "@/domain/entities/payment-schedule";

export interface PaymentScheduleSource {
  currency: "PEN" | "USD";
  periods: PaymentSchedulePeriod[];
}

export interface PaymentScheduleRepository {
  getLatestForCurrentUser(): Promise<PaymentScheduleSource | null>;
  /** Cronograma de una simulación específica del usuario actual. */
  getForSimulation(simulationId: string): Promise<PaymentScheduleSource | null>;
}