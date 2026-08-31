"use server";

import { redirect } from "next/navigation";

import { signOut } from "@/application/use-cases/auth/sign-out";
import { createSupabaseAuthRepository } from "@/infrastructure/repositories/supabase-auth-repository";

export async function logoutAction(): Promise<void> {
  await signOut(createSupabaseAuthRepository());
  redirect("/login");
}
