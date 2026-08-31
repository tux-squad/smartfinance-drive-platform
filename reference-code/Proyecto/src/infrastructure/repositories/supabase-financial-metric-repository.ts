import type { PaymentSchedulePeriod } from "@/domain/entities/payment-schedule";
import type {
  FinancialMetricRepository,
  FinancialMetricSource,
} from "@/domain/repositories/financial-metric-repository";
import { createSupabaseServerClient } from "@/infrastructure/services/supabase/server";

interface SimulationRow {
  id: string;
  client_name: string;
  vehicle_label: string;
  currency: "PEN" | "USD";
  amount_financed: number | string;
  monthly_payment: number | string;
  tcea: number | string;
  tir: number | string;
  van: number | string;
  status: "approved" | "pending" | "rejected";
  simulated_at: string;
  down_payment: number | string;
  balloon_fee: number | string;
  grace_period_months: number;
  term_months: number;
  is_capitalized: boolean;
  annual_rate: number | string;
  rate_type: string;
  compounding: number;
  insurance_rate: number | string;
  vehicle_id: string | null;
  vehicles:
    | {
        financial_entities:
          | {
              name: string;
            }
          | {
              name: string;
            }[]
          | null;
      }
    | {
        financial_entities:
          | {
              name: string;
            }
          | {
              name: string;
            }[]
          | null;
      }[]
    | null;
}

interface PaymentScheduleRow {
  id: string;
  simulation_id: string;
  period_number: number;
  due_date: string;
  opening_balance: number | string;
  interest: number | string;
  principal: number | string;
  insurance: number | string;
  commission: number | string;
  total_payment: number | string;
  cash_flow: number | string;
  closing_balance: number | string;
}

interface RateBenchmarkRow {
  annual_rate: number | string;
  financial_entities:
    | {
        name: string;
      }
    | {
        name: string;
      }[]
    | null;
}

function firstValue<T>(value: T | T[] | null | undefined): T | null {
  if (!value) {
    return null;
  }

  return Array.isArray(value) ? value[0] ?? null : value;
}

function toPeriod(row: PaymentScheduleRow): PaymentSchedulePeriod {
  return {
    id: row.id,
    simulationId: row.simulation_id,
    periodNumber: row.period_number,
    dueDate: row.due_date,
    openingBalance: Number(row.opening_balance),
    interest: Number(row.interest),
    principal: Number(row.principal),
    insurance: Number(row.insurance),
    commission: Number(row.commission),
    totalPayment: Number(row.total_payment),
    cashFlow: Number(row.cash_flow),
    closingBalance: Number(row.closing_balance),
  };
}

export function createSupabaseFinancialMetricRepository(): FinancialMetricRepository {
  return {
    async getLatestForCurrentUser(): Promise<FinancialMetricSource | null> {
      const supabase = await createSupabaseServerClient();
      const {
        data: { user },
      } = await supabase.auth.getUser();

      if (!user) {
        return null;
      }

      const { data: simulation, error: simulationError } = await supabase
        .from("simulations")
        .select(
          `
          id,
          client_name,
          vehicle_label,
          currency,
          amount_financed,
          monthly_payment,
          tcea,
          tir,
          van,
          status,
          simulated_at,
          down_payment,
          balloon_fee,
          grace_period_months,
          term_months,
          is_capitalized,
          annual_rate,
          rate_type,
          compounding,
          insurance_rate,
          vehicle_id,
          vehicles (
            financial_entities (
              name
            )
          )
          `,
        )
        .eq("user_id", user.id)
        .order("simulated_at", { ascending: false })
        .limit(1)
        .maybeSingle();

      if (simulationError) {
        throw simulationError;
      }

      if (!simulation) {
        return null;
      }

      const latestSimulation = simulation as SimulationRow;

      const [
        { data: scheduleData, error: scheduleError },
        { data: rateData, error: rateError },
      ] = await Promise.all([
        supabase
          .from("payment_schedules")
          .select(
            "id, simulation_id, period_number, due_date, opening_balance, interest, principal, insurance, commission, total_payment, cash_flow, closing_balance",
          )
          .eq("simulation_id", latestSimulation.id)
          .order("period_number", { ascending: true }),
        supabase
          .from("financial_entity_rate_benchmarks")
          .select(
            `
            annual_rate,
            financial_entities (
              name
            )
            `,
          )
          .eq("rate_type", "TCEA")
          .eq("currency", latestSimulation.currency)
          .order("annual_rate", { ascending: true }),
      ]);

      if (scheduleError) {
        throw scheduleError;
      }

      if (rateError) {
        throw rateError;
      }

      return {
        simulation: {
          id: latestSimulation.id,
          clientName: latestSimulation.client_name,
          vehicleLabel: latestSimulation.vehicle_label,
          currency: latestSimulation.currency,
          amountFinanced: Number(latestSimulation.amount_financed),
          monthlyPayment: Number(latestSimulation.monthly_payment),
          tcea: Number(latestSimulation.tcea),
          tir: Number(latestSimulation.tir),
          van: Number(latestSimulation.van),
          status: latestSimulation.status,
          simulatedAt: latestSimulation.simulated_at,
          downPayment: Number(latestSimulation.down_payment),
          balloonFee: Number(latestSimulation.balloon_fee),
          gracePeriod: latestSimulation.grace_period_months,
          term: latestSimulation.term_months,
          isCapital: latestSimulation.is_capitalized,
          annualRate: Number(latestSimulation.annual_rate),
          rateType:
            latestSimulation.rate_type === "nominal" ? "nominal" : "effective",
          compounding: latestSimulation.compounding,
          insuranceRate: Number(latestSimulation.insurance_rate),
          vehicleId: latestSimulation.vehicle_id,
          vehiclePrice: 0,
          vehicleImageUrl: null,
        },
        financialEntityName:
          firstValue(firstValue(latestSimulation.vehicles)?.financial_entities)
            ?.name ?? null,
        rateBenchmarks: ((rateData ?? []) as RateBenchmarkRow[])
          .filter((row) => firstValue(row.financial_entities)?.name)
          .map((row) => ({
            financialEntityName:
              firstValue(row.financial_entities)?.name ?? "Institution",
            annualRate: Number(row.annual_rate),
          })),
        periods: ((scheduleData ?? []) as PaymentScheduleRow[]).map(toPeriod),
      };
    },
  };
}
