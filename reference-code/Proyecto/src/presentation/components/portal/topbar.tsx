"use client";

import { Search } from "lucide-react";

import { LanguageSwitcher } from "@/presentation/components/language-switcher";
import { LogoutButton } from "@/presentation/components/portal/logout-button";
import { useLanguage } from "@/presentation/i18n/language-context";
import { portalDictionary } from "@/presentation/i18n/portal-dictionary";

type TopbarProps = {
  email: string;
  logoutAction: () => Promise<void>;
};

export function Topbar({ email, logoutAction }: TopbarProps) {
  const { lang } = useLanguage();
  const t = portalDictionary[lang];
  const initial = email.charAt(0).toUpperCase();

  return (
    <header className="sticky top-0 z-20 flex h-16 shrink-0 items-center gap-4 border-b border-slate-200 bg-white px-6">
      <div className="relative flex w-full max-w-md items-center">
        <Search className="pointer-events-none absolute left-3 h-4 w-4 text-slate-400" />
        <input
          type="search"
          placeholder={t.searchPlaceholder}
          aria-label={t.searchPlaceholder}
          className="w-full rounded-lg border border-slate-200 bg-slate-50 py-2.5 pl-9 pr-3 text-sm text-slate-900 transition placeholder:text-slate-400 focus:border-slate-400 focus:bg-white focus:outline-none focus:ring-2 focus:ring-slate-900/5"
        />
      </div>

      <div className="ml-auto flex items-center gap-1">
        <LanguageSwitcher />

        <div className="mx-2 h-6 w-px bg-slate-200" />

        <div className="flex items-center gap-2.5">
          <span className="flex h-8 w-8 items-center justify-center rounded-full bg-slate-900 text-xs font-semibold text-white">
            {initial}
          </span>
          <span className="hidden text-sm font-medium text-slate-700 md:inline">
            {email}
          </span>
        </div>

        <LogoutButton logoutAction={logoutAction} />
      </div>
    </header>
  );
}
