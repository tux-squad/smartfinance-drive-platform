/**
 * Usuario autenticado, expresado en terminos del dominio.
 * No expone detalles de Supabase ni de ningun proveedor concreto.
 */
export interface AuthUser {
  id: string;
  email: string;
}
