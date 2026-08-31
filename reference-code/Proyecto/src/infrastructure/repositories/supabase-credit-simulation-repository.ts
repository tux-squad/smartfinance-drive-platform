import type { SimulationSummary, SimulationStatus } from "@/domain/entities/simulation";
import type { VehicleCurrency } from "@/domain/entities/vehicle";
import type { CreditRepository, SaveSimulationData } from "@/domain/repositories/credit-repository";
import { createSupabaseServerClient } from "@/infrastructure/services/supabase/server";
import type { SupabaseClient } from "@supabase/supabase-js";

const VEHICLE_IMAGES_BUCKET = "vehicle-images";

interface VehicleJoin {
  price: number | string | null;
  image_path: string | null;
}

interface SupabaseSimulationRow {
  id: string;
  client_name: string;
  vehicle_label: string;
  currency: string;
  amount_financed: number | string;
  monthly_payment: number | string;
  tcea: number | string;
  tir: number | string;
  van: number | string;
  status: string;
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
  vehicles?: VehicleJoin | VehicleJoin[] | null;
}

// Columnas que se leen para armar un SimulationSummary completo.
const SIMULATION_SELECT = `
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
  vehicles ( price, image_path )
`;

function firstValue<T>(value: T | T[] | null | undefined): T | null {
  if (!value) {
    return null;
  }
  return Array.isArray(value) ? value[0] ?? null : value;
}

function createCreditSimulationSummary(
  row: SupabaseSimulationRow,
  imageUrl: string | null,
): SimulationSummary {
  const vehicle = firstValue(row.vehicles);
  return {
    id: row.id,
    clientName: row.client_name,
    vehicleLabel: row.vehicle_label,
    currency: row.currency as VehicleCurrency,
    amountFinanced: Number(row.amount_financed),
    monthlyPayment: Number(row.monthly_payment),
    tcea: Number(row.tcea),
    tir: Number(row.tir),
    van: Number(row.van),
    status: row.status as SimulationStatus,
    simulatedAt: row.simulated_at,
    downPayment: Number(row.down_payment),
    balloonFee: Number(row.balloon_fee),
    gracePeriod: row.grace_period_months,
    term: row.term_months,
    isCapital: row.is_capitalized,
    annualRate: Number(row.annual_rate),
    rateType: (row.rate_type === "nominal" ? "nominal" : "effective"),
    compounding: row.compounding,
    insuranceRate: Number(row.insurance_rate),
    vehicleId: row.vehicle_id,
    vehiclePrice: vehicle ? Number(vehicle.price ?? 0) : 0,
    vehicleImageUrl: imageUrl,
  };
}

export function createSupabaseCreditRepository(
  existingSupabaseClient?: SupabaseClient,
): CreditRepository {
  return {
    async create(data: SaveSimulationData) {
      const supabase = existingSupabaseClient ?? await createSupabaseServerClient();
      const {
        data: { user },
        error: authError,
      } = await supabase.auth.getUser();

      if (authError || !user) {
        throw new Error("Authentication required to save a simulation.");
      }

      const clientName =
        user.user_metadata?.full_name || user.email || "Client";

      // 1) Inserta la cabecera de la simulación con los indicadores YA calculados
      //    y los parámetros de entrada originales.
      const { data: insertedRow, error } = await supabase
        .from("simulations")
        .insert({
          user_id: user.id,
          vehicle_id: data.vehicleId,
          client_name: clientName,
          vehicle_label: data.vehicleLabel,
          currency: data.currency,
          down_payment: data.downPayment,
          balloon_fee: data.balloonFee,
          grace_period_months: data.gracePeriod,
          term_months: data.term,
          is_capitalized: data.isCapital,
          annual_rate: data.annualRate,
          rate_type: data.rateType,
          compounding: data.compounding,
          insurance_rate: data.insuranceRate,
          amount_financed: data.amountFinanced,
          monthly_payment: data.monthlyPayment,
          tcea: data.tcea,
          tir: data.tir,
          van: data.van,
          status: "pending",
        })
        .select(SIMULATION_SELECT)
        .single();

      if (error) {
        console.error("Supabase Database Error:", error.message);
        throw new Error(`Database error: ${error.message}`);
      }

      if (!insertedRow) {
        throw new Error("No data returned from the simulation creation.");
      }

      const inserted = insertedRow as unknown as SupabaseSimulationRow;

      // 2) Inserta el cronograma completo enlazado a la simulación.
      if (data.periods.length > 0) {
        const scheduleRows = data.periods.map((period) => ({
          simulation_id: inserted.id,
          period_number: period.periodNumber,
          due_date: period.dueDate,
          opening_balance: period.openingBalance,
          interest: period.interest,
          principal: period.principal,
          insurance: period.insurance,
          commission: period.commission,
          total_payment: period.totalPayment,
          cash_flow: period.cashFlow,
          closing_balance: period.closingBalance,
        }));

        const { error: scheduleError } = await supabase
          .from("payment_schedules")
          .insert(scheduleRows);

        if (scheduleError) {
          await supabase.from("simulations").delete().eq("id", inserted.id);
          console.error("Supabase Database Error:", scheduleError.message);
          throw new Error(`Database error: ${scheduleError.message}`);
        }
      }

      return createCreditSimulationSummary(
        inserted,
        resolveImageUrl(supabase, firstValue(inserted.vehicles)?.image_path ?? null),
      );
    },

    async listForCurrentUser(): Promise<SimulationSummary[]> {
      const supabase = existingSupabaseClient ?? await createSupabaseServerClient();
      const {
        data: { user },
        error: authError,
      } = await supabase.auth.getUser();

      if (authError || !user) {
        throw new Error("Authentication required to list simulations.");
      }

      const { data: rows, error } = await supabase
        .from("simulations")
        .select(SIMULATION_SELECT)
        .eq("user_id", user.id)
        .order("simulated_at", { ascending: false });

      if (error) throw new Error(error.message);
      if (!rows) return [];

      return (rows as unknown as SupabaseSimulationRow[]).map((row) =>
        createCreditSimulationSummary(
          row,
          resolveImageUrl(supabase, firstValue(row.vehicles)?.image_path ?? null),
        ),
      );
    },

    async delete(simulationId: string): Promise<void> {
      const supabase = existingSupabaseClient ?? await createSupabaseServerClient();
      const {
        data: { user },
        error: authError,
      } = await supabase.auth.getUser();

      if (authError || !user) {
        throw new Error("Authentication required to delete a simulation.");
      }

      // RLS ya restringe por dueño; el filtro user_id es defensa en profundidad.
      // El cronograma se elimina en cascada (FK on delete cascade).
      const { error } = await supabase
        .from("simulations")
        .delete()
        .eq("id", simulationId)
        .eq("user_id", user.id);

      if (error) throw new Error(error.message);
    },
  };
}

/** Construye la URL pública de la imagen del vehículo, si existe. */
function resolveImageUrl(
  supabase: SupabaseClient,
  imagePath: string | null,
): string | null {
  if (!imagePath) {
    return null;
  }
  return supabase.storage.from(VEHICLE_IMAGES_BUCKET).getPublicUrl(imagePath)
    .data.publicUrl;
}
