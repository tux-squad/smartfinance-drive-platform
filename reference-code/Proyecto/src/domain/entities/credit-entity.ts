import type { VehicleCurrency } from "./vehicle";

/**
 * DATOS DE ENTRADA de una simulación de crédito vehicular.
 *
 * Representa lo que se necesita para GENERAR el plan de pagos. Los montos ya
 * vienen resueltos (no porcentajes): la capa que arma este objeto usa el precio
 * autoritativo del vehículo (leído en el servidor) para convertir los
 * porcentajes de la UI a montos. Viaja: Server Action → Caso de uso → Dominio.
 */
export interface CreditSimulationInput {
  /** Identificador del vehículo seleccionado (FK a la tabla vehicles). */
  vehicleId: string;
  /** Precio del vehículo, tomado del registro autoritativo en BD. */
  vehiclePrice: number;
  /** Moneda de la operación (heredada del vehículo). */
  currency: VehicleCurrency;
  /** Etiqueta legible "Marca Modelo" para mostrar en listados. */
  vehicleLabel: string;
  /** Cuota inicial en MONTO. */
  downPayment: number;
  /** Cuota balón / cuota final diferida, en MONTO. */
  balloonFee: number;
  /** Meses de gracia al inicio de la operación. 0 = sin gracia. */
  gracePeriod: number;
  /** Plazo del crédito en número de meses (incluye la gracia). */
  term: number;
  /** Tipo de tasa ingresada: efectiva (TEA) o nominal (TNA). */
  rateType: "effective" | "nominal";
  /** Valor de la tasa anual en porcentaje, tal como la ingresa el usuario. */
  annualRate: number;
  /** Capitalizaciones por año (solo aplica si rateType === "nominal"). */
  compoundingPeriodsPerYear: number;
  /** Seguro de desgravamen mensual como fracción del saldo (decimal). */
  monthlyInsuranceRate: number;
  /** Comisión/portes fija por cuota (monto). */
  monthlyCommission: number;
  /** true = gracia TOTAL (capitaliza interés); false = gracia PARCIAL. */
  isCapital: boolean;
}
