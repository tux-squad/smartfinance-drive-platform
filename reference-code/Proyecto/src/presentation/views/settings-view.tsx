"use client";

import { Landmark, UserSquare } from "lucide-react";
import Link from "next/link";
import { useActionState } from "react";

import type { Profile } from "@/domain/entities/profile";
import { Button } from "@/presentation/components/ui/button";
import { Card } from "@/presentation/components/ui/card";
import { Input } from "@/presentation/components/ui/input";
import { Label } from "@/presentation/components/ui/label";
import { EMPLOYMENT_STATUSES } from "@/shared/constants/employment-status";
import type {
  SettingsFormAction,
  SettingsFormState,
} from "@/shared/types/settings-form-state";

const initialState: SettingsFormState = { error: null, success: false };

const selectClass =
  "w-full rounded-md border border-input bg-background px-3 py-2 text-sm text-foreground shadow-xs outline-none transition focus-visible:border-ring focus-visible:ring-[3px] focus-visible:ring-ring/50";

const phoneCodes = ["+51", "+1", "+52", "+54", "+56", "+57"];

type SettingsViewProps = {
  profile: Profile;
  action: SettingsFormAction;
};

export function SettingsView({ profile, action }: SettingsViewProps) {
  const [state, formAction, isPending] = useActionState(action, initialState);

  return (
    <div className="mx-auto w-full max-w-4xl px-6 py-10">
      <header className="mb-8">
        <h1 className="text-2xl font-bold text-foreground">Account Settings</h1>
        <p className="mt-1 text-sm text-muted-foreground">
          Manage your personal and financial information.
        </p>
      </header>

      <form action={formAction} className="flex flex-col gap-6">
        {state.error ? (
          <p
            role="alert"
            className="rounded-md border border-destructive/30 bg-destructive/10 px-4 py-3 text-sm text-destructive"
          >
            {state.error}
          </p>
        ) : null}
        {state.success ? (
          <p
            role="status"
            className="rounded-md border border-emerald-500/30 bg-emerald-500/10 px-4 py-3 text-sm text-emerald-700"
          >
            Your changes have been saved.
          </p>
        ) : null}

        {/* Personal Data */}
        <Card className="gap-0 p-6">
          <div className="mb-6 flex items-center gap-2 border-b border-border pb-4">
            <UserSquare className="size-5 text-muted-foreground" />
            <h2 className="text-lg font-semibold text-foreground">
              Personal Data
            </h2>
          </div>

          <div className="grid gap-5 sm:grid-cols-2">
            <div className="flex flex-col gap-1.5">
              <Label htmlFor="nationalId">National ID (DNI)</Label>
              <Input
                id="nationalId"
                name="nationalId"
                inputMode="numeric"
                maxLength={8}
                defaultValue={profile.nationalId ?? ""}
                placeholder="e.g. 12345678"
              />
            </div>

            <div className="flex flex-col gap-1.5">
              <Label htmlFor="dateOfBirth">Date of Birth</Label>
              <Input
                id="dateOfBirth"
                name="dateOfBirth"
                type="date"
                defaultValue={profile.dateOfBirth ?? ""}
              />
            </div>

            <div className="flex flex-col gap-1.5 sm:col-span-2">
              <Label htmlFor="fullLegalNames">Full Legal Names</Label>
              <Input
                id="fullLegalNames"
                name="fullLegalNames"
                defaultValue={profile.fullLegalNames ?? ""}
                placeholder="As it appears on DNI"
              />
            </div>

            <div className="flex flex-col gap-1.5">
              <Label htmlFor="email">Email Address</Label>
              <Input
                id="email"
                name="email"
                type="email"
                value={profile.email}
                readOnly
                disabled
                className="cursor-not-allowed bg-muted text-muted-foreground"
              />
              <span className="text-xs text-muted-foreground">
                Tied to your login. Cannot be changed here.
              </span>
            </div>

            <div className="flex flex-col gap-1.5">
              <Label htmlFor="mobilePhone">Mobile Phone</Label>
              <div className="flex gap-2">
                <select
                  name="phoneCountryCode"
                  aria-label="Country code"
                  defaultValue={profile.phoneCountryCode ?? "+51"}
                  className={`w-24 shrink-0 ${selectClass}`}
                >
                  {phoneCodes.map((code) => (
                    <option key={code} value={code}>
                      {code}
                    </option>
                  ))}
                </select>
                <Input
                  id="mobilePhone"
                  name="mobilePhone"
                  inputMode="tel"
                  defaultValue={profile.mobilePhone ?? ""}
                  placeholder="987 654 321"
                  className="min-w-0 flex-1"
                />
              </div>
            </div>
          </div>
        </Card>

        {/* Financial Profile */}
        <Card className="gap-0 p-6">
          <div className="mb-6 flex items-center gap-2 border-b border-border pb-4">
            <Landmark className="size-5 text-muted-foreground" />
            <h2 className="text-lg font-semibold text-foreground">
              Financial Profile
            </h2>
          </div>

          <div className="grid gap-5 sm:grid-cols-2">
            <div className="flex flex-col gap-1.5">
              <Label htmlFor="monthlyIncome">Monthly Income (PEN)</Label>
              <Input
                id="monthlyIncome"
                name="monthlyIncome"
                inputMode="decimal"
                defaultValue={
                  profile.monthlyIncome === null
                    ? ""
                    : String(profile.monthlyIncome)
                }
                placeholder="S/ 0.00"
              />
            </div>

            <div className="flex flex-col gap-1.5">
              <Label htmlFor="employmentStatus">Employment Status</Label>
              <select
                id="employmentStatus"
                name="employmentStatus"
                defaultValue={profile.employmentStatus ?? ""}
                className={selectClass}
              >
                <option value="">Select Status</option>
                {EMPLOYMENT_STATUSES.map((status) => (
                  <option key={status.value} value={status.value}>
                    {status.label}
                  </option>
                ))}
              </select>
            </div>
          </div>
        </Card>

        <div className="flex items-center justify-end gap-3">
          <Button asChild variant="outline">
            <Link href="/dashboard">Cancel</Link>
          </Button>
          <Button type="submit" disabled={isPending}>
            {isPending ? "Saving…" : "Save changes"}
          </Button>
        </div>
      </form>
    </div>
  );
}
