import type { FinancialMetricsOverview } from "@/domain/entities/financial-metric";
import type { FinancialMetricRepository } from "@/domain/repositories/financial-metric-repository";
import { annualizeMonthlyRate } from "@/domain/services/credit-calculator";

const baselineMonthlyRate = 0.0125;

function groupCashFlowsByQuarter(
  periods: { dueDate: string; cashFlow: number }[],
) {
  const totals = [0, 0, 0, 0];

  periods.forEach((period) => {
    const month = new Date(period.dueDate).getMonth();
    const quarterIndex = Math.floor(month / 3);
    totals[quarterIndex] += Math.abs(period.cashFlow);
  });

  return totals.map((value, index) => ({
    label: `Q${index + 1}`,
    value,
  }));
}

export async function getFinancialMetricsOverview(
  repository: FinancialMetricRepository,
): Promise<FinancialMetricsOverview> {
  const source = await repository.getLatestForCurrentUser();

  if (!source) {
    return {
      simulationId: null,
      financialEntityName: null,
      currency: "USD",
      van: 0,
      tir: 0,
      tcea: 0,
      tea: 0,
      tem: 0,
      baselineRate: baselineMonthlyRate,
      tirExcess: 0,
      quarterlyCashFlows: [
        { label: "Q1", value: 0 },
        { label: "Q2", value: 0 },
        { label: "Q3", value: 0 },
        { label: "Q4", value: 0 },
      ],
      rateComparisons: [
        { label: "Autify", value: 0 },
      ],
      interpretation: [
        "No hay simulaciones registradas todavia. Los indicadores financieros se calcularan cuando exista una simulacion con cronograma de pagos.",
      ],
    };
  }

  const firstPeriod = source.periods[0];
  const monthlyTir = source.simulation.tir;

  const tem =
    firstPeriod && firstPeriod.openingBalance > 0
      ? firstPeriod.interest / firstPeriod.openingBalance
      : monthlyTir;

  const tea = annualizeMonthlyRate(tem);
  const tcea =
    source.simulation.tcea > 0
      ? source.simulation.tcea
      : annualizeMonthlyRate(monthlyTir);
  const tirExcess = monthlyTir - baselineMonthlyRate;
  const benchmarkRates = source.rateBenchmarks.map((benchmark) => ({
    label: benchmark.financialEntityName,
    value: benchmark.annualRate,
  }));

  return {
    simulationId: source.simulation.id,
    financialEntityName: source.financialEntityName,
    currency: source.simulation.currency,
    van: source.simulation.van,
    tir: monthlyTir,
    tcea,
    tea,
    tem,
    baselineRate: baselineMonthlyRate,
    tirExcess,
    quarterlyCashFlows: groupCashFlowsByQuarter(source.periods),
    rateComparisons: [
      ...benchmarkRates,
      { label: "Autify", value: tcea },
    ],
    interpretation: [
      `El VAN del deudor es ${
        source.simulation.van >= 0 ? "positivo" : "negativo"
      }, lo que indica ${
        source.simulation.van >= 0
          ? "que los flujos descontados compensan el monto inicial bajo la tasa COK utilizada."
          : "que el credito debe revisarse porque los flujos descontados no compensan el monto inicial bajo la tasa COK utilizada."
      }`,
      `La TIR mensual del flujo del deudor es ${(monthlyTir * 100).toFixed(
        2,
      )}%, ${
        tirExcess >= 0 ? "superando" : "quedando por debajo de"
      } la tasa base mensual por ${Math.abs(tirExcess * 100).toFixed(
        2,
      )} puntos porcentuales.`,
      `La TCEA anual es ${(tcea * 100).toFixed(
        2,
      )}% y se obtiene anualizando la TIR mensual del flujo. Este indicador representa el costo efectivo anual del credito, incluyendo intereses, seguros y comisiones.`,
      `La TEA es ${(tea * 100).toFixed(
        2,
      )}% y se calcula a partir de la TEM usada en el cronograma. La diferencia entre TEA y TCEA muestra el impacto de costos adicionales como seguros y comisiones.`,
    ],
  };
}
