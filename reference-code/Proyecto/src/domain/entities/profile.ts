/**
 * Perfil de cuenta del usuario autenticado (informacion editable en Settings).
 * Expresado en terminos de dominio, sin acoplarse a Supabase.
 */
export interface Profile {
  id: string;
  email: string;
  nationalId: string | null;
  fullLegalNames: string | null;
  /** Fecha en formato ISO `YYYY-MM-DD`. */
  dateOfBirth: string | null;
  phoneCountryCode: string | null;
  mobilePhone: string | null;
  monthlyIncome: number | null;
  employmentStatus: string | null;
}
