"use client";

import {
  BarChart3,
  Download,
  Landmark,
  Lightbulb,
  Percent,
  TrendingUp,
} from "lucide-react";

import type { FinancialMetricsOverview } from "@/domain/entities/financial-metric";

type FinancialMetricsViewProps = {
  overview: FinancialMetricsOverview;
};

function formatMoney(value: number, currency: "PEN" | "USD"): string {
  return new Intl.NumberFormat("en-US", {
    currency: currency === "USD" ? "USD" : "PEN",
    maximumFractionDigits: 2,
    minimumFractionDigits: 2,
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

function escapeHtml(value: string): string {
  return value
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");
}

function MetricCard({
  helper,
  label,
  value,
}: {
  helper: string;
  label: string;
  value: string;
}) {
  return (
    <article className="rounded-lg border border-slate-200 bg-white p-5 shadow-sm">
      <p className="text-xs font-bold uppercase text-slate-600">{label}</p>
      <p className="mt-4 text-2xl font-bold text-slate-950">{value}</p>
      <p className="mt-2 text-sm text-slate-600">{helper}</p>
    </article>
  );
}

export function FinancialMetricsView({ overview }: FinancialMetricsViewProps) {
  const maxCashFlow = Math.max(
    1,
    ...overview.quarterlyCashFlows.map((flow) => flow.value),
  );
  const maxRate = Math.max(
    0.01,
    ...overview.rateComparisons.map((rate) => rate.value),
  );
  const vanProgress = Math.min(100, Math.max(8, Math.abs(overview.van) / 200));
  const tirMessage =
    overview.tirExcess >= 0
      ? `Exceeds baseline threshold by ${formatPercent(overview.tirExcess)}`
      : `Below baseline threshold by ${formatPercent(
          Math.abs(overview.tirExcess),
        )}`;

  function exportPdf() {
    const printWindow = window.open("", "_blank");

    if (!printWindow) {
      return;
    }

    const interpretationHtml = overview.interpretation
      .map((paragraph) => `<p>${escapeHtml(paragraph)}</p>`)
      .join("");

    const ratesHtml = overview.rateComparisons
      .map(
        (rate) => `
          <tr>
            <td>${escapeHtml(rate.label)}</td>
            <td>${escapeHtml(formatPercent(rate.value))}</td>
          </tr>
        `,
      )
      .join("");

    const cashFlowsHtml = overview.quarterlyCashFlows
      .map(
        (flow) => `
          <tr>
            <td>${escapeHtml(flow.label)}</td>
            <td>${escapeHtml(formatMoney(flow.value, overview.currency))}</td>
          </tr>
        `,
      )
      .join("");

    printWindow.document.write(`
      <html>
        <head>
          <title>Financial Indicators</title>
          <style>
            body {
              font-family: Arial, sans-serif;
              padding: 32px;
              color: #020617;
            }

            h1 {
              margin: 0 0 8px;
              font-size: 28px;
            }

            h2 {
              margin-top: 28px;
              font-size: 18px;
            }

            p {
              color: #334155;
              line-height: 1.6;
            }

            .grid {
              display: grid;
              grid-template-columns: repeat(3, 1fr);
              gap: 12px;
              margin-top: 24px;
            }

            .card {
              border: 1px solid #cbd5e1;
              padding: 16px;
            }

            .label {
              color: #475569;
              font-size: 11px;
              font-weight: 700;
              text-transform: uppercase;
            }

            .value {
              margin-top: 8px;
              color: #020617;
              font-size: 24px;
              font-weight: 700;
            }

            table {
              width: 100%;
              border-collapse: collapse;
              margin-top: 12px;
              font-size: 13px;
            }

            th {
              background: #020617;
              color: white;
              padding: 8px;
              text-align: left;
            }

            td {
              border-bottom: 1px solid #e2e8f0;
              padding: 8px;
            }
          </style>
        </head>
        <body>
          <h1>Financial Indicators</h1>
          <p>Simulation ID: ${escapeHtml(overview.simulationId ?? "--")}</p>

          <section class="grid">
            <div class="card">
              <div class="label">VAN</div>
              <div class="value">${escapeHtml(
                formatMoney(overview.van, overview.currency),
              )}</div>
            </div>

            <div class="card">
              <div class="label">TIR mensual</div>
              <div class="value">${escapeHtml(
                formatPercent(overview.tir),
              )}</div>
            </div>

            <div class="card">
              <div class="label">TCEA anual</div>
              <div class="value">${escapeHtml(
                formatPercent(overview.tcea),
              )}</div>
            </div>

            <div class="card">
              <div class="label">TEA anual</div>
              <div class="value">${escapeHtml(
                formatPercent(overview.tea),
              )}</div>
            </div>

            <div class="card">
              <div class="label">TEM mensual</div>
              <div class="value">${escapeHtml(
                formatPercent(overview.tem),
              )}</div>
            </div>

            <div class="card">
              <div class="label">TIR base mensual</div>
              <div class="value">${escapeHtml(
                formatPercent(overview.baselineRate),
              )}</div>
            </div>
          </section>

          <h2>Cash Flow Projection</h2>
          <table>
            <thead>
              <tr>
                <th>Quarter</th>
                <th>Cash Flow</th>
              </tr>
            </thead>
            <tbody>
              ${cashFlowsHtml}
            </tbody>
          </table>

          <h2>Rate Comparison Matrix</h2>
          <table>
            <thead>
              <tr>
                <th>Indicator</th>
                <th>Rate</th>
              </tr>
            </thead>
            <tbody>
              ${ratesHtml}
            </tbody>
          </table>

          <h2>Financial Interpretation</h2>
          ${interpretationHtml}

          <script>
            window.print();
          </script>
        </body>
      </html>
    `);

    printWindow.document.close();
  }

  return (
    <div className="mx-auto flex w-full max-w-7xl flex-col gap-7 px-8 py-8">
      <header className="flex flex-col gap-5 md:flex-row md:items-start md:justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-normal text-slate-950">
            Financial Indicators
          </h1>
          <p className="mt-2 text-sm text-slate-600">
            Deep-dive analytics for simulation ID{" "}
            {overview.simulationId
              ? `#${overview.simulationId.slice(0, 6)}`
              : "--"}
          </p>
        </div>

        <button
          type="button"
          onClick={exportPdf}
          disabled={!overview.simulationId}
          className="inline-flex items-center justify-center gap-2 rounded-md border border-slate-300 bg-white px-4 py-2.5 text-xs font-bold text-slate-950 shadow-sm transition hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-50"
        >
          <Download className="h-4 w-4" />
          Export PDF
        </button>
      </header>

      <section className="grid gap-5 lg:grid-cols-[minmax(0,2fr)_minmax(260px,1fr)]">
        <article className="rounded-lg border border-slate-200 bg-white p-6 shadow-sm">
          <div className="flex items-start justify-between gap-4">
            <div>
              <p className="text-xs font-bold uppercase text-slate-600">
                Net Present Value (VAN)
              </p>
              <div className="mt-2 flex flex-wrap items-end gap-3">
                <p className="text-5xl font-bold tracking-normal text-slate-950">
                  {formatMoney(overview.van, overview.currency)}
                </p>
                <span
                  className={`rounded-md px-2 py-1 text-xs font-bold ${
                    overview.van >= 0
                      ? "bg-emerald-50 text-emerald-700"
                      : "bg-red-50 text-red-700"
                  }`}
                >
                  {overview.van >= 0 ? "+" : "-"}
                  {formatPercent(Math.abs(overview.van / 100000))}
                </span>
              </div>
            </div>

            <span className="flex h-10 w-10 items-center justify-center rounded-lg bg-slate-100 text-slate-900">
              <Landmark className="h-5 w-5" />
            </span>
          </div>

          <div className="mt-8 h-2 overflow-hidden rounded-full bg-slate-100">
            <div
              className="h-full bg-slate-950"
              style={{ width: `${vanProgress}%` }}
            />
          </div>
        </article>

        <article className="rounded-lg border border-slate-200 bg-white p-6 shadow-sm">
          <div className="flex items-start justify-between gap-4">
            <div>
              <p className="text-xs font-bold uppercase text-slate-600">
                Internal Rate of Return (TIR)
              </p>
              <p className="mt-3 text-3xl font-bold text-slate-950">
                {formatPercent(overview.tir)}
              </p>
            </div>
            <span className="flex h-10 w-10 items-center justify-center rounded-lg bg-emerald-50 text-emerald-700">
              <Percent className="h-5 w-5" />
            </span>
          </div>
          <p className="mt-8 text-sm text-slate-600">{tirMessage}</p>
        </article>
      </section>

      <section className="grid gap-5 md:grid-cols-3">
        <MetricCard
          helper="Annual Cost Effective"
          label="TCEA"
          value={formatPercent(overview.tcea)}
        />
        <MetricCard
          helper="Annual Effective Rate"
          label="TEA"
          value={formatPercent(overview.tea)}
        />
        <MetricCard
          helper="Monthly Effective Rate"
          label="TEM"
          value={formatPercent(overview.tem)}
        />
      </section>

      <section className="grid gap-5 lg:grid-cols-2">
        <article className="rounded-lg border border-slate-200 bg-white p-6 shadow-sm">
          <div className="flex items-center justify-between">
            <h2 className="text-xl font-bold text-slate-950">
              Cash Flow Projection
            </h2>
            <BarChart3 className="h-5 w-5 text-slate-500" />
          </div>

          <div className="mt-8 flex h-40 items-end gap-8 border-b border-slate-200 px-4">
            {overview.quarterlyCashFlows.map((flow) => (
              <div
                className="flex flex-1 flex-col items-center gap-2"
                key={flow.label}
              >
                <div
                  className="w-full max-w-10 rounded-t-sm bg-slate-950"
                  style={{
                    height: `${Math.max(
                      8,
                      (flow.value / maxCashFlow) * 120,
                    )}px`,
                  }}
                  title={formatMoney(flow.value, overview.currency)}
                />
                <span className="text-xs text-slate-600">{flow.label}</span>
              </div>
            ))}
          </div>
        </article>

        <article className="rounded-lg border border-slate-200 bg-white p-6 shadow-sm">
          <div className="flex items-center justify-between">
            <h2 className="text-xl font-bold text-slate-950">
              Rate Comparison Matrix
            </h2>
            <TrendingUp className="h-5 w-5 text-slate-500" />
          </div>

          <div className="mt-8 flex flex-col gap-6">
            {overview.rateComparisons.map((rate) => (
              <div
                className="grid grid-cols-[minmax(140px,180px)_minmax(0,1fr)_56px] items-center gap-3"
                key={rate.label}
              >
                <span className="text-xs font-bold uppercase text-slate-600">
                  {rate.label}
                </span>
                <div className="h-2 overflow-hidden rounded-full bg-slate-100">
                  <div
                    className={`h-full ${
                      rate.label === "Autify" ? "bg-emerald-600" : "bg-slate-950"
                    }`}
                    style={{
                      width: `${Math.max(4, (rate.value / maxRate) * 100)}%`,
                    }}
                  />
                </div>
                <span className="text-right text-xs font-medium text-slate-950">
                  {formatPercent(rate.value)}
                </span>
              </div>
            ))}
          </div>
        </article>
      </section>

      <section className="rounded-lg border border-slate-200 bg-white p-6 shadow-sm">
        <div className="flex items-center gap-3">
          <span className="flex h-9 w-9 items-center justify-center rounded-lg bg-slate-100 text-slate-600">
            <Lightbulb className="h-5 w-5" />
          </span>
          <h2 className="text-xl font-bold text-slate-950">
            Financial Interpretation
          </h2>
        </div>

        <div className="mt-5 space-y-4 text-sm leading-6 text-slate-700">
          {overview.interpretation.map((paragraph) => (
            <p key={paragraph}>{paragraph}</p>
          ))}
        </div>
      </section>
    </div>
  );
}
