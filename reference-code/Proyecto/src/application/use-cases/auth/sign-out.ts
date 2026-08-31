import type { AuthRepository } from "@/domain/repositories/auth-repository";

export async function signOut(repository: AuthRepository): Promise<void> {
  await repository.signOut();
}
