import type { Profile } from "@/domain/entities/profile";
import type {
  ProfileRepository,
  ProfileUpdate,
} from "@/domain/repositories/profile-repository";
import { createSupabaseServerClient } from "@/infrastructure/services/supabase/server";

interface ProfileRow {
  id: string;
  email: string;
  national_id: string | null;
  full_legal_names: string | null;
  date_of_birth: string | null;
  phone_country_code: string | null;
  mobile_phone: string | null;
  // numeric(12,2) puede llegar como string desde Postgres.
  monthly_income: number | string | null;
  employment_status: string | null;
}

function toProfile(row: ProfileRow): Profile {
  return {
    id: row.id,
    email: row.email,
    nationalId: row.national_id,
    fullLegalNames: row.full_legal_names,
    dateOfBirth: row.date_of_birth,
    phoneCountryCode: row.phone_country_code,
    mobilePhone: row.mobile_phone,
    monthlyIncome:
      row.monthly_income === null ? null : Number(row.monthly_income),
    employmentStatus: row.employment_status,
  };
}

/**
 * Implementacion del puerto `ProfileRepository` sobre Supabase.
 * Las politicas RLS garantizan que cada usuario solo accede a su fila.
 */
export function createSupabaseProfileRepository(): ProfileRepository {
  return {
    async getCurrent(): Promise<Profile | null> {
      const supabase = await createSupabaseServerClient();
      const {
        data: { user },
      } = await supabase.auth.getUser();
      if (!user) {
        return null;
      }

      const { data, error } = await supabase
        .from("profiles")
        .select("*")
        .eq("id", user.id)
        .maybeSingle<ProfileRow>();

      if (error) {
        throw error;
      }

      if (data) {
        return toProfile(data);
      }

      // Sin fila aun (p. ej. usuario previo al trigger): perfil base con email.
      return {
        id: user.id,
        email: user.email ?? "",
        nationalId: null,
        fullLegalNames: null,
        dateOfBirth: null,
        phoneCountryCode: "+51",
        mobilePhone: null,
        monthlyIncome: null,
        employmentStatus: null,
      };
    },

    async update(input: ProfileUpdate): Promise<Profile> {
      const supabase = await createSupabaseServerClient();
      const {
        data: { user },
      } = await supabase.auth.getUser();
      if (!user) {
        throw new Error("Not authenticated");
      }

      const { data, error } = await supabase
        .from("profiles")
        .upsert(
          {
            id: user.id,
            email: user.email ?? "",
            national_id: input.nationalId,
            full_legal_names: input.fullLegalNames,
            date_of_birth: input.dateOfBirth,
            phone_country_code: input.phoneCountryCode,
            mobile_phone: input.mobilePhone,
            monthly_income: input.monthlyIncome,
            employment_status: input.employmentStatus,
          },
          { onConflict: "id" },
        )
        .select("*")
        .single<ProfileRow>();

      if (error) {
        throw error;
      }

      return toProfile(data);
    },
  };
}
