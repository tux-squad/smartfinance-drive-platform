"use client";

import {
  ArrowDownRight,
  ArrowUpRight,
  FileSpreadsheet,
  Loader2,
  Minus,
  Search,
} from "lucide-react";
import { useRouter } from "next/navigation";
import { useMemo, useState, useTransition } from "react";

import type {
  PaymentScheduleOverview,
  PaymentSchedulePeriod,
} from "@/domain/entities/payment-schedule";
import { Button } from "@/presentation/components/ui/button";
import { Card } from "@/presentation/components/ui/card";
import { Input } from "@/presentation/components/ui/input";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/presentation/components/ui/select";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/presentation/components/ui/table";
import { useLanguage } from "@/presentation/i18n/language-context";

interface SimulationOption {
  id: string;
  label: string;
  date: string;
}

type PaymentScheduleViewProps = {
  overview: PaymentScheduleOverview;
  simulations: SimulationOption[];
  selectedId: string | null;
};

const pageSize = 12;

const copy = {
  es: {
    eyebrow: "Detalle de la simulación",
    title: "Cronograma de pagos",
    subtitle:
      "Plan de pagos por el método francés vencido. Cambia de simulación para comparar escenarios.",
    pickSimulation: "Elegir simulación",
    exportPdf: "Exportar PDF",
    exportExcel: "Exportar Excel",
    totalLoan: "Monto financiado",
    totalLoanHint: "Capital neto prestado al inicio",
    totalInterest: "Interés total",
    totalInterestHint: "Costo financiero acumulado",
    totalInsurance: "Seguros totales",
    totalInsuranceHint: "Suma de desgravamen en el periodo",
    totalPaid: "Total a pagar",
    totalPaidHint: "Lo que terminarás pagando",
    legendTitle: "Cómo leer la tabla",
    legendDown: "Baja cada mes",
    legendUp: "Sube cada mes",
    legendStable: "Se mantiene constante",
    legInterest: "Interés: se calcula sobre el saldo, por eso disminuye.",
    legPrincipal: "Amortización: cada mes pagas más capital.",
    legTotal: "Cuota total: constante (método francés).",
    legBalance: "Saldo: el capital pendiente baja hasta cero.",
    searchPlaceholder: "Buscar por cuota #",
    allYears: "Todos los años",
    showing: "Mostrando",
    of: "de",
    periods: "periodos",
    empty: "No hay cronograma para la simulación seleccionada.",
    prev: "Anterior",
    next: "Siguiente",
    cols: {
      n: "Cuota",
      opening: "Saldo inicial",
      interest: "Interés",
      principal: "Amortización",
      insurance: "Seguro",
      total: "Cuota total",
      closing: "Saldo final",
    },
    breakdown: "Amortización vs. Interés",
    principal: "Amortización",
    interest: "Interés",
    compositionTitle: "Composición de la cuota",
    balanceTitle: "Saldo pendiente",
    balanceSeries: "Saldo",
    periodLabel: "Cuota",
  },
  en: {
    eyebrow: "Simulation details",
    title: "Payment schedule",
    subtitle:
      "Payment plan using the French amortization method. Switch simulation to compare scenarios.",
    pickSimulation: "Pick simulation",
    exportPdf: "Export PDF",
    exportExcel: "Export Excel",
    totalLoan: "Financed amount",
    totalLoanHint: "Net principal disbursed at the start",
    totalInterest: "Total interest",
    totalInterestHint: "Accumulated financing cost",
    totalInsurance: "Total insurance",
    totalInsuranceHint: "Sum of life insurance charges",
    totalPaid: "Total to pay",
    totalPaidHint: "What you'll end up paying",
    legendTitle: "How to read the table",
    legendDown: "Decreases monthly",
    legendUp: "Increases monthly",
    legendStable: "Stays constant",
    legInterest: "Interest: computed on the balance, so it decreases.",
    legPrincipal: "Principal: you repay more capital each month.",
    legTotal: "Total installment: constant (French method).",
    legBalance: "Balance: the outstanding capital falls to zero.",
    searchPlaceholder: "Search by installment #",
    allYears: "All years",
    showing: "Showing",
    of: "of",
    periods: "periods",
    empty: "No schedule for the selected simulation.",
    prev: "Previous",
    next: "Next",
    cols: {
      n: "No.",
      opening: "Opening balance",
      interest: "Interest",
      principal: "Principal",
      insurance: "Insurance",
      total: "Total payment",
      closing: "Closing balance",
    },
    breakdown: "Principal vs. Interest",
    principal: "Principal",
    interest: "Interest",
    compositionTitle: "Installment composition",
    balanceTitle: "Outstanding balance",
    balanceSeries: "Balance",
    periodLabel: "Installment",
  },
} as const;

function formatMoney(value: number, currency: "PEN" | "USD"): string {
  return new Intl.NumberFormat("en-US", {
    currency,
    maximumFractionDigits: 2,
    minimumFractionDigits: 2,
    style: "currency",
  }).format(value);
}

function formatPeriod(periodNumber: number): string {
  return periodNumber.toString().padStart(2, "0");
}

function getYear(value: string): string {
  return new Date(value).getFullYear().toString();
}

function formatSimulationDate(value: string, lang: string): string {
  return new Date(value).toLocaleString(lang, {
    month: "short",
    day: "numeric",
    hour: "2-digit",
    minute: "2-digit",
    hour12: false,
  });
}

export function PaymentScheduleView({
  overview,
  simulations,
  selectedId,
}: PaymentScheduleViewProps) {
  const { lang } = useLanguage();
  const t = copy[lang];
  const router = useRouter();

  const [search, setSearch] = useState("");
  const [selectedYear, setSelectedYear] = useState("all");
  const [page, setPage] = useState(1);
  const [isPending, startTransition] = useTransition();

  const years = useMemo(
    () =>
      Array.from(
        new Set(overview.periods.map((period) => getYear(period.dueDate))),
      ).sort((a, b) => Number(a) - Number(b)),
    [overview.periods],
  );

  const filteredPeriods = useMemo(() => {
    const normalizedSearch = search.trim();
    return overview.periods.filter((period) => {
      const matchesSearch =
        !normalizedSearch ||
        formatPeriod(period.periodNumber).includes(normalizedSearch);
      const matchesYear =
        selectedYear === "all" || getYear(period.dueDate) === selectedYear;
      return matchesSearch && matchesYear;
    });
  }, [overview.periods, search, selectedYear]);

  const totalPages = Math.max(1, Math.ceil(filteredPeriods.length / pageSize));
  const safePage = Math.min(page, totalPages);
  const startIndex = (safePage - 1) * pageSize;
  const visiblePeriods = filteredPeriods.slice(
    startIndex,
    startIndex + pageSize,
  );

  const showingFrom =
    filteredPeriods.length === 0
      ? 0
      : Math.min(startIndex + 1, filteredPeriods.length);
  const showingTo = Math.min(startIndex + pageSize, filteredPeriods.length);
  const pages = Array.from(
    { length: Math.min(totalPages, 5) },
    (_, index) => index + 1,
  );

  function downloadFile(filename: string, content: string, type: string) {
    const blob = new Blob([content], { type });
    const url = URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = url;
    link.download = filename;
    link.click();
    URL.revokeObjectURL(url);
  }

  function exportExcel() {
    const headers = [
      t.cols.n,
      t.cols.opening,
      t.cols.interest,
      t.cols.principal,
      t.cols.insurance,
      t.cols.total,
      t.cols.closing,
      "Fecha",
    ];
    const rows = filteredPeriods.map((period) => [
      formatPeriod(period.periodNumber),
      period.openingBalance.toFixed(2),
      period.interest.toFixed(2),
      period.principal.toFixed(2),
      period.insurance.toFixed(2),
      period.totalPayment.toFixed(2),
      period.closingBalance.toFixed(2),
      period.dueDate,
    ]);
    const csv = [headers, ...rows]
      .map((row) => row.map((cell) => `"${cell}"`).join(","))
      .join("\n");
    downloadFile(
      "cronograma-pagos.csv",
      `﻿${csv}`,
      "text/csv;charset=utf-8",
    );
  }

  return (
    <div className="mx-auto flex w-full max-w-7xl flex-col gap-6 px-6 py-8">
      <header className="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
        <div>
          <p className="text-xs font-bold uppercase tracking-wide text-muted-foreground">
            {t.eyebrow}
          </p>
          <h1 className="mt-1 text-3xl font-bold tracking-tight text-foreground">
            {t.title}
          </h1>
          <p className="mt-1 max-w-xl text-sm text-muted-foreground">
            {t.subtitle}
          </p>
        </div>

        <div className="flex w-full flex-col gap-2 sm:w-auto sm:flex-row sm:items-center sm:justify-end">
          {simulations.length > 0 ? (
            <div className="relative w-full sm:w-64">
              <Select
                value={selectedId ?? undefined}
                disabled={isPending}
                onValueChange={(value) =>
                  startTransition(() => router.push(`/payment?sim=${value}`))
                }
              >
                <SelectTrigger className="w-full">
                  <SelectValue placeholder={t.pickSimulation} />
                </SelectTrigger>
                <SelectContent>
                  {simulations.map((item) => (
                    <SelectItem key={item.id} value={item.id}>
                      {item.label} · {formatSimulationDate(item.date, lang)}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              {isPending ? (
                <Loader2 className="pointer-events-none absolute right-9 top-1/2 size-4 -translate-y-1/2 animate-spin text-muted-foreground" />
              ) : null}
            </div>
          ) : null}
          <Button
            size="sm"
            onClick={exportExcel}
            disabled={filteredPeriods.length === 0}
            className="h-9 px-3 sm:px-4"
          >
            <FileSpreadsheet className="size-4" />
            {t.exportExcel}
          </Button>
        </div>
      </header>

      <section className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
        <SummaryCard
          label={t.totalLoan}
          value={formatMoney(overview.totalLoanAmount, overview.currency)}
          hint={t.totalLoanHint}
        />
        <SummaryCard
          label={t.totalInterest}
          value={formatMoney(overview.totalInterest, overview.currency)}
          hint={t.totalInterestHint}
        />
        <SummaryCard
          label={t.totalInsurance}
          value={formatMoney(overview.totalInsurance, overview.currency)}
          hint={t.totalInsuranceHint}
        />
        <SummaryCard
          label={t.totalPaid}
          value={formatMoney(overview.totalPaid, overview.currency)}
          hint={t.totalPaidHint}
          highlight
        />
      </section>

      {/* Leyenda: qué baja, qué sube, qué se mantiene */}
      <Card className="gap-3 py-4">
        <div className="px-6 text-sm font-semibold text-foreground">
          {t.legendTitle}
        </div>
        <div className="grid gap-2 px-6 text-sm sm:grid-cols-2">
          <LegendItem icon="down" text={t.legInterest} />
          <LegendItem icon="up" text={t.legPrincipal} />
          <LegendItem icon="stable" text={t.legTotal} />
          <LegendItem icon="down" text={t.legBalance} />
        </div>
      </Card>

      {/* Filtros */}
      <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <div className="flex flex-wrap gap-3">
          <div className="relative">
            <Search className="pointer-events-none absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
            <Input
              value={search}
              onChange={(event) => {
                setSearch(event.target.value);
                setPage(1);
              }}
              placeholder={t.searchPlaceholder}
              className="w-56 pl-9"
            />
          </div>
          <Select
            value={selectedYear}
            onValueChange={(value) => {
              setSelectedYear(value);
              setPage(1);
            }}
          >
            <SelectTrigger className="w-40">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">{t.allYears}</SelectItem>
              {years.map((year) => (
                <SelectItem key={year} value={year}>
                  {year}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
        <p className="text-sm text-muted-foreground">
          {t.showing} {showingFrom}-{showingTo} {t.of} {filteredPeriods.length}{" "}
          {t.periods}
        </p>
      </div>

      {/* Tabla */}
      <Card
        className={`overflow-hidden py-0 transition-opacity ${
          isPending ? "pointer-events-none opacity-60" : "opacity-100"
        }`}
      >
        <Table>
          <TableHeader>
            <TableRow className="bg-muted/50 hover:bg-muted/50">
              <TableHead className="pl-4">{t.cols.n}</TableHead>
              <TableHead className="text-right">{t.cols.opening}</TableHead>
              <TableHead className="text-right">
                <span className="inline-flex items-center gap-1">
                  {t.cols.interest}
                  <ArrowDownRight className="size-3 text-amber-500" />
                </span>
              </TableHead>
              <TableHead className="text-right">
                <span className="inline-flex items-center gap-1">
                  {t.cols.principal}
                  <ArrowUpRight className="size-3 text-emerald-500" />
                </span>
              </TableHead>
              <TableHead className="text-right">{t.cols.insurance}</TableHead>
              <TableHead className="text-right">
                <span className="inline-flex items-center gap-1">
                  {t.cols.total}
                  <Minus className="size-3 text-muted-foreground" />
                </span>
              </TableHead>
              <TableHead className="pr-4 text-right">{t.cols.closing}</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {visiblePeriods.length > 0 ? (
              visiblePeriods.map((period) => (
                <ScheduleRow
                  key={period.id}
                  currency={overview.currency}
                  period={period}
                />
              ))
            ) : (
              <TableRow>
                <TableCell
                  colSpan={7}
                  className="py-10 text-center text-sm text-muted-foreground"
                >
                  {t.empty}
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </Card>

      {/* Paginación */}
      {filteredPeriods.length > 0 ? (
        <div className="flex items-center justify-between text-sm">
          <Button
            variant="ghost"
            size="sm"
            disabled={safePage === 1}
            onClick={() => setPage((current) => Math.max(1, current - 1))}
          >
            {t.prev}
          </Button>
          <div className="flex items-center gap-1">
            {pages.map((pageNumber) => (
              <Button
                key={pageNumber}
                variant={safePage === pageNumber ? "default" : "ghost"}
                size="icon"
                className="size-8"
                onClick={() => setPage(pageNumber)}
              >
                {pageNumber}
              </Button>
            ))}
          </div>
          <Button
            variant="ghost"
            size="sm"
            disabled={safePage === totalPages}
            onClick={() =>
              setPage((current) => Math.min(totalPages, current + 1))
            }
          >
            {t.next}
          </Button>
        </div>
      ) : null}
    </div>
  );
}

function SummaryCard({
  label,
  value,
  hint,
  highlight,
}: {
  label: string;
  value: string;
  hint?: string;
  highlight?: boolean;
}) {
  return (
    <Card className="gap-1 py-4">
      <div className="px-5">
        <p className="text-xs font-semibold text-muted-foreground">{label}</p>
        <p
          className={`mt-1 text-lg font-bold ${
            highlight ? "text-emerald-600" : "text-foreground"
          }`}
        >
          {value}
        </p>
        {hint ? (
          <p className="mt-0.5 text-xs text-muted-foreground">{hint}</p>
        ) : null}
      </div>
    </Card>
  );
}

function LegendItem({
  icon,
  text,
}: {
  icon: "up" | "down" | "stable";
  text: string;
}) {
  const Icon =
    icon === "up" ? ArrowUpRight : icon === "down" ? ArrowDownRight : Minus;
  const color =
    icon === "up"
      ? "text-emerald-500"
      : icon === "down"
        ? "text-amber-500"
        : "text-muted-foreground";
  return (
    <span className="flex items-center gap-2 text-muted-foreground">
      <Icon className={`size-4 shrink-0 ${color}`} />
      {text}
    </span>
  );
}

function ScheduleRow({
  currency,
  period,
}: {
  currency: "PEN" | "USD";
  period: PaymentSchedulePeriod;
}) {
  return (
    <TableRow>
      <TableCell className="pl-4 font-medium text-foreground">
        {formatPeriod(period.periodNumber)}
      </TableCell>
      <TableCell className="text-right font-semibold text-foreground">
        {formatMoney(period.openingBalance, currency)}
      </TableCell>
      <TableCell className="text-right text-amber-600">
        {formatMoney(period.interest, currency)}
      </TableCell>
      <TableCell className="text-right text-emerald-600">
        {formatMoney(period.principal, currency)}
      </TableCell>
      <TableCell className="text-right text-muted-foreground">
        {formatMoney(period.insurance, currency)}
      </TableCell>
      <TableCell className="text-right font-bold text-foreground">
        {formatMoney(period.totalPayment, currency)}
      </TableCell>
      <TableCell className="pr-4 text-right font-semibold text-foreground">
        {formatMoney(period.closingBalance, currency)}
      </TableCell>
    </TableRow>
  );
}
