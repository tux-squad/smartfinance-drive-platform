import type { User } from "@supabase/supabase-js";

import type { AuthUser } from "@/domain/entities/auth-user";
import type {
  AuthRepository,
  Credentials,
} from "@/domain/repositories/auth-repository";
import { createSupabaseServerClient } from "@/infrastructure/services/supabase/server";
import { AuthError, type AuthErrorCode } from "@/shared/errors/auth-error";

function toAuthUser(user: User): AuthUser {
  return { id: user.id, email: user.email ?? "" };
}

/** Traduce el codigo de error de Supabase a un codigo de dominio. */
function mapSignUpErrorCode(code: string | undefined): AuthErrorCode {
  if (code === "user_already_exists" || code === "email_exists") {
    return "email_taken";
  }
  if (code === "weak_password") {
    return "weak_password";
  }
  return "unknown";
}

/**
 * Implementacion del puerto `AuthRepository` sobre Supabase Auth.
 *
 * Usa el cliente de servidor, que lee/escribe la sesion en cookies via
 * `@supabase/ssr`. Por eso un sign-in o sign-up correcto deja al usuario
 * autenticado en la siguiente peticion.
 */
export function createSupabaseAuthRepository(): AuthRepository {
  return {
    async signInWithPassword({ email, password }: Credentials): Promise<AuthUser> {
      const supabase = await createSupabaseServerClient();
      const { data, error } = await supabase.auth.signInWithPassword({
        email,
        password,
      });

      if (error || !data.user) {
        throw new AuthError(
          "invalid_credentials",
          error?.message ?? "Invalid credentials",
        );
      }

      return toAuthUser(data.user);
    },

    async signUpWithPassword({ email, password }: Credentials): Promise<AuthUser> {
      const supabase = await createSupabaseServerClient();
      const { data, error } = await supabase.auth.signUp({ email, password });

      if (error || !data.user) {
        throw new AuthError(
          mapSignUpErrorCode(error?.code),
          error?.message ?? "Could not sign up",
        );
      }

      return toAuthUser(data.user);
    },

    async signOut(): Promise<void> {
      const supabase = await createSupabaseServerClient();
      await supabase.auth.signOut();
    },

    async getCurrentUser(): Promise<AuthUser | null> {
      const supabase = await createSupabaseServerClient();
      const {
        data: { user },
      } = await supabase.auth.getUser();

      return user ? toAuthUser(user) : null;
    },
  };
}
