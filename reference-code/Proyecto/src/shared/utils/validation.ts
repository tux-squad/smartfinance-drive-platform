/** Longitud minima de contrasena exigida (coincide con el default de Supabase). */
export const MIN_PASSWORD_LENGTH = 6;

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

export function isValidEmail(value: string): boolean {
  return EMAIL_PATTERN.test(value);
}

/** DNI peruano: exactamente 8 digitos. */
const DNI_PATTERN = /^\d{8}$/;

export function isValidDni(value: string): boolean {
  return DNI_PATTERN.test(value);
}
