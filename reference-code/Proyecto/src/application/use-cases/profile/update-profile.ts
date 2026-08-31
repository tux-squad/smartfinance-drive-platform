import type { Profile } from "@/domain/entities/profile";
import type {
  ProfileRepository,
  ProfileUpdate,
} from "@/domain/repositories/profile-repository";
import { EMPLOYMENT_STATUS_VALUES } from "@/shared/constants/employment-status";
import type { Result } from "@/shared/types/result";
import { isValidDni } from "@/shared/utils/validation";

export interface UpdateProfileInput {
  nationalId: string;
  fullLegalNames: string;
  dateOfBirth: string;
  phoneCountryCode: string;
  mobilePhone: string;
  monthlyIncome: string;
  employmentStatus: string;
}

function emptyToNull(value: string): string | null {
  const trimmed = value.trim();
  return trimmed === "" ? null : trimmed;
}

export async function updateProfile(
  repository: ProfileRepository,
  input: UpdateProfileInput,
): Promise<Result<Profile>> {
  const nationalId = emptyToNull(input.nationalId);
  if (nationalId !== null && !isValidDni(nationalId)) {
    return { ok: false, error: "National ID (DNI) must be 8 digits." };
  }

  const dateOfBirth = emptyToNull(input.dateOfBirth);
  if (dateOfBirth !== null) {
    const parsed = new Date(dateOfBirth);
    if (Number.isNaN(parsed.getTime())) {
      return { ok: false, error: "Date of birth is not a valid date." };
    }
    if (parsed.getTime() > Date.now()) {
      return { ok: false, error: "Date of birth cannot be in the future." };
    }
  }

  let monthlyIncome: number | null = null;
  const incomeRaw = input.monthlyIncome.trim();
  if (incomeRaw !== "") {
    const parsed = Number(incomeRaw);
    if (Number.isNaN(parsed) || parsed < 0) {
      return { ok: false, error: "Monthly income must be a positive number." };
    }
    monthlyIncome = parsed;
  }

  const employmentStatus = emptyToNull(input.employmentStatus);
  if (
    employmentStatus !== null &&
    !EMPLOYMENT_STATUS_VALUES.includes(employmentStatus)
  ) {
    return { ok: false, error: "Select a valid employment status." };
  }

  const update: ProfileUpdate = {
    nationalId,
    fullLegalNames: emptyToNull(input.fullLegalNames),
    dateOfBirth,
    phoneCountryCode: emptyToNull(input.phoneCountryCode) ?? "+51",
    mobilePhone: emptyToNull(input.mobilePhone),
    monthlyIncome,
    employmentStatus,
  };

  try {
    const profile = await repository.update(update);
    return { ok: true, value: profile };
  } catch {
    return {
      ok: false,
      error: "Could not save your changes. Please try again.",
    };
  }
}
