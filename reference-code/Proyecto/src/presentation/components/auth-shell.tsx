"use client";

import type { ReactNode } from "react";

import { LanguageSwitcher } from "@/presentation/components/language-switcher";
import { useLanguage } from "@/presentation/i18n/language-context";

/** Panel derecho: video de carros con capa oscura y texto de marca. */
function HeroPanel() {
  const { t } = useLanguage();

  return (
    <aside className="relative hidden overflow-hidden bg-slate-950 lg:block lg:w-1/2">
      {/* Fondo de respaldo por si el video aun no carga. */}
      <div
        aria-hidden="true"
        className="absolute inset-0 bg-gradient-to-br from-slate-800 via-slate-900 to-black"
      />

      <video
        className="absolute inset-0 h-full w-full object-cover"
        autoPlay
        muted
        loop
        playsInline
        poster="/auth/cars-poster.jpg"
      >
        <source src="/auth/cars.mp4" type="video/mp4" />
        <source src="/auth/cars.webm" type="video/webm" />
      </video>

      {/* Capa oscura uniforme para dar contraste. */}
      <div aria-hidden="true" className="absolute inset-0 bg-black/50" />

      <div className="relative flex h-full flex-col justify-end p-12 text-white">
        <h2 className="text-3xl font-semibold leading-tight">{t.heroTitle}</h2>
        <p className="mt-3 max-w-md text-sm leading-relaxed text-white/80">
          {t.heroSubtitle}
        </p>
      </div>
    </aside>
  );
}

/**
 * Estructura compartida de las pantallas de auth:
 * - Columna izquierda: formulario (login / registro).
 * - Columna derecha: video (solo en escritorio).
 * - Switcher de idioma fijo arriba a la derecha, visible en movil y escritorio.
 */
export function AuthShell({ children }: { children: ReactNode }) {
  return (
    <main className="relative flex min-h-screen bg-background">
      <div className="absolute right-4 top-4 z-20 sm:right-6 sm:top-6">
        <LanguageSwitcher />
      </div>

      <section className="flex w-full flex-col items-center justify-center px-6 py-12 sm:px-10 lg:w-1/2">
        {children}
      </section>

      <HeroPanel />
    </main>
  );
}
