"use server";

import { redirect } from "next/navigation";

import { signInWithPassword } from "@/application/use-cases/auth/sign-in-with-password";
import { createSupabaseAuthRepository } from "@/infrastructure/repositories/supabase-auth-repository";
import type { AuthFormState } from "@/shared/types/auth-form-state";

export async function loginAction(
  _prevState: AuthFormState,
  formData: FormData,
): Promise<AuthFormState> {
  const result = await signInWithPassword(createSupabaseAuthRepository(), {
    email: String(formData.get("email") ?? ""),
    password: String(formData.get("password") ?? ""),
  });

  if (!result.ok) {
    return { errorCode: result.error };
  }

  // Sesion ya escrita en cookies por el repositorio: navegamos al area privada.
  redirect("/dashboard");
}
