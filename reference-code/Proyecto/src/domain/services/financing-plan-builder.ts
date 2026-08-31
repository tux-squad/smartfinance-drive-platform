/**
 * =============================================================================
 *  CONSTRUCTOR DEL PLAN DE PAGOS — Método Francés Vencido Ordinario
 * =============================================================================
 *
 * Este servicio de dominio arma el CRONOGRAMA COMPLETO de un crédito vehicular
 * "Compra Inteligente" y calcula sus indicadores de transparencia (cuota, VAN,
 * TIR y TCEA) desde el punto de vista del deudor.
 *
 * Reutiliza las fórmulas puras de `credit-calculator.ts` (fuente única de la
 * verdad). Es 100 % puro: no depende de UI ni de base de datos, por lo que puede
 * ejecutarse igual en el servidor (al guardar) que en el navegador (vista previa
 * en tiempo real). Si el profesor pide cambiar un supuesto, se ajusta aquí.
 *
 * Soporta lo que exige el enunciado:
 *   - Moneda indistinta (PEN/USD): las fórmulas no dependen de la moneda.
 *   - Tasa efectiva o nominal (con su capitalización) → se convierte a TEM.
 *   - Períodos de gracia TOTAL (se capitaliza el interés) o PARCIAL (se paga
 *     solo el interés).
 *   - Cuota balón (VFMG) diferida al final de la operación.
 *   - Seguro de desgravamen y comisiones/portes, incluidos en la TCEA.
 */

import {
  AnnualRateInput,
  calculateFrenchMonthlyPayment,
  calculateInternalRateOfReturn,
  calculateNetPresentValue,
  toEffectiveMonthlyRate,
  annualizeMonthlyRate,
} from "./credit-calculator";

/** Tipo de período de gracia al inicio de la operación. */
export type GraceType = "none" | "partial" | "total";

/** Parámetros de negocio para construir un plan de pagos. */
export interface FinancingPlanInput {
  /** Precio del vehículo (valor de venta). */
  vehiclePrice: number;
  /** Cuota inicial aportada por el cliente, en MONTO (no porcentaje). */
  downPayment: number;
  /** Cuota balón / VFMG diferida al final, en MONTO (no porcentaje). */
  balloonPayment: number;
  /** Plazo total del crédito en meses (incluye los meses de gracia). */
  termMonths: number;
  /** Meses de gracia al inicio (0 = sin gracia). */
  gracePeriodMonths: number;
  /** Tipo de gracia a aplicar durante `gracePeriodMonths`. */
  graceType: GraceType;
  /** Tasa anual configurada por el usuario (efectiva o nominal). */
  annualRate: AnnualRateInput;
  /** Seguro de desgravamen mensual como fracción del saldo (ej. 0.0005 = 0.05 %). */
  monthlyInsuranceRate: number;
  /** Comisión/portes fija cargada en cada cuota (monto). */
  monthlyCommission: number;
  /** Tasa de descuento mensual (COK) para el VAN, en decimal. */
  discountMonthlyRate: number;
  /** Fecha de inicio; la primera cuota vence un mes después. Por defecto: hoy. */
  startDate?: Date;
}

/** Una fila calculada del cronograma (aún sin ids de base de datos). */
export interface ComputedSchedulePeriod {
  periodNumber: number;
  dueDate: string; // ISO (yyyy-mm-dd)
  openingBalance: number;
  interest: number;
  principal: number;
  insurance: number;
  commission: number;
  totalPayment: number;
  cashFlow: number; // Óptica del deudor: pago = negativo.
  closingBalance: number;
}

/** Resultado completo: cronograma + indicadores financieros. */
export interface FinancingPlanResult {
  amountFinanced: number; // Capital desembolsado = precio − inicial.
  monthlyRate: number; // TEM aplicada.
  monthlyPayment: number; // Cuota representativa (primer mes de amortización).
  van: number; // Valor Actual Neto (deudor), descontado al COK.
  tir: number; // Tasa Interna de Retorno MENSUAL del flujo del deudor.
  tcea: number; // Tasa de Costo Efectivo Anual = anualización de la TIR.
  totalInterest: number;
  totalInsurance: number;
  totalCommission: number;
  totalPaid: number;
  totalPrincipal: number;
  periods: ComputedSchedulePeriod[];
}

/** Redondea a 2 decimales (céntimos), evitando arrastres de coma flotante. */
function round2(value: number): number {
  return Math.round((value + Number.EPSILON) * 100) / 100;
}

/** Suma la propiedad numérica indicada de todas las filas. */
function sumBy(
  periods: ComputedSchedulePeriod[],
  selector: (period: ComputedSchedulePeriod) => number,
): number {
  return round2(periods.reduce((total, period) => total + selector(period), 0));
}

/** Devuelve la fecha `base` desplazada `months` meses (día 1, formato ISO). */
function addMonthsIso(base: Date, months: number): string {
  const date = new Date(base.getFullYear(), base.getMonth() + months, 1);
  return date.toISOString().slice(0, 10);
}

/**
 * Construye el plan de pagos completo por el método francés vencido ordinario.
 *
 * Flujo del algoritmo (ver también el diagrama en docs/):
 *   1. Capital financiado P₀ = precio − cuota inicial.
 *   2. Convertir la tasa anual (efectiva/nominal) a TEM.
 *   3. Fase de GRACIA (g meses): total (capitaliza interés) o parcial (paga
 *      solo interés). No amortiza capital.
 *   4. Fase de AMORTIZACIÓN (n − g meses): cuota constante que deja al final un
 *      saldo igual a la cuota balón, la cual se paga en el último período.
 *   5. Con el flujo de caja resultante se calculan VAN, TIR y TCEA.
 *
 * @throws si el plazo no deja al menos un mes de amortización.
 */
export function buildFinancingPlan(
  input: FinancingPlanInput,
): FinancingPlanResult {
  const {
    vehiclePrice,
    downPayment,
    balloonPayment,
    termMonths,
    gracePeriodMonths,
    graceType,
    annualRate,
    monthlyInsuranceRate,
    monthlyCommission,
    discountMonthlyRate,
    startDate = new Date(),
  } = input;

  // (1) Capital que la entidad desembolsa y que se amortiza en el tiempo.
  const amountFinanced = round2(vehiclePrice - downPayment);

  // Los meses de gracia forman parte del plazo; el resto amortiza capital.
  const grace = graceType === "none" ? 0 : Math.max(0, gracePeriodMonths);
  const amortizingMonths = termMonths - grace;

  if (amortizingMonths <= 0) {
    throw new Error(
      "El plazo debe ser mayor que el período de gracia para poder amortizar.",
    );
  }

  // (2) Tasa efectiva mensual, común a toda la operación.
  const monthlyRate = toEffectiveMonthlyRate(annualRate);

  const periods: ComputedSchedulePeriod[] = [];
  let balance = amountFinanced;
  let periodNumber = 0;

  // (3) Fase de gracia.
  for (let i = 0; i < grace; i += 1) {
    periodNumber += 1;
    const opening = balance;
    const interest = round2(opening * monthlyRate);

    if (graceType === "total") {
      // Gracia TOTAL: no se paga nada; el interés se capitaliza al saldo.
      const closing = round2(opening + interest);
      balance = closing;
      periods.push({
        periodNumber,
        dueDate: addMonthsIso(startDate, periodNumber),
        openingBalance: opening,
        interest,
        principal: 0,
        insurance: 0,
        commission: 0,
        totalPayment: 0,
        cashFlow: 0,
        closingBalance: closing,
      });
    } else {
      // Gracia PARCIAL: se paga solo el interés (y seguros/portes); saldo intacto.
      const insurance = round2(opening * monthlyInsuranceRate);
      const commission = round2(monthlyCommission);
      const totalPayment = round2(interest + insurance + commission);
      periods.push({
        periodNumber,
        dueDate: addMonthsIso(startDate, periodNumber),
        openingBalance: opening,
        interest,
        principal: 0,
        insurance,
        commission,
        totalPayment,
        cashFlow: -totalPayment,
        closingBalance: opening,
      });
    }
  }

  // (4) Fase de amortización con método francés + cuota balón.
  // La cuota (parte capital+interés) se calcula sobre el saldo neto del valor
  // presente de la balón, de modo que al final quede justo la balón por pagar.
  const balloonPresentValue =
    balloonPayment > 0
      ? balloonPayment / (1 + monthlyRate) ** amortizingMonths
      : 0;
  const capitalInterestPayment = calculateFrenchMonthlyPayment(
    balance - balloonPresentValue,
    monthlyRate,
    amortizingMonths,
  );

  for (let i = 1; i <= amortizingMonths; i += 1) {
    periodNumber += 1;
    const opening = balance;
    const interest = round2(opening * monthlyRate);
    let principal = round2(capitalInterestPayment - interest);
    const insurance = round2(opening * monthlyInsuranceRate);
    const commission = round2(monthlyCommission);
    let closing = round2(opening - principal);

    if (i === amortizingMonths) {
      // Último período: se cancela todo el saldo remanente (la cuota balón),
      // garantizando saldo final 0 y consistencia al céntimo.
      principal = round2(principal + closing);
      closing = 0;
    }

    const totalPayment = round2(interest + principal + insurance + commission);
    periods.push({
      periodNumber,
      dueDate: addMonthsIso(startDate, periodNumber),
      openingBalance: opening,
      interest,
      principal,
      insurance,
      commission,
      totalPayment,
      cashFlow: -totalPayment,
      closingBalance: closing,
    });
    balance = closing;
  }

  // (5) Indicadores financieros desde la óptica del deudor.
  // Flujo para el VAN: cuotas pagadas (positivas) descontadas contra el capital
  // recibido. calculateNetPresentValue resta el `initialAmount`, así que se pasa
  // el capital recibido como monto inicial y los pagos como flujos.
  const paymentFlows = periods.map((period) => period.totalPayment);
  const van = round2(
    calculateNetPresentValue(amountFinanced, paymentFlows, discountMonthlyRate) *
      -1,
  );

  // Flujo para la TIR: +capital recibido en t0 y −cuotas en cada período.
  const irrCashFlows = [amountFinanced, ...periods.map((p) => -p.totalPayment)];
  let tir = 0;
  try {
    tir = calculateInternalRateOfReturn(irrCashFlows);
  } catch {
    // Si el flujo no tiene cambio de signo (caso degenerado), se aproxima con la TEM.
    tir = monthlyRate;
  }
  const tcea = annualizeMonthlyRate(tir);

  // Cuota representativa: primer período que efectivamente amortiza capital.
  const representative =
    periods.find((period) => period.principal > 0) ?? periods[0];

  return {
    amountFinanced,
    monthlyRate,
    monthlyPayment: representative ? representative.totalPayment : 0,
    van,
    tir: Math.max(0, tir), // La BD exige tir >= 0; el costo del deudor es positivo.
    tcea: Math.max(0, tcea),
    totalInterest: sumBy(periods, (p) => p.interest),
    totalInsurance: sumBy(periods, (p) => p.insurance),
    totalCommission: sumBy(periods, (p) => p.commission),
    totalPaid: sumBy(periods, (p) => p.totalPayment),
    totalPrincipal: sumBy(periods, (p) => p.principal),
    periods,
  };
}

/**
 * Deriva el {@link GraceType} a partir de los campos que captura la UI:
 * meses de gracia + checkbox "capitalizar" (`isCapital`).
 */
export function resolveGraceType(
  gracePeriodMonths: number,
  isCapital: boolean,
): GraceType {
  if (gracePeriodMonths <= 0) {
    return "none";
  }
  return isCapital ? "total" : "partial";
}
