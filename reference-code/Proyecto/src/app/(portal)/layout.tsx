import { redirect } from "next/navigation";
import type { ReactNode } from "react";

import { getCurrentUser } from "@/application/use-cases/auth/get-current-user";
import { createSupabaseAuthRepository } from "@/infrastructure/repositories/supabase-auth-repository";
import { Sidebar } from "@/presentation/components/portal/sidebar";
import { Topbar } from "@/presentation/components/portal/topbar";

import { logoutAction } from "./actions";

export default async function PortalLayout({
  children,
}: {
  children: ReactNode;
}) {
  const user = await getCurrentUser(createSupabaseAuthRepository());

  // El proxy ya protege estas rutas; segunda barrera por si acaso.
  if (!user) {
    redirect("/login");
  }

  return (
    <div className="flex min-h-screen bg-[#f6f7fb]">
      <Sidebar />
      <div className="flex min-h-screen min-w-0 flex-1 flex-col">
        <Topbar email={user.email} logoutAction={logoutAction} />
        <main className="min-h-0 flex-1">{children}</main>
      </div>
    </div>
  );
}
