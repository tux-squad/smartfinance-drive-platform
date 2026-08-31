"use server";

import { redirect } from "next/navigation";

import { signUpWithPassword } from "@/application/use-cases/auth/sign-up-with-password";
import { createSupabaseAuthRepository } from "@/infrastructure/repositories/supabase-auth-repository";
import type { AuthFormState } from "@/shared/types/auth-form-state";

export async function signupAction(
  _prevState: AuthFormState,
  formData: FormData,
): Promise<AuthFormState> {
  const result = await signUpWithPassword(createSupabaseAuthRepository(), {
    email: String(formData.get("email") ?? ""),
    password: String(formData.get("password") ?? ""),
    confirmPassword: String(formData.get("confirmPassword") ?? ""),
  });

  if (!result.ok) {
    return { errorCode: result.error };
  }

  // Con la confirmacion por email desactivada, el registro deja sesion activa.
  redirect("/dashboard");
}
