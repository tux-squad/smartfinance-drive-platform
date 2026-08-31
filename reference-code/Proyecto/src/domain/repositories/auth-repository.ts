import type { AuthUser } from "@/domain/entities/auth-user";

export interface Credentials {
  email: string;
  password: string;
}

/**
 * Puerto de autenticacion. El dominio define el contrato; la infraestructura
 * (Supabase) lo implementa y puede reemplazarse sin tocar casos de uso.
 *
 * Las implementaciones lanzan `AuthError` ante fallos esperables.
 */
export interface AuthRepository {
  signInWithPassword(credentials: Credentials): Promise<AuthUser>;
  signUpWithPassword(credentials: Credentials): Promise<AuthUser>;
  signOut(): Promise<void>;
  getCurrentUser(): Promise<AuthUser | null>;
}
