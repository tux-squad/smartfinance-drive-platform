import { CreditSimulationInput } from "@/domain/entities/credit-entity";
import { SimulationSummary } from "@/domain/entities/simulation";
import { CreditRepository } from "@/domain/repositories/credit-repository";
import {
  buildFinancingPlan,
  resolveGraceType,
} from "@/domain/services/financing-plan-builder";
import { DEFAULT_DISCOUNT_MONTHLY_RATE } from "@/shared/config/financing";
import { Result } from "@/shared/types/result";

/**
 * Caso de uso: crear (calcular y persistir) una simulación de crédito vehicular.
 *
 * Orquesta el flujo completo:
 *   1. Valida los datos de entrada.
 *   2. Construye el CRONOGRAMA DE PAGOS y calcula cuota, VAN, TIR y TCEA reales
 *      con el servicio de dominio `buildFinancingPlan` (fórmulas centralizadas).
 *   3. Delega en el repositorio la escritura de la simulación y su cronograma.
 *
 * Devuelve un `Result` (ok/error) para que la presentación muestre mensajes sin
 * manejar excepciones.
 */
export async function createCreditSimulation(
  repository: CreditRepository,
  input: CreditSimulationInput,
): Promise<Result<SimulationSummary>> {
  // -- Validaciones de entrada -------------------------------------------------
  if (!input.vehicleId) {
    return { ok: false, error: "A valid vehicle must be selected." };
  }
  if (!(input.vehiclePrice > 0)) {
    return { ok: false, error: "The selected vehicle has no valid price." };
  }
  if (input.downPayment < 0) {
    return { ok: false, error: "Down payment can not be less than 0." };
  }
  if (input.balloonFee < 0) {
    return { ok: false, error: "Balloon fee can not be less than 0." };
  }
  if (input.downPayment + input.balloonFee >= input.vehiclePrice) {
    return {
      ok: false,
      error: "Down payment plus balloon must be less than the vehicle price.",
    };
  }
  if (input.gracePeriod < 0) {
    return { ok: false, error: "Grace period can not be less than 0." };
  }
  if (input.term <= 0) {
    return { ok: false, error: "The term must be a valid amount of months." };
  }
  if (input.gracePeriod >= input.term) {
    return {
      ok: false,
      error: "The grace period must be shorter than the total term.",
    };
  }
  if (!(input.annualRate > 0)) {
    return { ok: false, error: "The interest rate must be greater than 0." };
  }

  // -- Construcción del plan de pagos (fórmulas de dominio) --------------------
  let plan;
  try {
    plan = buildFinancingPlan({
      vehiclePrice: input.vehiclePrice,
      downPayment: input.downPayment,
      balloonPayment: input.balloonFee,
      termMonths: input.term,
      gracePeriodMonths: input.gracePeriod,
      graceType: resolveGraceType(input.gracePeriod, input.isCapital),
      annualRate: {
        type: input.rateType,
        annualRate: input.annualRate / 100, // porcentaje → decimal
        compoundingPeriodsPerYear: input.compoundingPeriodsPerYear,
      },
      monthlyInsuranceRate: input.monthlyInsuranceRate,
      monthlyCommission: input.monthlyCommission,
      discountMonthlyRate: DEFAULT_DISCOUNT_MONTHLY_RATE,
    });
  } catch (error) {
    return {
      ok: false,
      error:
        error instanceof Error
          ? error.message
          : "Could not build the payment plan.",
    };
  }

  // -- Persistencia ------------------------------------------------------------
  try {
    const simulation = await repository.create({
      vehicleId: input.vehicleId,
      vehicleLabel: input.vehicleLabel,
      currency: input.currency,
      downPayment: input.downPayment,
      balloonFee: input.balloonFee,
      gracePeriod: input.gracePeriod,
      term: input.term,
      isCapital: input.isCapital,
      annualRate: input.annualRate,
      rateType: input.rateType,
      compounding: input.compoundingPeriodsPerYear,
      insuranceRate: input.monthlyInsuranceRate,
      amountFinanced: plan.amountFinanced,
      monthlyPayment: plan.monthlyPayment,
      tcea: plan.tcea,
      tir: plan.tir,
      van: plan.van,
      periods: plan.periods,
    });

    return { ok: true, value: simulation };
  } catch {
    return {
      ok: false,
      error: "Could not save the simulation. Please try again.",
    };
  }
}
