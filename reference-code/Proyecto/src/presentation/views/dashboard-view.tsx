"use client";

import {
  Banknote,
  Calculator,
  ChartNoAxesCombined,
  Landmark,
  Percent,
  Plus,
  TrendingUp,
  type LucideIcon,
} from "lucide-react";
import Link from "next/link";

import type { DashboardOverview } from "@/domain/entities/simulation";
import { Badge } from "@/presentation/components/ui/badge";
import { Button } from "@/presentation/components/ui/button";
import { Card } from "@/presentation/components/ui/card";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/presentation/components/ui/table";
import { useLanguage } from "@/presentation/i18n/language-context";
import {
  portalDictionary,
  type PortalTexts,
} from "@/presentation/i18n/portal-dictionary";

type DashboardViewProps = {
  email: string;
  overview: DashboardOverview;
};

type Metric = {
  label: string;
  value: string;
  helper: string;
  icon: LucideIcon;
  chip: string;
};

function formatMoney(value: number): string {
  return new Intl.NumberFormat("en-US", {
    currency: "USD",
    maximumFractionDigits: 0,
    style: "currency",
  }).format(value);
}

function formatPercent(value: number): string {
  return new Intl.NumberFormat("en-US", {
    maximumFractionDigits: 1,
    minimumFractionDigits: 1,
    style: "percent",
  }).format(value);
}

function formatDate(value: string): string {
  return new Intl.DateTimeFormat("en-US", {
    day: "2-digit",
    month: "short",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
    hour12: false,
  }).format(new Date(value));
}

function statusVariant(
  status: string,
): "success" | "secondary" | "destructive" {
  if (status === "approved") return "success";
  if (status === "rejected") return "destructive";
  return "secondary";
}

function statusLabel(status: string, t: PortalTexts): string {
  if (status === "approved") return t.status.approved;
  if (status === "rejected") return t.status.rejected;
  return t.status.pending;
}

export function DashboardView({ email, overview }: DashboardViewProps) {
  const { lang } = useLanguage();
  const t = portalDictionary[lang];
  const hasSimulations = overview.recentSimulations.length > 0;
  const h = t.metricHelpers;

  const metrics: Metric[] = [
    {
      label: t.metricLabels.funded,
      value: formatMoney(overview.totalFundedAmount),
      helper: hasSimulations ? h.portfolioTotal : h.noSimulations,
      icon: Banknote,
      chip: "bg-emerald-500/10 text-emerald-600",
    },
    {
      label: t.metricLabels.monthly,
      value: formatMoney(overview.averageMonthlyPayment),
      helper: hasSimulations ? h.averagePayment : h.stable,
      icon: Calculator,
      chip: "bg-blue-500/10 text-blue-600",
    },
    {
      label: t.metricLabels.tcea,
      value: formatPercent(overview.averageTcea),
      helper: hasSimulations ? h.costOfCredit : h.awaitingData,
      icon: Percent,
      chip: "bg-amber-500/10 text-amber-600",
    },
    {
      label: t.metricLabels.van,
      value: formatMoney(overview.portfolioVan),
      helper: hasSimulations ? h.netPresentValue : h.awaitingData,
      icon: Landmark,
      chip: "bg-violet-500/10 text-violet-600",
    },
    {
      label: t.metricLabels.tir,
      value: formatPercent(overview.averageTir),
      helper: hasSimulations ? h.averageIrr : h.targetPending,
      icon: TrendingUp,
      chip: "bg-emerald-500/10 text-emerald-600",
    },
  ];

  return (
    <div className="mx-auto flex w-full max-w-7xl flex-col gap-8 px-6 py-8">
      <header className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-foreground sm:text-3xl">
            {t.overviewTitle}
          </h1>
          <p className="mt-1.5 text-sm text-muted-foreground">
            {t.overviewSubtitle}{" "}
            <span className="font-medium text-foreground">{email}</span>.
          </p>
        </div>
        <Button asChild>
          <Link href="/vehicles">
            <Plus className="size-4" />
            {t.newSimulation}
          </Link>
        </Button>
      </header>

      <section className="grid gap-4 sm:grid-cols-2 xl:grid-cols-5">
        {metrics.map((metric) => (
          <Card
            key={metric.label}
            className="gap-0 py-5 transition hover:shadow-md"
          >
            <div className="flex items-center justify-between gap-3 px-5">
              <p className="text-[11px] font-semibold uppercase tracking-wide text-muted-foreground">
                {metric.label}
              </p>
              <span
                className={`flex size-8 shrink-0 items-center justify-center rounded-lg ${metric.chip}`}
              >
                <metric.icon className="size-4" />
              </span>
            </div>
            <p className="mt-4 px-5 text-2xl font-bold tracking-tight text-foreground">
              {metric.value}
            </p>
            <p className="mt-1.5 px-5 text-xs text-muted-foreground">
              {metric.helper}
            </p>
          </Card>
        ))}
      </section>

      <Card className="gap-0 overflow-hidden py-0">
        <div className="flex items-center justify-between border-b border-border px-6 py-4">
          <div>
            <h2 className="text-base font-semibold text-foreground">
              {t.recentTitle}
            </h2>
            <p className="mt-0.5 text-xs text-muted-foreground">
              {t.recentSubtitle}
            </p>
          </div>
          <span className="flex size-9 items-center justify-center rounded-lg bg-muted text-muted-foreground">
            <ChartNoAxesCombined className="size-[18px]" />
          </span>
        </div>

        {hasSimulations ? (
          <Table>
            <TableHeader>
              <TableRow className="bg-muted/40 hover:bg-muted/40">
                <TableHead className="pl-6">{t.table.client}</TableHead>
                <TableHead>{t.table.vehicle}</TableHead>
                <TableHead>{t.table.amount}</TableHead>
                <TableHead>{t.table.date}</TableHead>
                <TableHead className="pr-6">{t.table.status}</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {overview.recentSimulations.map((simulation) => (
                <TableRow key={simulation.id}>
                  <TableCell className="pl-6 font-medium text-foreground">
                    {simulation.clientName}
                  </TableCell>
                  <TableCell className="text-muted-foreground">
                    {simulation.vehicleLabel}
                  </TableCell>
                  <TableCell className="font-medium text-foreground">
                    {formatMoney(simulation.amountFinanced)}
                  </TableCell>
                  <TableCell className="text-muted-foreground">
                    {formatDate(simulation.simulatedAt)}
                  </TableCell>
                  <TableCell className="pr-6">
                    <Badge variant={statusVariant(simulation.status)}>
                      {statusLabel(simulation.status, t)}
                    </Badge>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        ) : (
          <div className="flex flex-col items-center justify-center px-6 py-16 text-center">
            <div className="mb-4 flex size-14 items-center justify-center rounded-2xl bg-muted">
              <Calculator className="size-6 text-muted-foreground" />
            </div>
            <h3 className="text-base font-semibold text-foreground">
              {t.emptyTitle}
            </h3>
            <p className="mt-1.5 max-w-md text-sm leading-6 text-muted-foreground">
              {t.emptyDescription}
            </p>
            <Button asChild className="mt-6">
              <Link href="/vehicles">
                <Plus className="size-4" />
                {t.registerVehicle}
              </Link>
            </Button>
          </div>
        )}
      </Card>
    </div>
  );
}
