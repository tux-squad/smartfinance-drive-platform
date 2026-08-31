import { createServerClient } from "@supabase/ssr";
import { cookies } from "next/headers";

import { getServerEnv, publicEnv } from "@/shared/config/env";

/**
 * Cliente de Supabase para el SERVIDOR (route handlers, server components,
 * server actions).
 *
 * Usa la anon key y la sesion del usuario almacenada en cookies, por lo que
 * respeta las politicas de Row Level Security (RLS). En Next.js 16 `cookies()`
 * es asincrono, de ahi que la funcion sea `async`.
 */
export async function createSupabaseServerClient() {
  const cookieStore = await cookies();

  return createServerClient(
    publicEnv.supabaseUrl,
    publicEnv.supabaseAnonKey,
    {
      cookies: {
        getAll() {
          return cookieStore.getAll();
        },
        setAll(cookiesToSet) {
          try {
            cookiesToSet.forEach(({ name, value, options }) => {
              cookieStore.set(name, value, options);
            });
          } catch {
            // `setAll` se llama desde un Server Component sin acceso de
            // escritura a cookies. Es seguro ignorarlo si el refresco de
            // sesion lo gestiona un middleware.
          }
        },
      },
    },
  );
}

/**
 * Cliente ADMINISTRADOR para el servidor.
 *
 * Usa la service role key y SALTA Row Level Security. Usar solo para tareas
 * privilegiadas en el backend (jobs, seeds, operaciones de sistema).
 *
 * NUNCA importar este modulo desde codigo que llegue al navegador.
 */
export function createSupabaseAdminClient() {
  const { supabaseServiceRoleKey } = getServerEnv();

  return createServerClient(
    publicEnv.supabaseUrl,
    supabaseServiceRoleKey,
    {
      cookies: {
        getAll() {
          return [];
        },
        setAll() {
          // El cliente admin no maneja sesion de usuario.
        },
      },
    },
  );
}
