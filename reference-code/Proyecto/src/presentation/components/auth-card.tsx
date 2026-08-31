"use client";

import type { ReactNode } from "react";

import { ShieldIcon } from "@/presentation/components/shield-icon";
import { useLanguage } from "@/presentation/i18n/language-context";

type AuthCardProps = {
  subtitle: string;
  children: ReactNode;
};

/**
 * Marco visual compartido por las pantallas de auth: marca Autify, subtitulo
 * y un pie discreto. El contenido del formulario se pasa como children.
 */
export function AuthCard({ subtitle, children }: AuthCardProps) {
  const { t } = useLanguage();

  return (
    <div className="w-full max-w-sm rounded-2xl border border-border bg-card p-8 shadow-sm">
      <div className="flex flex-col items-center gap-4 text-center">
        <span className="flex size-12 items-center justify-center rounded-xl bg-primary text-primary-foreground shadow-sm">
          <ShieldIcon className="size-6" />
        </span>
        <div className="space-y-1">
          <h1 className="text-2xl font-bold tracking-tight text-foreground">
            Autify
          </h1>
          <p className="text-sm text-muted-foreground">{subtitle}</p>
        </div>
      </div>

      <div className="mt-8">{children}</div>

      <p className="mt-10 text-center text-xs text-muted-foreground">
        {t.footer}
      </p>
    </div>
  );
}
