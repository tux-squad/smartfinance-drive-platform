import type { AuthUser } from "@/domain/entities/auth-user";
import type { AuthRepository } from "@/domain/repositories/auth-repository";
import { AuthError } from "@/shared/errors/auth-error";
import type { AuthFormErrorCode } from "@/shared/types/auth-form-state";
import type { Result } from "@/shared/types/result";
import { isValidEmail, MIN_PASSWORD_LENGTH } from "@/shared/utils/validation";

export interface SignUpInput {
  email: string;
  password: string;
  confirmPassword: string;
}

export async function signUpWithPassword(
  repository: AuthRepository,
  input: SignUpInput,
): Promise<Result<AuthUser, AuthFormErrorCode>> {
  const email = input.email.trim().toLowerCase();

  if (!isValidEmail(email)) {
    return { ok: false, error: "invalid_email" };
  }

  if (input.password.length < MIN_PASSWORD_LENGTH) {
    return { ok: false, error: "password_too_short" };
  }

  if (input.password !== input.confirmPassword) {
    return { ok: false, error: "passwords_mismatch" };
  }

  try {
    const user = await repository.signUpWithPassword({
      email,
      password: input.password,
    });

    return { ok: true, value: user };
  } catch (error) {
    if (error instanceof AuthError && error.code === "email_taken") {
      return { ok: false, error: "email_taken" };
    }

    return { ok: false, error: "sign_up_failed" };
  }
}
