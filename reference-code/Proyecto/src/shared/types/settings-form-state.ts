/**
 * Estado del formulario de Settings, consumido con `useActionState`.
 * A diferencia de auth, expone tambien `success` para mostrar confirmacion.
 */
export interface SettingsFormState {
  error: string | null;
  success: boolean;
}

export type SettingsFormAction = (
  prevState: SettingsFormState,
  formData: FormData,
) => Promise<SettingsFormState>;
