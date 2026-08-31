import { createBrowserClient } from "@supabase/ssr";

import { publicEnv } from "@/shared/config/env";

/**
 * Cliente de Supabase para el NAVEGADOR (componentes cliente).
 *
 * Usa la anon key y respeta las politicas de Row Level Security (RLS).
 * Seguro para exponerse en el bundle del cliente.
 */
export function createSupabaseBrowserClient() {
  return createBrowserClient(
    publicEnv.supabaseUrl,
    publicEnv.supabaseAnonKey,
  );
}
