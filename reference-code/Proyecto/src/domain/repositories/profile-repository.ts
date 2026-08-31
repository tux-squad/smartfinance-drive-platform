import type { Profile } from "@/domain/entities/profile";

/** Campos editables del perfil (email queda atado a la cuenta de auth). */
export interface ProfileUpdate {
  nationalId: string | null;
  fullLegalNames: string | null;
  dateOfBirth: string | null;
  phoneCountryCode: string | null;
  mobilePhone: string | null;
  monthlyIncome: number | null;
  employmentStatus: string | null;
}

/**
 * Puerto para leer y guardar el perfil del usuario actual.
 * La implementacion resuelve el usuario desde la sesion (no se pasa el id).
 */
export interface ProfileRepository {
  getCurrent(): Promise<Profile | null>;
  update(input: ProfileUpdate): Promise<Profile>;
}
