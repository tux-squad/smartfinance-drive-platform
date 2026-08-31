import { createServerClient } from "@supabase/ssr";
import { NextResponse, type NextRequest } from "next/server";

import { publicEnv } from "@/shared/config/env";

/** Rutas accesibles sin sesion. */
const PUBLIC_ROUTES = ["/login", "/signup"];

/**
 * Refresca la sesion de Supabase en cada request y aplica el control de acceso.
 *
 * Se ejecuta en el middleware (Edge), por lo que NO puede usar el cliente de
 * servidor basado en `next/headers`. Crea su propio cliente atado a las cookies
 * del request/response.
 *
 * Reglas:
 * - Usuario sin sesion en ruta privada -> redirige a /login.
 * - Usuario con sesion en /login o /signup -> redirige a /dashboard.
 * - Las rutas /api no se redirigen: cada handler decide su propia proteccion.
 */
export async function updateSession(request: NextRequest): Promise<NextResponse> {
  let response = NextResponse.next({ request });

  const supabase = createServerClient(
    publicEnv.supabaseUrl,
    publicEnv.supabaseAnonKey,
    {
      cookies: {
        getAll() {
          return request.cookies.getAll();
        },
        setAll(cookiesToSet) {
          cookiesToSet.forEach(({ name, value }) => {
            request.cookies.set(name, value);
          });
          response = NextResponse.next({ request });
          cookiesToSet.forEach(({ name, value, options }) => {
            response.cookies.set(name, value, options);
          });
        },
      },
    },
  );

  // IMPORTANTE: getUser() revalida el token contra Supabase. No usar getSession()
  // aqui, que solo lee la cookie sin verificarla.
  const {
    data: { user },
  } = await supabase.auth.getUser();

  const { pathname } = request.nextUrl;
  const isPublic = PUBLIC_ROUTES.some((route) => pathname.startsWith(route));
  const isApi = pathname.startsWith("/api");

  if (!user && !isPublic && !isApi) {
    const url = request.nextUrl.clone();
    url.pathname = "/login";
    return NextResponse.redirect(url);
  }

  if (user && isPublic) {
    const url = request.nextUrl.clone();
    url.pathname = "/dashboard";
    return NextResponse.redirect(url);
  }

  return response;
}
