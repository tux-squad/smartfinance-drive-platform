/**
 * Codigos de error del flujo de auth. Los casos de uso devuelven uno de estos
 * codigos (no un texto), de modo que la UI pueda mostrar el mensaje en el
 * idioma activo. Vive en `shared` para no acoplar `application` a `presentation`.
 */
export type AuthFormErrorCode =
  | "invalid_email"
  | "password_required"
  | "invalid_credentials"
  | "sign_in_failed"
  | "password_too_short"
  | "passwords_mismatch"
  | "email_taken"
  | "sign_up_failed";

/**
 * Estado compartido entre los server actions de auth y las vistas que los
 * consumen con `useActionState`. Vive en `shared` para que ni `app` ni
 * `presentation` dependan una de la otra.
 */
export interface AuthFormState {
  errorCode: AuthFormErrorCode | null;
}

export type AuthFormAction = (
  prevState: AuthFormState,
  formData: FormData,
) => Promise<AuthFormState>;
