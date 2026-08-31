"use client";

import { useSyncExternalStore } from "react";

import { authDictionary, type AuthTexts, type Language } from "./auth-dictionary";

const STORAGE_KEY = "autify-lang";

/**
 * Pequeno store externo para la preferencia de idioma, respaldado por
 * localStorage. Se lee con useSyncExternalStore para soportar SSR sin
 * provocar setState dentro de efectos ni desajustes de hidratacion.
 */
let listeners: Array<() => void> = [];

function subscribe(callback: () => void) {
  listeners.push(callback);
  return () => {
    listeners = listeners.filter((listener) => listener !== callback);
  };
}

function getSnapshot(): Language {
  const stored = window.localStorage.getItem(STORAGE_KEY);
  return stored === "en" ? "en" : "es";
}

/** En el servidor (y en la primera hidratacion) el idioma por defecto es español. */
function getServerSnapshot(): Language {
  return "es";
}

function setLanguage(next: Language) {
  window.localStorage.setItem(STORAGE_KEY, next);
  listeners.forEach((listener) => listener());
}

type UseLanguageResult = {
  lang: Language;
  setLang: (lang: Language) => void;
  /** Diccionario de textos del idioma activo. */
  t: AuthTexts;
};

export function useLanguage(): UseLanguageResult {
  const lang = useSyncExternalStore(subscribe, getSnapshot, getServerSnapshot);
  return { lang, setLang: setLanguage, t: authDictionary[lang] };
}
