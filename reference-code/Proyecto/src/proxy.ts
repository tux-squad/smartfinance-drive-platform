import type { NextRequest } from "next/server";

import { updateSession } from "@/infrastructure/services/supabase/middleware";

// Convencion de Next.js 16 (sustituye al antiguo `middleware.ts`).
export async function proxy(request: NextRequest) {
  return updateSession(request);
}

export const config = {
  /**
   * Ejecuta el proxy en todas las rutas salvo assets estaticos e imagenes.
   * Asi la sesion se refresca en navegacion de paginas y se protege el acceso.
   */
  matcher: [
    "/((?!_next/static|_next/image|favicon.ico|.*\\.(?:svg|png|jpg|jpeg|gif|webp|ico|mp4|webm|mov|ogg)$).*)",
  ],
};
