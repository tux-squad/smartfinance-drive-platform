import type { AuthUser } from "@/domain/entities/auth-user";
import type { AuthRepository } from "@/domain/repositories/auth-repository";

export async function getCurrentUser(
  repository: AuthRepository,
): Promise<AuthUser | null> {
  return repository.getCurrentUser();
}
