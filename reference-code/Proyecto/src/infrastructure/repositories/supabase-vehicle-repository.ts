import type { FinancialEntity } from "@/domain/entities/financial-entity";
import type { Vehicle } from "@/domain/entities/vehicle";
import type {
  VehicleCreate,
  VehicleRepository,
  VehicleUpdate,
} from "@/domain/repositories/vehicle-repository";
import { createSupabaseServerClient } from "@/infrastructure/services/supabase/server";
import {
  VehicleBrandOption,
  VEHICLE_BRAND_IMAGE,
} from "@/shared/constants/vehicle";
import { SupabaseClient } from "@supabase/supabase-js";

interface FinancialEntityRow {
  id: string;
  name: string;
}

interface VehicleRow {
  id: string;
  financial_entity_id: string;
  financial_entities: { name: string } | { name: string }[] | null;
  brand: string;
  model: string;
  manufacture_year: number;
  condition: "new" | "used";
  currency: "PEN" | "USD";
  price: number | string;
  created_at: string;
}

function brandImageUrl(brand: string): string | null {
  return VEHICLE_BRAND_IMAGE[brand as VehicleBrandOption] ?? null;
}

function toVehicle(row: VehicleRow): Vehicle {
  const entity = Array.isArray(row.financial_entities)
    ? row.financial_entities[0]
    : row.financial_entities;

  return {
    id: row.id,
    financialEntityId: row.financial_entity_id,
    financialEntityName: entity?.name ?? "Unknown entity",
    brand: row.brand,
    model: row.model,
    manufactureYear: row.manufacture_year,
    condition: row.condition,
    currency: row.currency,
    price: Number(row.price),
    imageUrl: brandImageUrl(row.brand),
    createdAt: row.created_at,
  };
}

export function createSupabaseVehicleRepository(supabase: SupabaseClient<any, "public", "public", any, any>): VehicleRepository {
  return {
    async listFinancialEntities(): Promise<FinancialEntity[]> {
      const supabase = await createSupabaseServerClient();
      const { data, error } = await supabase
        .from("financial_entities")
        .select("id, name")
        .order("name", { ascending: true });

      if (error) {
        throw error;
      }

      return ((data ?? []) as FinancialEntityRow[]).map((row) => ({
        id: row.id,
        name: row.name,
      }));
    },

    async listCurrentUserVehicles(): Promise<Vehicle[]> {
      const supabase = await createSupabaseServerClient();
      const {
        data: { user },
      } = await supabase.auth.getUser();
      if (!user) {
        return [];
      }

      const { data, error } = await supabase
        .from("vehicles")
        .select(
          "id, financial_entity_id, brand, model, manufacture_year, condition, currency, price, created_at, financial_entities(name)",
        )
        .eq("user_id", user.id)
        .order("created_at", { ascending: false });

      if (error) {
        throw error;
      }

      return ((data ?? []) as unknown as VehicleRow[]).map((row) =>
        toVehicle(row),
      );
    },

    async create(input: VehicleCreate): Promise<Vehicle> {
      const supabase = await createSupabaseServerClient();
      const {
        data: { user },
      } = await supabase.auth.getUser();
      if (!user) {
        throw new Error("Not authenticated");
      }

      const { data, error } = await supabase
        .from("vehicles")
        .insert({
          user_id: user.id,
          financial_entity_id: input.financialEntityId,
          brand: input.brand,
          model: input.model,
          manufacture_year: input.manufactureYear,
          condition: input.condition,
          currency: input.currency,
          price: input.price,
        })
        .select(
          "id, financial_entity_id, brand, model, manufacture_year, condition, currency, price, created_at, financial_entities(name)",
        )
        .single();

      if (error) {
        throw error;
      }

      return toVehicle(data as VehicleRow);
    },

    async update(input: VehicleUpdate): Promise<Vehicle> {
      const supabase = await createSupabaseServerClient();
      const {
        data: { user },
      } = await supabase.auth.getUser();
      if (!user) {
        throw new Error("Not authenticated");
      }

      const { data, error } = await supabase
        .from("vehicles")
        .update({
          financial_entity_id: input.financialEntityId,
          brand: input.brand,
          model: input.model,
          manufacture_year: input.manufactureYear,
          condition: input.condition,
          currency: input.currency,
          price: input.price,
        })
        .eq("id", input.id)
        .eq("user_id", user.id)
        .select(
          "id, financial_entity_id, brand, model, manufacture_year, condition, currency, price, created_at, financial_entities(name)",
        )
        .single();

      if (error) {
        throw error;
      }

      return toVehicle(data as VehicleRow);
    },

    async delete(id: string): Promise<void> {
      const supabase = await createSupabaseServerClient();
      const {
        data: { user },
      } = await supabase.auth.getUser();
      if (!user) {
        throw new Error("Not authenticated");
      }

      const { error } = await supabase
        .from("vehicles")
        .delete()
        .eq("id", id)
        .eq("user_id", user.id);

      if (error) {
        throw error;
      }
    },
  };
}
