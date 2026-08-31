import { redirect } from "next/navigation";

/**
 * La sección "Métricas financieras" se integró dentro de cada simulación.
 * Esta ruta se conserva por compatibilidad y redirige a Simulaciones.
 */
export default function FinancialMetricsPage() {
  redirect("/simulations");
}
