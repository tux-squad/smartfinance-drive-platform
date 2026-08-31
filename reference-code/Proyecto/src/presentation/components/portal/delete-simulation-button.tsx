"use client";

import { Trash2 } from "lucide-react";
import { useActionState } from "react";

import type { DeleteSimulationState } from "@/app/(portal)/simulations/actions";
import { Button } from "@/presentation/components/ui/button";
import {
  Dialog,
  DialogClose,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/presentation/components/ui/dialog";
import { useLanguage } from "@/presentation/i18n/language-context";
import { portalDictionary } from "@/presentation/i18n/portal-dictionary";

const initialState: DeleteSimulationState = { error: null, success: false };

interface DeleteSimulationButtonProps {
  simulationId: string;
  action: (
    prevState: DeleteSimulationState,
    formData: FormData,
  ) => Promise<DeleteSimulationState>;
}

export function DeleteSimulationButton({
  simulationId,
  action,
}: DeleteSimulationButtonProps) {
  const { lang } = useLanguage();
  const s = portalDictionary[lang].simulations;
  const [state, formAction, isPending] = useActionState(action, initialState);

  // El diálogo no se controla por estado: al eliminar con éxito, la revalidación
  // desmonta esta fila del acordeón y el diálogo desaparece de forma natural.
  return (
    <Dialog>
      <DialogTrigger asChild>
        <Button variant="outline" size="sm" className="text-destructive">
          <Trash2 className="size-4" />
          {s.delete}
        </Button>
      </DialogTrigger>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>{s.deleteTitle}</DialogTitle>
          <DialogDescription>{s.deleteMessage}</DialogDescription>
        </DialogHeader>
        {state.error ? (
          <p className="rounded-md bg-destructive/10 px-3 py-2 text-sm text-destructive">
            {state.error}
          </p>
        ) : null}
        <DialogFooter>
          <DialogClose asChild>
            <Button variant="outline">{s.cancel}</Button>
          </DialogClose>
          <form action={formAction}>
            <input type="hidden" name="simulationId" value={simulationId} />
            <Button type="submit" variant="destructive" disabled={isPending}>
              {isPending ? s.deleting : s.confirmDelete}
            </Button>
          </form>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
