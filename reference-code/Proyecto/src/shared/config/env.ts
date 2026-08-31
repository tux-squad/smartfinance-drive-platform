/**
 * Validacion centralizada de variables de entorno.
 *
 * IMPORTANTE: las variables `NEXT_PUBLIC_*` se acceden de forma ESTATICA
 * (`process.env.NOMBRE_LITERAL`) para que Next.js las inyecte en el bundle del
 * navegador y en el Edge runtime del middleware. El acceso dinamico
 * (`process.env[variable]`) no se inyecta y devolveria undefined en Edge.
 *
 * Reglas:
 * - Las variables con prefijo `NEXT_PUBLIC_` son seguras para el navegador.
 * - `SUPABASE_SERVICE_ROLE_KEY` es SECRETA: solo debe leerse en el servidor.
 */

function required(name: string, value: string | undefined): string {
  if (!value || value.trim() === "") {
    throw new Error(
      `Falta la variable de entorno "${name}". Revisa tu archivo .env.local.`,
    );
  }

  return value;
}

/** Variables publicas: disponibles en navegador, servidor y Edge. */
export const publicEnv = {
  supabaseUrl: required(
    "NEXT_PUBLIC_SUPABASE_URL",
    process.env.NEXT_PUBLIC_SUPABASE_URL,
  ),
  supabaseAnonKey: required(
    "NEXT_PUBLIC_SUPABASE_ANON_KEY",
    process.env.NEXT_PUBLIC_SUPABASE_ANON_KEY,
  ),
} as const;

/**
 * Variables solo de servidor. Lanza si se intenta leer en el navegador,
 * evitando filtrar la service role key al cliente por accidente. La lectura va
 * dentro de la funcion para que nunca se evalue al importar en codigo cliente.
 */
export function getServerEnv() {
  if (typeof window !== "undefined") {
    throw new Error(
      "getServerEnv() no puede usarse en el navegador. Es solo de servidor.",
    );
  }

  return {
    supabaseServiceRoleKey: required(
      "SUPABASE_SERVICE_ROLE_KEY",
      process.env.SUPABASE_SERVICE_ROLE_KEY,
    ),
  } as const;
}
