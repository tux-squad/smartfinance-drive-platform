import type { AuthUser } from "@/domain/entities/auth-user";
import type { AuthRepository } from "@/domain/repositories/auth-repository";
import { AuthError } from "@/shared/errors/auth-error";
import type { AuthFormErrorCode } from "@/shared/types/auth-form-state";
import type { Result } from "@/shared/types/result";
import { isValidEmail } from "@/shared/utils/validation";

export interface SignInInput {
  email: string;
  password: string;
}

export async function signInWithPassword(
  repository: AuthRepository,
  input: SignInInput,
): Promise<Result<AuthUser, AuthFormErrorCode>> {
  const email = input.email.trim().toLowerCase();

  if (!isValidEmail(email)) {
    return { ok: false, error: "invalid_email" };
  }

  if (input.password.length === 0) {
    return { ok: false, error: "password_required" };
  }

  try {
    const user = await repository.signInWithPassword({
      email,
      password: input.password,
    });

    return { ok: true, value: user };
  } catch (error) {
    if (error instanceof AuthError && error.code === "invalid_credentials") {
      return { ok: false, error: "invalid_credentials" };
    }

    return { ok: false, error: "sign_in_failed" };
  }
}
