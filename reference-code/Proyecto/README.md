# Autify

Aplicacion fullstack con Next.js para construir herramientas de gestion
financiera. El frontend y el backend viven en el mismo proyecto, separados por
capas de Clean Architecture.

## Stack

- Next.js 16 con App Router
- React 19
- TypeScript
- Tailwind CSS 4
- Supabase (Postgres + Auth + API)
- ESLint
- pnpm

## Requisitos

- Node.js 20 o superior
- pnpm 11 o superior (recomendado activarlo con `corepack enable`)

## Instalacion

```bash
pnpm install
```

## Variables de entorno

Copia la plantilla y rellena los valores desde
Supabase -> Project Settings -> API:

```bash
cp .env.example .env.local
```

## Desarrollo

```bash
pnpm dev
```

Abrir [http://localhost:3000](http://localhost:3000).

## Scripts

```bash
pnpm dev
pnpm lint
pnpm build
pnpm start
```

## Estructura

```text
src/
  app/              Rutas Next.js, layouts, paginas y endpoints HTTP
  application/      Casos de uso de la aplicacion
  domain/           Entidades, contratos y reglas de negocio
  infrastructure/   Adaptadores, servicios externos y persistencia
  presentation/     Componentes y vistas de interfaz
  shared/           Configuracion, tipos, errores y utilidades comunes
```

## Backend

El backend se implementa con Route Handlers de Next.js en `src/app/api`.
Las rutas HTTP no deben contener reglas de negocio; deben delegar en casos de
uso dentro de `src/application`.

Endpoint inicial:

```http
GET /api/health
```

Respuesta:

```json
{
  "status": "ok",
  "service": "app-finanzas"
}
```

## Contexto para IA

Las reglas para colaboradores y agentes de IA estan en `AGENTS.md`.
