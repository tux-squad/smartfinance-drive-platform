import type { Profile } from "@/domain/entities/profile";
import type { ProfileRepository } from "@/domain/repositories/profile-repository";

export async function getProfile(
  repository: ProfileRepository,
): Promise<Profile | null> {
  return repository.getCurrent();
}
