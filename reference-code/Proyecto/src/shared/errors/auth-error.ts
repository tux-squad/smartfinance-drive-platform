export type AuthErrorCode =
  | "invalid_credentials"
  | "email_taken"
  | "weak_password"
  | "unknown";

/**
 * Error de dominio para el flujo de autenticacion. La infraestructura traduce
 * los errores de Supabase a uno de estos codigos y los casos de uso los mapean
 * a mensajes para la UI.
 */
export class AuthError extends Error {
  constructor(
    public readonly code: AuthErrorCode,
    message: string,
  ) {
    super(message);
    this.name = "AuthError";
  }
}
