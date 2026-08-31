import type {
  SimulationStatus,
  SimulationSummary,
} from "@/domain/entities/simulation";
import type { SimulationRepository } from "@/domain/repositories/simulation-repository";
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
  status: SimulationStatus;
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
}

function toSimulationSummary(row: SimulationRow): SimulationSummary {
  return {
    id: row.id,
    clientName: row.client_name,
    vehicleLabel: row.vehicle_label,
    currency: row.currency,
    amountFinanced: Number(row.amount_financed),
    monthlyPayment: Number(row.monthly_payment),
    tcea: Number(row.tcea),
    tir: Number(row.tir),
    van: Number(row.van),
    status: row.status,
    simulatedAt: row.simulated_at,
    downPayment: Number(row.down_payment),
    balloonFee: Number(row.balloon_fee),
    gracePeriod: row.grace_period_months,
    term: row.term_months,
    isCapital: row.is_capitalized,
    annualRate: Number(row.annual_rate),
    rateType: row.rate_type === "nominal" ? "nominal" : "effective",
    compounding: row.compounding,
    insuranceRate: Number(row.insurance_rate),
    vehicleId: row.vehicle_id,
    vehiclePrice: 0,
    vehicleImageUrl: null,
  };
}

export function createSupabaseSimulationRepository(): SimulationRepository {
  return {
    async listForCurrentUser(): Promise<SimulationSummary[]> {
      const supabase = await createSupabaseServerClient();
      const {
        data: { user },
      } = await supabase.auth.getUser();
      if (!user) {
        return [];
      }

      const { data, error } = await supabase
        .from("simulations")
        .select(
          "id, client_name, vehicle_label, currency, amount_financed, monthly_payment, tcea, tir, van, status, simulated_at, down_payment, balloon_fee, grace_period_months, term_months, is_capitalized, annual_rate, rate_type, compounding, insurance_rate, vehicle_id",
        )
        .eq("user_id", user.id)
        .order("simulated_at", { ascending: false });

      if (error) {
        throw error;
      }

      return ((data ?? []) as SimulationRow[]).map(toSimulationSummary);
    },
  };
}
