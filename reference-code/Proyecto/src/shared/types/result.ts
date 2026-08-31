/**
 * Resultado explicito para casos de uso: evita lanzar excepciones a traves de
 * las capas y obliga a manejar el error en el adaptador.
 */
export type Result<T, E = string> =
  | { ok: true; value: T }
  | { ok: false; error: E };
