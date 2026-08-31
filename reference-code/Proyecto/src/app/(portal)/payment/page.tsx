import { listCurrentSimulations } from "@/application/use-cases/credit/list-current-simulation";
import { getPaymentOverview } from "@/application/use-cases/payment/get-payment-overview";
import { createSupabaseCreditRepository } from "@/infrastructure/repositories/supabase-credit-simulation-repository";
import { createSupabasePaymentScheduleRepository } from "@/infrastructure/repositories/supabase-payment-schedule-repository";
import { PaymentScheduleView } from "@/presentation/views/payment-schedule-view";

interface PaymentPageProps {
  searchParams: Promise<{ sim?: string }>;
}

export default async function PaymentPage({ searchParams }: PaymentPageProps) {
  const { sim } = await searchParams;

  // Lista de simulaciones para el selector + cronograma de la seleccionada
  // (o la más reciente si no se indica ninguna).
  const simulations = await listCurrentSimulations(
    createSupabaseCreditRepository(),
  );

  const selectedId =
    sim && simulations.some((item) => item.id === sim)
      ? sim
      : simulations[0]?.id;

  const overview = await getPaymentOverview(
    createSupabasePaymentScheduleRepository(),
    selectedId,
  );

  return (
    <PaymentScheduleView
      overview={overview}
      simulations={simulations.map((item) => ({
        id: item.id,
        label: item.vehicleLabel,
        date: item.simulatedAt,
      }))}
      selectedId={selectedId ?? null}
    />
  );
}
