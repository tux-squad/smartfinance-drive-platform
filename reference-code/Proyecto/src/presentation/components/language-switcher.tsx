"use client";

import { useLanguage } from "@/presentation/i18n/language-context";
import type { Language } from "@/presentation/i18n/auth-dictionary";

const OPTIONS: Language[] = ["es", "en"];

/**
 * Conmutador de idioma (ES / EN). Tiene su propio fondo oscuro translucido,
 * por lo que se ve bien tanto sobre el video como sobre fondos claros (movil).
 */
export function LanguageSwitcher() {
  const { lang, setLang } = useLanguage();

  return (
    <div className="inline-flex items-center gap-0.5 rounded-full border border-white/20 bg-slate-900/70 p-1 text-xs font-semibold shadow-sm backdrop-blur">
      {OPTIONS.map((option) => {
        const active = option === lang;
        return (
          <button
            key={option}
            type="button"
            onClick={() => setLang(option)}
            aria-pressed={active}
            className={`rounded-full px-3 py-1 uppercase transition ${
              active
                ? "bg-white text-slate-900"
                : "text-white/70 hover:text-white"
            }`}
          >
            {option}
          </button>
        );
      })}
    </div>
  );
}
