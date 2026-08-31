/**
 * =============================================================================
 *  MOTOR FINANCIERO CENTRAL — Método Francés Vencido Ordinario (Compra Inteligente)
 * =============================================================================
 *
 * Este archivo concentra TODAS las fórmulas financieras del sistema en un solo
 * lugar (capa de dominio, sin dependencias de UI ni de base de datos). Es la
 * "fuente única de la verdad" para los cálculos: si el profesor pide cambiar una
 * fórmula, un supuesto o una convención, este es el único archivo que se toca.
 *
 * Convenciones adoptadas (según el enunciado del trabajo final):
 *   - Método de amortización: FRANCÉS VENCIDO ORDINARIO (cuota constante,
 *     pagos al final de cada período).
 *   - Meses de 30 días → 12 períodos por año. La conversión TEA↔TEM usa 12.
 *   - Funciona indistintamente para Soles (PEN) o Dólares (USD): las fórmulas
 *     son independientes de la moneda; la moneda solo afecta el formato de salida.
 *   - Admite tasas EFECTIVAS o NOMINALES (con su capitalización).
 *
 * Todos los importes están expresados en la unidad monetaria de la simulación.
 * Todas las tasas se manejan en forma DECIMAL (ej. 0.12 = 12 %), no en porcentaje.
 */

/** Tipo de tasa de interés que el usuario configura para la operación. */
export type RateType = "effective" | "nominal";

/**
 * Entrada normalizada para describir una tasa anual, sea efectiva o nominal.
 *
 * @property type    "effective" (TEA) o "nominal" (TNA).
 * @property annualRate  Tasa anual en decimal (ej. 0.145 para 14.5 %).
 * @property compoundingPeriodsPerYear  Solo relevante si type === "nominal".
 *           Número de capitalizaciones por año (12 = mensual, 4 = trimestral,
 *           2 = semestral, 1 = anual). Por defecto 12 (capitalización mensual).
 */
export interface AnnualRateInput {
  type: RateType;
  annualRate: number;
  compoundingPeriodsPerYear?: number;
}

/**
 * Calcula el CAPITAL A FINANCIAR bajo el esquema "Compra Inteligente".
 *
 * Fórmula:  Capital = Precio − Cuota inicial − Cuota balón (VFMG)
 *
 * La cuota inicial (down payment) la aporta el cliente al inicio y la cuota
 * balón (o "cuota final" / valor futuro mínimo garantizado) se difiere al final,
 * por lo que ninguna de las dos forma parte del capital que se amortiza mes a mes.
 *
 * NOTA financiera: esta es una simplificación. En un modelo estricto de Compra
 * Inteligente, los intereses de la cuota balón siguen devengándose durante toda
 * la operación (la balón se descuenta a valor presente, no se resta directa del
 * capital). Si el profesor exige el tratamiento riguroso de la balón, este es el
 * punto a ajustar. Ver también `calculateFrenchMonthlyPayment`.
 *
 * @param vehiclePrice   Precio de venta del vehículo.
 * @param downPayment    Cuota inicial aportada por el cliente.
 * @param balloonPayment Cuota balón / cuota final diferida.
 * @returns Monto sobre el que se construye el plan de pagos francés.
 */
export function calculateSmartPurchasePrincipal(
  vehiclePrice: number,
  downPayment: number,
  balloonPayment: number,
): number {
  return vehiclePrice - downPayment - balloonPayment;
}

/**
 * Convierte cualquier tasa anual (efectiva o nominal) a TASA EFECTIVA ANUAL (TEA).
 *
 * - Si ya es efectiva, se devuelve tal cual.
 * - Si es nominal (TNA con m capitalizaciones/año):
 *
 *        TEA = (1 + TNA / m) ^ m − 1
 *
 * @param input Descripción de la tasa (ver {@link AnnualRateInput}).
 * @returns TEA en decimal.
 */
export function toEffectiveAnnualRate(input: AnnualRateInput): number {
  if (input.type === "effective") {
    return input.annualRate;
  }

  // Capitalización mensual por defecto (12 períodos), coherente con meses de 30 días.
  const periods = input.compoundingPeriodsPerYear ?? 12;
  return (1 + input.annualRate / periods) ** periods - 1;
}

/**
 * Convierte una tasa anual a TASA EFECTIVA MENSUAL (TEM), que es la tasa que
 * realmente se aplica período a período en el cronograma francés.
 *
 *        TEM = (1 + TEA) ^ (1/12) − 1
 *
 * Se usa exponente 1/12 porque el enunciado fija meses de 30 días → 12 períodos/año.
 *
 * @param input Descripción de la tasa (efectiva o nominal).
 * @returns TEM en decimal.
 */
export function toEffectiveMonthlyRate(input: AnnualRateInput): number {
  const tea = toEffectiveAnnualRate(input);
  return (1 + tea) ** (1 / 12) - 1;
}

/**
 * Calcula la CUOTA MENSUAL CONSTANTE del método francés vencido ordinario.
 *
 * Fórmula de la anualidad (cuota fija):
 *
 *              P · i · (1 + i)^n
 *      A =  ───────────────────────
 *               (1 + i)^n − 1
 *
 *   donde   P = capital financiado
 *           i = tasa efectiva del período (TEM en decimal)
 *           n = número de períodos (meses)
 *
 * Casos borde manejados:
 *   - periods <= 0  → error (no existe un plan de pagos sin períodos).
 *   - i === 0       → cuota = P / n (sin interés, amortización lineal).
 *
 * IMPORTANTE — Períodos de gracia: el enunciado exige soportar gracia TOTAL y
 * PARCIAL. Esta función calcula la cuota "pura" sin gracia. El tratamiento de la
 * gracia (total: no se paga nada y el interés se capitaliza; parcial: solo se
 * paga el interés) debe aplicarse ANTES de llamar aquí, ajustando el capital P
 * y/o el número de períodos n, o en la rutina que arma el cronograma completo.
 * Ver el reporte de análisis en docs/ANALISIS-CUMPLIMIENTO.MD (hallazgo C4).
 *
 * @param amountFinanced Capital financiado P.
 * @param monthlyRate    Tasa efectiva mensual i (decimal).
 * @param periods        Número de cuotas n.
 * @returns Valor de la cuota mensual constante.
 */
export function calculateFrenchMonthlyPayment(
  amountFinanced: number,
  monthlyRate: number,
  periods: number,
): number {
  if (periods <= 0) {
    throw new Error("Periods must be greater than zero.");
  }

  // Sin interés: la cuota es simplemente el capital repartido en n períodos.
  if (monthlyRate === 0) {
    return amountFinanced / periods;
  }

  const compoundFactor = (1 + monthlyRate) ** periods; // (1 + i)^n
  return (
    (amountFinanced * monthlyRate * compoundFactor) / (compoundFactor - 1)
  );
}

/**
 * Calcula el VALOR ACTUAL NETO (VAN / NPV) de un flujo de caja.
 *
 *      VAN = −I₀ + Σ  Fₜ / (1 + r)^t      (t = 1..n)
 *
 * Convención de esta función:
 *   - `initialAmount` (I₀) es el desembolso inicial en el período 0 y se resta.
 *   - `cashFlows[k]` es el flujo del período (k + 1); es decir, el primer
 *     elemento del arreglo corresponde a t = 1, NO a t = 0.
 *   - `monthlyDiscountRate` (r) es la tasa de descuento del período (COK mensual).
 *
 * Desde el punto de vista del DEUDOR (como pide el enunciado), I₀ es el dinero
 * que recibe (capital financiado) y los Fₜ son las cuotas que paga (negativas).
 *
 * @param initialAmount        Desembolso/monto inicial en t = 0.
 * @param cashFlows            Flujos desde t = 1 en adelante.
 * @param monthlyDiscountRate  Tasa de descuento por período (decimal).
 * @returns VAN en unidades monetarias.
 */
export function calculateNetPresentValue(
  initialAmount: number,
  cashFlows: number[],
  monthlyDiscountRate: number,
): number {
  return cashFlows.reduce(
    (total, flow, index) =>
      total + flow / (1 + monthlyDiscountRate) ** (index + 1),
    -initialAmount,
  );
}

/**
 * Calcula la TASA INTERNA DE RETORNO (TIR / IRR) mensual de un flujo de caja,
 * resolviendo por BISECCIÓN la tasa r que hace VAN(r) = 0.
 *
 *      Σ  Fₜ / (1 + r)^t = 0      (t = 0..n)
 *
 * Convención de esta función (DISTINTA a la de calculateNetPresentValue):
 *   - `cashFlows[0]` es el flujo del período 0 (normalmente el desembolso
 *     inicial, con signo negativo desde la óptica del deudor).
 *   - `cashFlows[k]` es el flujo del período k.
 *   El arreglo YA debe incluir el flujo inicial como primer elemento; aquí NO
 *   se separa un `initialAmount` aparte.
 *
 * Requisito: el flujo debe cambiar de signo (tener al menos un ingreso y un
 * egreso), de lo contrario no existe una TIR real y se lanza un error.
 *
 * Método numérico:
 *   - Busca la raíz en el intervalo [-0.9999, 10] (mensual).
 *   - Hasta 100 iteraciones o tolerancia 1e-6.
 * La TIR resultante es MENSUAL; para anualizarla usar {@link annualizeMonthlyRate}.
 *
 * @param cashFlows Flujo completo incluyendo el período 0.
 * @returns TIR mensual en decimal.
 */
export function calculateInternalRateOfReturn(cashFlows: number[]): number {
  let low = -0.9999; // Tasa mínima evaluable (evita división por cero en (1+r)).
  let high = 10; // Tasa máxima evaluable (1000 % mensual como cota superior).

  // VAN del flujo para una tasa dada (t empieza en 0 → primer elemento es t=0).
  const valueAt = (rate: number) =>
    cashFlows.reduce((total, flow, index) => total + flow / (1 + rate) ** index, 0);

  const lowValue = valueAt(low);
  const highValue = valueAt(high);

  // Si VAN(low) y VAN(high) tienen el mismo signo, no hay raíz en el intervalo.
  if (Math.sign(lowValue) === Math.sign(highValue)) {
    throw new Error("Cash flows do not bracket an internal rate of return.");
  }

  // Bisección: se estrecha el intervalo conservando el sub-intervalo con cambio de signo.
  for (let iteration = 0; iteration < 100; iteration += 1) {
    const mid = (low + high) / 2;
    const midValue = valueAt(mid);

    if (Math.abs(midValue) < 0.000001) {
      return mid; // Convergió dentro de la tolerancia.
    }

    if (Math.sign(midValue) === Math.sign(lowValue)) {
      low = mid;
    } else {
      high = mid;
    }
  }

  return (low + high) / 2; // Mejor aproximación tras agotar las iteraciones.
}

/**
 * Anualiza una tasa efectiva MENSUAL para obtener la efectiva ANUAL.
 *
 *      TEA = (1 + TEM) ^ 12 − 1
 *
 * Se usa, por ejemplo, para convertir la TIR mensual del cronograma en TCEA/TEA
 * anual mostrada en el panel de indicadores.
 *
 * @param monthlyRate Tasa efectiva mensual (decimal).
 * @returns Tasa efectiva anual (decimal).
 */
export function annualizeMonthlyRate(monthlyRate: number): number {
  return (1 + monthlyRate) ** 12 - 1;
}
