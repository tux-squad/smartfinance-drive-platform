"use client";

import { useId, useMemo, useState } from "react";

import type { PaymentSchedulePeriod } from "@/domain/entities/payment-schedule";

/**
 * Gráfico de amortización (SVG puro, sin dependencias, tema-aware).
 *
 * Dos "small multiples" que comparten el eje de períodos:
 *   1. Composición de la cuota: área apilada Interés (baja) + Amortización (sube).
 *   2. Saldo pendiente: área/línea que decrece hasta cero.
 *
 * Paleta categórica validada (regla de relieve cubierta por la tabla + leyenda):
 *   Amortización = aqua, Interés = naranja. Colores por rol vía CSS custom props,
 *   con variante clara/oscura.
 */

interface AmortizationChartProps {
  periods: PaymentSchedulePeriod[];
  currency: "PEN" | "USD";
  labels: {
    compositionTitle: string;
    balanceTitle: string;
    principal: string;
    interest: string;
    balance: string;
    period: string;
  };
  locale: string;
}

const W = 760;
const H = 240;
const PAD = { top: 16, right: 16, bottom: 28, left: 56 };
const PLOT_W = W - PAD.left - PAD.right;
const PLOT_H = H - PAD.top - PAD.bottom;

function money(value: number, currency: "PEN" | "USD"): string {
  return new Intl.NumberFormat("en-US", {
    style: "currency",
    currency,
    maximumFractionDigits: 0,
  }).format(value);
}

function moneyExact(value: number, currency: "PEN" | "USD"): string {
  return new Intl.NumberFormat("en-US", {
    style: "currency",
    currency,
    maximumFractionDigits: 2,
    minimumFractionDigits: 2,
  }).format(value);
}

export function AmortizationChart({
  periods,
  currency,
  labels,
}: AmortizationChartProps) {
  const gradId = useId().replace(/:/g, "");
  const [hover, setHover] = useState<number | null>(null);

  const model = useMemo(() => {
    const n = periods.length;
    if (n === 0) {
      return null;
    }

    const x = (i: number) =>
      PAD.left + (n === 1 ? PLOT_W / 2 : (i / (n - 1)) * PLOT_W);

    const maxStack = Math.max(
      ...periods.map((p) => p.interest + p.principal),
      1,
    );
    const maxBalance = Math.max(...periods.map((p) => p.openingBalance), 1);

    const yStack = (v: number) => PAD.top + PLOT_H - (v / maxStack) * PLOT_H;
    const yBal = (v: number) => PAD.top + PLOT_H - (v / maxBalance) * PLOT_H;

    // Área inferior: interés (0 → interés). Área superior: amortización apilada.
    const interestTop = periods.map((p, i) => `${x(i)},${yStack(p.interest)}`);
    const principalTop = periods.map(
      (p, i) => `${x(i)},${yStack(p.interest + p.principal)}`,
    );
    const baseline = PAD.top + PLOT_H;

    const interestArea = `M ${x(0)},${baseline} L ${interestTop.join(
      " L ",
    )} L ${x(n - 1)},${baseline} Z`;
    const principalArea = `M ${interestTop
      .join(" L ")} L ${[...principalTop].reverse().join(" L ")} Z`;

    const balanceLine = periods
      .map((p, i) => `${x(i)},${yBal(p.openingBalance)}`)
      .join(" L ");
    const balanceArea = `M ${x(0)},${baseline} L ${balanceLine} L ${x(
      n - 1,
    )},${baseline} Z`;

    return {
      n,
      x,
      yStack,
      yBal,
      baseline,
      maxStack,
      maxBalance,
      interestArea,
      principalArea,
      balanceLine,
      balanceArea,
    };
  }, [periods]);

  if (!model) {
    return null;
  }

  const active = hover != null ? periods[hover] : null;

  const pointCount = model.n;
  function handleMove(event: React.MouseEvent<SVGSVGElement>) {
    const rect = event.currentTarget.getBoundingClientRect();
    const mx = ((event.clientX - rect.left) / rect.width) * W;
    const ratio = Math.max(0, Math.min(1, (mx - PAD.left) / PLOT_W));
    setHover(Math.round(ratio * (pointCount - 1)));
  }

  const tickIdx = [0, Math.floor(model.n / 3), Math.floor((2 * model.n) / 3), model.n - 1]
    .filter((v, i, arr) => arr.indexOf(v) === i);

  return (
    <div className="amort-chart grid gap-4 lg:grid-cols-2">
      <style>{`
        .amort-chart {
          --series-principal: #1baf7a;
          --series-interest: #eb6834;
          --series-balance: #2a78d6;
          --grid: #e1e0d9;
          --axis-ink: #898781;
        }
        :root.dark .amort-chart {
          --series-principal: #199e70;
          --series-interest: #d95926;
          --series-balance: #3987e5;
          --grid: #2c2c2a;
          --axis-ink: #898781;
        }
      `}</style>

      {/* Composición de la cuota */}
      <figure className="m-0 rounded-xl border border-border bg-card p-4">
        <figcaption className="mb-2 flex items-center justify-between">
          <span className="text-sm font-semibold text-foreground">
            {labels.compositionTitle}
          </span>
          <span className="flex items-center gap-3 text-xs text-muted-foreground">
            <span className="inline-flex items-center gap-1.5">
              <span
                className="size-2.5 rounded-sm"
                style={{ background: "var(--series-principal)" }}
              />
              {labels.principal}
            </span>
            <span className="inline-flex items-center gap-1.5">
              <span
                className="size-2.5 rounded-sm"
                style={{ background: "var(--series-interest)" }}
              />
              {labels.interest}
            </span>
          </span>
        </figcaption>
        <svg
          viewBox={`0 0 ${W} ${H}`}
          className="w-full"
          role="img"
          onMouseMove={handleMove}
          onMouseLeave={() => setHover(null)}
        >
          {[0.25, 0.5, 0.75, 1].map((g) => (
            <line
              key={g}
              x1={PAD.left}
              x2={W - PAD.right}
              y1={PAD.top + PLOT_H - g * PLOT_H}
              y2={PAD.top + PLOT_H - g * PLOT_H}
              stroke="var(--grid)"
              strokeWidth={1}
            />
          ))}
          <path d={model.interestArea} fill="var(--series-interest)" opacity={0.9} />
          <path
            d={model.principalArea}
            fill="var(--series-principal)"
            opacity={0.9}
          />
          {/* separador 2px entre segmentos apilados */}
          <polyline
            points={periods
              .map((p, i) => `${model.x(i)},${model.yStack(p.interest)}`)
              .join(" ")}
            fill="none"
            stroke="var(--card)"
            strokeWidth={2}
          />
          {active && hover != null ? (
            <g>
              <line
                x1={model.x(hover)}
                x2={model.x(hover)}
                y1={PAD.top}
                y2={model.baseline}
                stroke="var(--axis-ink)"
                strokeWidth={1}
                strokeDasharray="3 3"
              />
              <circle
                cx={model.x(hover)}
                cy={model.yStack(active.interest + active.principal)}
                r={3.5}
                fill="var(--series-principal)"
                stroke="var(--card)"
                strokeWidth={2}
              />
            </g>
          ) : null}
          {tickIdx.map((i) => (
            <text
              key={i}
              x={model.x(i)}
              y={H - 8}
              textAnchor="middle"
              className="fill-[var(--axis-ink)]"
              style={{ fontSize: 11 }}
            >
              {periods[i].periodNumber}
            </text>
          ))}
        </svg>
      </figure>

      {/* Saldo pendiente */}
      <figure className="m-0 rounded-xl border border-border bg-card p-4">
        <figcaption className="mb-2 flex items-center justify-between">
          <span className="text-sm font-semibold text-foreground">
            {labels.balanceTitle}
          </span>
          <span className="inline-flex items-center gap-1.5 text-xs text-muted-foreground">
            <span
              className="size-2.5 rounded-sm"
              style={{ background: "var(--series-balance)" }}
            />
            {labels.balance}
          </span>
        </figcaption>
        <svg
          viewBox={`0 0 ${W} ${H}`}
          className="w-full"
          role="img"
          onMouseMove={handleMove}
          onMouseLeave={() => setHover(null)}
        >
          <defs>
            <linearGradient id={`bal-${gradId}`} x1="0" x2="0" y1="0" y2="1">
              <stop
                offset="0%"
                stopColor="var(--series-balance)"
                stopOpacity={0.35}
              />
              <stop
                offset="100%"
                stopColor="var(--series-balance)"
                stopOpacity={0.02}
              />
            </linearGradient>
          </defs>
          {[0.25, 0.5, 0.75, 1].map((g) => (
            <line
              key={g}
              x1={PAD.left}
              x2={W - PAD.right}
              y1={PAD.top + PLOT_H - g * PLOT_H}
              y2={PAD.top + PLOT_H - g * PLOT_H}
              stroke="var(--grid)"
              strokeWidth={1}
            />
          ))}
          <path d={model.balanceArea} fill={`url(#bal-${gradId})`} />
          <polyline
            points={model.balanceLine}
            fill="none"
            stroke="var(--series-balance)"
            strokeWidth={2}
          />
          {active && hover != null ? (
            <g>
              <line
                x1={model.x(hover)}
                x2={model.x(hover)}
                y1={PAD.top}
                y2={model.baseline}
                stroke="var(--axis-ink)"
                strokeWidth={1}
                strokeDasharray="3 3"
              />
              <circle
                cx={model.x(hover)}
                cy={model.yBal(active.openingBalance)}
                r={3.5}
                fill="var(--series-balance)"
                stroke="var(--card)"
                strokeWidth={2}
              />
            </g>
          ) : null}
          {tickIdx.map((i) => (
            <text
              key={i}
              x={model.x(i)}
              y={H - 8}
              textAnchor="middle"
              className="fill-[var(--axis-ink)]"
              style={{ fontSize: 11 }}
            >
              {periods[i].periodNumber}
            </text>
          ))}
        </svg>
      </figure>

      {/* Tooltip compartido */}
      {active ? (
        <div className="pointer-events-none lg:col-span-2 rounded-lg border border-border bg-popover px-4 py-2 text-xs shadow-sm">
          <span className="font-semibold text-foreground">
            {labels.period} {active.periodNumber}
          </span>
          <span className="ml-3" style={{ color: "var(--series-interest)" }}>
            {labels.interest}: {moneyExact(active.interest, currency)}
          </span>
          <span className="ml-3" style={{ color: "var(--series-principal)" }}>
            {labels.principal}: {moneyExact(active.principal, currency)}
          </span>
          <span className="ml-3" style={{ color: "var(--series-balance)" }}>
            {labels.balance}: {money(active.openingBalance, currency)}
          </span>
        </div>
      ) : null}
    </div>
  );
}
