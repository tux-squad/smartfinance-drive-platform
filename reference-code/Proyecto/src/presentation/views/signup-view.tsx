"use client";

import Link from "next/link";
import { useActionState } from "react";

import { AuthCard } from "@/presentation/components/auth-card";
import { Button } from "@/presentation/components/ui/button";
import { Input } from "@/presentation/components/ui/input";
import { Label } from "@/presentation/components/ui/label";
import { useLanguage } from "@/presentation/i18n/language-context";
import type { AuthFormAction, AuthFormState } from "@/shared/types/auth-form-state";

const initialState: AuthFormState = { errorCode: null };

export function SignupView({ action }: { action: AuthFormAction }) {
  const [state, formAction, isPending] = useActionState(action, initialState);
  const { t } = useLanguage();

  return (
    <AuthCard subtitle={t.signupSubtitle}>
      <form action={formAction} className="flex flex-col gap-5">
        {state.errorCode ? (
          <p
            role="alert"
            className="rounded-lg border border-destructive/30 bg-destructive/10 px-3 py-2 text-sm text-destructive"
          >
            {t.errors[state.errorCode]}
          </p>
        ) : null}

        <div className="flex flex-col gap-1.5">
          <Label htmlFor="email">{t.emailLabel}</Label>
          <Input
            id="email"
            name="email"
            type="email"
            autoComplete="email"
            required
            placeholder={t.emailPlaceholder}
          />
        </div>

        <div className="flex flex-col gap-1.5">
          <Label htmlFor="password">{t.passwordLabel}</Label>
          <Input
            id="password"
            name="password"
            type="password"
            autoComplete="new-password"
            required
            minLength={6}
            placeholder={t.passwordHint}
          />
        </div>

        <div className="flex flex-col gap-1.5">
          <Label htmlFor="confirmPassword">{t.confirmPasswordLabel}</Label>
          <Input
            id="confirmPassword"
            name="confirmPassword"
            type="password"
            autoComplete="new-password"
            required
            minLength={6}
            placeholder={t.confirmPasswordPlaceholder}
          />
        </div>

        <Button type="submit" disabled={isPending} className="mt-1 w-full">
          {isPending ? (
            t.creatingAccount
          ) : (
            <>
              {t.createAccount} <span aria-hidden="true">→</span>
            </>
          )}
        </Button>
      </form>

      <p className="mt-6 text-center text-sm text-muted-foreground">
        {t.haveAccount}{" "}
        <Link
          href="/login"
          className="font-semibold text-foreground underline-offset-2 hover:underline"
        >
          {t.goSignIn}
        </Link>
      </p>
    </AuthCard>
  );
}
