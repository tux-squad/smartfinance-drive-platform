import { redirect } from "next/navigation";

import { getProfile } from "@/application/use-cases/profile/get-profile";
import { createSupabaseProfileRepository } from "@/infrastructure/repositories/supabase-profile-repository";
import { SettingsView } from "@/presentation/views/settings-view";

import { updateProfileAction } from "./actions";

export default async function SettingsPage() {
  const profile = await getProfile(createSupabaseProfileRepository());

  if (!profile) {
    redirect("/login");
  }

  return <SettingsView profile={profile} action={updateProfileAction} />;
}
