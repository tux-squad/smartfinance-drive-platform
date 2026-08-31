"use client";

import { LogOut } from "lucide-react";
import { useEffect, useState } from "react";
import { useFormStatus } from "react-dom";

import { useLanguage } from "@/presentation/i18n/language-context";
import { portalDictionary } from "@/presentation/i18n/portal-dictionary";

function ConfirmButton({ label }: { label: string }) {
  const { pending } = useFormStatus();

  return (
    <button
      type="submit"
      disabled={pending}
      className="w-full rounded-lg bg-red-600 px-4 py-2.5 text-sm font-semibold text-white shadow-sm transition hover:bg-red-700 disabled:cursor-not-allowed disabled:opacity-60"
    >
      {pending ? `${label}...` : label}
    </button>
  );
}

export function LogoutButton({
  logoutAction,
}: {
  logoutAction: () => Promise<void>;
}) {
  const { lang } = useLanguage();
  const t = portalDictionary[lang];
  const [open, setOpen] = useState(false);

  // Cerrar el modal con la tecla Escape.
  useEffect(() => {
    if (!open) {
      return;
    }

    const onKeyDown = (event: KeyboardEvent) => {
      if (event.key === "Escape") {
        setOpen(false);
      }
    };

    window.addEventListener("keydown", onKeyDown);
    return () => window.removeEventListener("keydown", onKeyDown);
  }, [open]);

  return (
    <>
      <button
        type="button"
        onClick={() => setOpen(true)}
        aria-label={t.signOut}
        title={t.signOut}
        className="ml-1 flex h-9 w-9 items-center justify-center rounded-lg text-slate-500 transition hover:bg-red-50 hover:text-red-600"
      >
        <LogOut className="h-[18px] w-[18px]" />
      </button>

      {open ? (
        <div
          className="fixed inset-0 z-50 flex items-center justify-center p-4"
          role="dialog"
          aria-modal="true"
          aria-labelledby="logout-title"
        >
          <button
            type="button"
            aria-label={t.cancel}
            onClick={() => setOpen(false)}
            className="absolute inset-0 cursor-default bg-slate-900/50 backdrop-blur-sm"
          />

          <div className="relative w-full max-w-sm rounded-xl border border-slate-200 bg-white p-6 shadow-xl">
            <div className="flex flex-col items-center text-center">
              <span className="flex h-12 w-12 items-center justify-center rounded-full bg-red-50 text-red-600">
                <LogOut className="h-6 w-6" />
              </span>
              <h2
                id="logout-title"
                className="mt-4 text-lg font-semibold text-slate-900"
              >
                {t.signOutTitle}
              </h2>
              <p className="mt-2 text-sm leading-6 text-slate-500">
                {t.signOutMessage}
              </p>
            </div>

            <div className="mt-6 flex gap-3">
              <button
                type="button"
                onClick={() => setOpen(false)}
                className="flex-1 rounded-lg border border-slate-200 px-4 py-2.5 text-sm font-semibold text-slate-700 transition hover:bg-slate-50"
              >
                {t.cancel}
              </button>
              <form action={logoutAction} className="flex-1">
                <ConfirmButton label={t.confirmSignOut} />
              </form>
            </div>
          </div>
        </div>
      ) : null}
    </>
  );
}
