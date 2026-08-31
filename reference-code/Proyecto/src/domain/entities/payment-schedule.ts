/**
 * DATOS INTERMEDIOS/SALIDA — Una fila del cronograma de amortización francés.
 *
 * Cada objeto representa un período (mes) del plan de pagos. La relación entre
 * columnas para el método francés vencido es:
 *
 *   interest      = openingBalance × TEM
 *   principal     = totalPayment − interest        (amortización del período)
 *   closingBalance= openingBalance − principal
 *   totalPayment  = interest + principal + insurance + commission
 *   cashFlow      = flujo desde la óptica del deudor (negativo = pago)
 */
export interface PaymentSchedulePeriod {
  id: string;
  /** FK a la simulación dueña de este cronograma. */
  simulationId: string;
  /** Número de período/cuota, empezando en 1. */
  periodNumber: number;
  /** Fecha de vencimiento de la cuota (ISO). */
  dueDate: string;
  /** Saldo de capital al inicio del período. */
  openingBalance: number;
  /** Interés del período = openingBalance × TEM. */
  interest: number;
  /** Amortización de capital del período. */
  principal: number;
  /** Seguro (desgravamen / vehicular) cargado en la cuota. */
  insurance: number;
  /** Comisiones/portes cargados en la cuota. */
  commission: number;
  /** Cuota total pagada en el período (interés + principal + seguro + comisión). */
  totalPayment: number;
  /** Flujo de caja del período usado para VAN/TIR (deudor: pago = negativo). */
  cashFlow: number;
  /** Saldo de capital al cierre del período. */
  closingBalance: number;
}

/**
 * DATOS DE SALIDA — Resumen agregado del cronograma completo, listo para la UI.
 * Los totales permiten mostrar el costo total del crédito de un vistazo.
 */
export interface PaymentScheduleOverview {
  currency: "PEN" | "USD";
  /** Capital financiado total (monto del préstamo). */
  totalLoanAmount: number;
  /** Suma de todos los intereses pagados a lo largo del crédito. */
  totalInterest: number;
  /** Suma de todos los seguros pagados. */
  totalInsurance: number;
  /** Total desembolsado por el cliente (suma de todas las cuotas). */
  totalPaid: number;
  /** Suma de todas las amortizaciones de capital (≈ totalLoanAmount). */
  totalPrincipal: number;
  /** Detalle período por período. */
  periods: PaymentSchedulePeriod[];
}
