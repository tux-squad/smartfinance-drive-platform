import type { PaymentSchedulePeriod } from "@/domain/entities/payment-schedule";
import type {
  PaymentScheduleRepository,
  PaymentScheduleSource,
} from "@/domain/repositories/payment-schedule-repository";
import { createSupabaseServerClient } from "@/infrastructure/services/supabase/server";

interface SimulationRow {
  id: string;
  currency: "PEN" | "USD";
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

function toPaymentSchedulePeriod(
  row: PaymentScheduleRow,
): PaymentSchedulePeriod {
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

const SCHEDULE_COLUMNS =
  "id, simulation_id, period_number, due_date, opening_balance, interest, principal, insurance, commission, total_payment, cash_flow, closing_balance";

export function createSupabasePaymentScheduleRepository(): PaymentScheduleRepository {
  /** Carga el cronograma de una simulación ya validada como del usuario. */
  async function loadSchedule(
    supabase: Awaited<ReturnType<typeof createSupabaseServerClient>>,
    simulation: SimulationRow,
  ): Promise<PaymentScheduleSource> {
    const { data, error } = await supabase
      .from("payment_schedules")
      .select(SCHEDULE_COLUMNS)
      .eq("simulation_id", simulation.id)
      .order("period_number", { ascending: true });

    if (error) {
      throw error;
    }

    return {
      currency: simulation.currency,
      periods: ((data ?? []) as PaymentScheduleRow[]).map(
        toPaymentSchedulePeriod,
      ),
    };
  }

  return {
    async getLatestForCurrentUser(): Promise<PaymentScheduleSource | null> {
      const supabase = await createSupabaseServerClient();
      const {
        data: { user },
      } = await supabase.auth.getUser();

      if (!user) {
        return null;
      }

      const { data: simulation, error: simulationError } = await supabase
        .from("simulations")
        .select("id, currency")
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

      return loadSchedule(supabase, simulation as SimulationRow);
    },

    async getForSimulation(
      simulationId: string,
    ): Promise<PaymentScheduleSource | null> {
      const supabase = await createSupabaseServerClient();
      const {
        data: { user },
      } = await supabase.auth.getUser();

      if (!user) {
        return null;
      }

      // El filtro por user_id (además de RLS) evita leer simulaciones ajenas.
      const { data: simulation, error: simulationError } = await supabase
        .from("simulations")
        .select("id, currency")
        .eq("id", simulationId)
        .eq("user_id", user.id)
        .maybeSingle();

      if (simulationError) {
        throw simulationError;
      }

      if (!simulation) {
        return null;
      }

      return loadSchedule(supabase, simulation as SimulationRow);
    },
  };
}