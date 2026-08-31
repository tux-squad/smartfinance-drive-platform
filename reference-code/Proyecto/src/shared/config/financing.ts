/**
 * Parámetros financieros por defecto del producto "Compra Inteligente".
 *
 * Centralizados aquí para que un cambio de política (seguros, COK, capitalización)
 * se haga en un solo punto. Todas las tasas están en DECIMAL (0.0005 = 0.05 %).
 */

/** Capitalizaciones por año según la periodicidad de una tasa NOMINAL. */
export const COMPOUNDING_PER_YEAR = {
  monthly: 12,
  bimonthly: 6,
  quarterly: 4,
  semiannual: 2,
  annual: 1,
} as const;

export type CompoundingKey = keyof typeof COMPOUNDING_PER_YEAR;

/**
 * Seguro de desgravamen mensual por defecto, como fracción del saldo.
 * Referencia de mercado peruano: ~0.03 %–0.06 % mensual. Se usa 0.05 %.
 */
export const DEFAULT_MONTHLY_INSURANCE_RATE = 0.0005;

/** Comisión/portes fija por cuota (monto). Por defecto sin portes. */
export const DEFAULT_MONTHLY_COMMISSION = 0;

/**
 * Costo de Oportunidad del Capital (COK) MENSUAL usado para descontar el VAN.
 * Es la tasa mínima que el deudor exige; sirve de referencia para decidir si el
 * crédito conviene. Ajustable según la política del evaluador.
 */
export const DEFAULT_DISCOUNT_MONTHLY_RATE = 0.0125;
