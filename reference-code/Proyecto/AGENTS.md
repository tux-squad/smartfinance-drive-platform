# AGENTS.md

## Contexto del proyecto

Autify es una aplicacion fullstack construida con Next.js, TypeScript,
Tailwind CSS y pnpm. La persistencia y la autenticacion se apoyan en Supabase
(Postgres). Frontend y backend viven en la misma carpeta para mantener un flujo
simple de desarrollo y despliegue.

El gestor de paquetes es pnpm (no usar npm ni yarn). La version esta fijada con
el campo `packageManager` del `package.json`; activala con `corepack enable`.
pnpm bloquea los scripts de postinstall por defecto: los paquetes de confianza
que si los necesitan se declaran en `pnpm.onlyBuiltDependencies`.

Las variables de entorno viven en `.env.local` (ignorado por git). La plantilla
es `.env.example`. Los clientes de Supabase estan en
`src/infrastructure/services/supabase` y la validacion de entorno en
`src/shared/config/env.ts`. La `service_role` key es secreta y solo puede usarse
en el servidor.

Antes de modificar codigo de Next.js, revisa la documentacion local instalada
cuando aplique: `node_modules/next/dist/docs/`. Esta version puede tener APIs o
convenciones distintas a versiones anteriores.

## Arquitectura

El proyecto usa Clean Architecture. Mantener estas responsabilidades:

- `src/app`: App Router de Next.js, paginas, layouts y route handlers HTTP.
- `src/presentation`: vistas y componentes de UI.
- `src/application`: casos de uso y orquestacion de reglas.
- `src/domain`: entidades, value objects, contratos y reglas de negocio puras.
- `src/infrastructure`: implementaciones tecnicas como repositorios, clientes y servicios externos.
- `src/shared`: configuracion, tipos, errores y utilidades transversales.

Reglas principales:

- Las rutas en `src/app/api` deben ser adaptadores HTTP delgados.
- No poner reglas de negocio en componentes, paginas o route handlers.
- Los casos de uso pueden depender de `domain` y `shared`.
- `domain` no debe depender de Next.js, React, infraestructura ni APIs externas.
- `infrastructure` implementa detalles tecnicos y debe poder reemplazarse sin cambiar el dominio.
- Usar el alias `@/*` para imports desde `src`.

## Buenas practicas

- Preferir TypeScript estricto y tipos explicitos en contratos publicos.
- Mantener componentes pequenos y enfocados.
- Validar entradas en la capa que recibe datos externos antes de ejecutar reglas de negocio.
- Evitar duplicar reglas financieras; moverlas a `domain` o `application`.
- Mantener los nombres de archivos en kebab-case.
- Ejecutar `pnpm lint` y `pnpm build` antes de abrir un PR cuando sea posible.

## Commits

Usar Conventional Commits. Formato:

```text
<type>(optional-scope): <description>
```

Tipos permitidos:

- `feat`: nueva funcionalidad.
- `fix`: correccion de bug.
- `docs`: documentacion.
- `style`: cambios de formato sin alterar comportamiento.
- `refactor`: reestructura sin cambiar comportamiento.
- `test`: pruebas.
- `chore`: mantenimiento, tooling o dependencias.
- `build`: cambios de build o empaquetado.
- `ci`: cambios de integracion continua.

Buenas reglas:

- Usar imperativo y descripcion corta en ingles o espanol, pero consistente por PR.
- No terminar el subject con punto.
- Incluir scope cuando ayude a ubicar el cambio: `feat(api): add health check`.
- Separar cambios no relacionados en commits distintos.

Ejemplos validos:

```text
feat: add transaction summary view
fix: correct balance calculation
docs: update onboarding instructions
refactor: move account logic to use case
test: add health endpoint coverage
chore: update dependencies
```

## Comandos utiles

```bash
pnpm dev
pnpm lint
pnpm build
pnpm start
```
