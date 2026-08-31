"use server";

import { revalidatePath } from "next/cache";

import { updateProfile } from "@/application/use-cases/profile/update-profile";
import { createSupabaseProfileRepository } from "@/infrastructure/repositories/supabase-profile-repository";
import type { SettingsFormState } from "@/shared/types/settings-form-state";

export async function updateProfileAction(
  _prevState: SettingsFormState,
  formData: FormData,
): Promise<SettingsFormState> {
  const result = await updateProfile(createSupabaseProfileRepository(), {
    nationalId: String(formData.get("nationalId") ?? ""),
    fullLegalNames: String(formData.get("fullLegalNames") ?? ""),
    dateOfBirth: String(formData.get("dateOfBirth") ?? ""),
    phoneCountryCode: String(formData.get("phoneCountryCode") ?? ""),
    mobilePhone: String(formData.get("mobilePhone") ?? ""),
    monthlyIncome: String(formData.get("monthlyIncome") ?? ""),
    employmentStatus: String(formData.get("employmentStatus") ?? ""),
  });

  if (!result.ok) {
    return { error: result.error, success: false };
  }

  revalidatePath("/settings");
  return { error: null, success: true };
}
