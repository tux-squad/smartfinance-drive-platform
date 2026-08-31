# Análisis de Cumplimiento — Enunciado vs. Sistema

> Revisión del proyecto (Next.js + Supabase, Clean Architecture) contra el
> enunciado del Trabajo Final de *Finanzas e Ingeniería Económica (1ASI0642)*.
> Fecha de revisión: 2026-07-07 · Rama: `develop`.

---

## 1. Resumen ejecutivo

El proyecto tiene una **arquitectura sólida y bien organizada** (dominio,
aplicación, infraestructura, presentación) y una **base de datos completa** que
modela clientes, vehículos, entidades financieras, simulaciones y cronogramas.
Las **fórmulas financieras están correctamente implementadas y centralizadas**
en `src/domain/services/credit-calculator.ts`.

> **ACTUALIZACIÓN (2026-07-07): el núcleo del enunciado ya está implementado.**
> Se conectaron las fórmulas al flujo real: al guardar una simulación se
> **genera el cronograma francés completo** (con gracia total/parcial y cuota
> balón), se calculan **cuota, VAN, TIR y TCEA reales** y se persisten la
> cabecera **y todas las filas del cronograma**. Se eliminaron la TCEA mock y la
> cuota hardcodeada, y se añadió el **selector de tasa efectiva/nominal +
> capitalización** y una **vista previa en tiempo real** que usa el mismo motor.
> Ver §6 "Cambios implementados". Queda pendiente la **edición** de registros
> ya creados (requisito 3).

| Estado | Significado |
|--------|-------------|
| ✅ | Cumple |
| ⚠️ | Parcial / requiere ajuste |
| ❌ | No cumple / falta |

---

## 2. Cumplimiento punto por punto

### Requisitos funcionales del enunciado

| # | Requisito | Estado | Comentario |
|---|-----------|--------|------------|
| 1 | Login y password obligatorio | ✅ | Auth con Supabase (`(auth)/login`, `signup`), middleware de sesión. |
| 2 | Alta de datos del cliente y del vehículo | ✅ | Perfil (`settings`) y registro de vehículos (`vehicles`) persistidos en BD. |
| 3 | Editar/modificar y volver a guardar | ⚠️ | Perfil editable. **Pendiente**: edición de vehículos/simulaciones ya creados. |
| 4 | Método **francés vencido ordinario** (meses 30 días) | ✅ | `buildFinancingPlan` genera el cronograma real y se persiste. Validado con casos conocidos. |
| 5 | Indistinto **Soles / Dólares** | ✅ | Moneda modelada en `vehicles`/`simulations`; fórmulas son moneda-agnósticas. |
| 6 | Tasas **nominales o efectivas** (+ capitalización) | ✅ | Selector efectiva/nominal + capitalización en la UI, convertido a TEM en el dominio. |
| 7 | **Períodos de gracia total o parcial** | ✅ | Gracia total (capitaliza interés) y parcial (paga solo interés) aplicadas en el cronograma. |
| 8 | Cálculo de **VAN y TIR** (óptica del deudor) | ✅ | Calculados con el flujo de caja real del deudor y persistidos. |
| 9 | Indicadores de **transparencia** (TCEA, TEA, TEM) | ✅ | TCEA real por TIR anualizada, incluye seguros/comisiones. Sin mocks. |
| 10 | Opciones de **configuración** (moneda, tipo de tasa, gracia) | ✅ | La configuración ahora impacta directamente el cálculo y el cronograma. |
| 11 | Registrar **todas las operaciones** en BD | ✅ | Se guardan la simulación **y** todas las filas del cronograma (con rollback si falla). |

---

## 3. Hallazgos críticos (con ubicación exacta)

> **Estado:** C1–C5 **RESUELTOS** (ver §6). Se dejan documentados como registro
> del diagnóstico original.

### 🔴 C1 — El cronograma de pagos nunca se genera
La tabla `payment_schedules` existe, tiene RLS y se **lee** en
`supabase-payment-schedule-repository.ts` y `supabase-financial-metric-repository.ts`,
pero **no hay ningún `INSERT`** en ningún punto del código. El plan de pagos por
método francés —corazón del trabajo— no se produce.
- **Dónde debería ir:** al crear la simulación, en
  `create-credit-simulation.ts` / `supabase-credit-simulation-repository.ts`.

### 🔴 C2 — TCEA y cuota mensual son ficticias
`src/presentation/views/credit-configuration-view.tsx`:
- Línea ~79: `const mockCalculatedTcea = (Number(interestRate) * 1.15)...` → TCEA inventada.
- Línea ~350: `{currencySymbol}1,345.80` → **cuota mensual hardcodeada** en el HTML.

Estos valores mock son los que terminan guardándose en la BD.

### 🔴 C3 — Las fórmulas centralizadas están casi sin usar
De `credit-calculator.ts`, solo `annualizeMonthlyRate` se invoca (en
`get-financial-metrics-overview.ts`). Las funciones núcleo
(`calculateFrenchMonthlyPayment`, `calculateNetPresentValue`,
`calculateInternalRateOfReturn`, `toEffectiveMonthlyRate`,
`calculateSmartPurchasePrincipal`) **no se llaman en el flujo de guardado**.

### 🟠 C4 — Períodos de gracia sin implementar
`create-credit-simulation.ts` valida `gracePeriod` e `isCapital`, y se persisten,
pero no existe lógica que:
- **Gracia total:** no cobre cuota y capitalice el interés al saldo.
- **Gracia parcial:** cobre solo el interés del período.

### 🟠 C5 — Falta el selector de tipo de tasa (nominal/efectiva)
El enunciado lo pide explícitamente ("si es efectiva o nominal... definir la
capitalización"). La UI solo captura una TEA en `credit-configuration-view.tsx`.
El dominio ya lo soporta vía `AnnualRateInput`.

### 🟡 C6 — Convención VAN vs TIR (documentada, ojo al usarla)
`calculateNetPresentValue` toma el flujo inicial aparte (los flujos empiezan en
t=1); `calculateInternalRateOfReturn` espera el flujo inicial **dentro** del
arreglo (t=0). Es correcto, pero hay que respetar cada convención al llamar.
Ya quedó documentado en el código.

---

## 4. Lo que SÍ está bien (para destacar en la exposición)

- ✅ **Clean Architecture** real: dominio puro sin dependencias, casos de uso,
  repositorios por interfaz, infraestructura Supabase intercambiable.
- ✅ **Fórmulas correctas** y centralizadas en un solo archivo (fácil de auditar
  y de cambiar si el profesor lo pide).
- ✅ **Modelo de datos completo y normalizado** con RLS por usuario, benchmarks
  de tasas por entidad financiera (transparencia), e índices adecuados.
- ✅ **Autenticación** robusta y **i18n ES/EN**.
- ✅ Cronograma e indicadores ya tienen **UI y tipos** listos para recibir datos
  reales: solo falta alimentarlos.

---

## 5. Plan de corrección — estado

1. ✅ **`buildFinancingPlan(...)`** en `src/domain/services/financing-plan-builder.ts`:
   arma el cronograma (gracia total/parcial + balón) y calcula cuota, VAN, TIR y TCEA.
2. ✅ **Cableado en el guardado**: `create-credit-simulation` construye el plan y el
   repositorio inserta la simulación **y** sus `payment_schedules` (con rollback).
3. ✅ **Mocks eliminados** en `credit-configuration-view.tsx`; se muestran los valores
   reales y una **vista previa en tiempo real**.
4. ✅ **Selector nominal/efectiva + capitalización** en la UI, pasado al dominio.
5. ⬜ **Edición** de vehículos/simulaciones (requisito 3) — pendiente.
6. ⬜ **2 juegos de datos de prueba** (informe 6.d) — pendiente (documentación).

---

## 6. Cambios implementados (2026-07-07)

**Motor y flujo de cálculo**
- `src/domain/services/financing-plan-builder.ts` (nuevo): construye el cronograma
  francés vencido ordinario con gracia **total** (capitaliza interés) y **parcial**
  (paga solo interés), **cuota balón** diferida, y calcula **VAN, TIR y TCEA** desde
  la óptica del deudor incluyendo **seguro de desgravamen y comisiones** (transparencia).
- `src/shared/config/financing.ts` (nuevo): parámetros por defecto (COK, seguro,
  capitalizaciones) centralizados.
- `create-credit-simulation.ts`: ahora valida y **orquesta** el cálculo del plan.
- `supabase-credit-simulation-repository.ts`: inserta cabecera **+ cronograma**; si
  falla el cronograma, borra la cabecera para no dejar datos huérfanos.
- `credit/actions.ts`: lee **precio y moneda autoritativos** del vehículo (no del
  cliente) y convierte los porcentajes de la UI a montos.

**Interfaz**
- `credit-configuration-view.tsx`: se eliminaron la **TCEA mock** (`TEA×1.15`) y la
  **cuota hardcodeada** (`S/1,345.80`). Se agregó **vista previa en tiempo real**
  (VAN, TIR, TCEA, cuota) usando el mismo motor de dominio, más selector de
  **tasa efectiva/nominal + capitalización** y campo de **seguro desgravamen**.

**Verificación**
- Matemática validada contra casos conocidos (cuota francesa clásica = 888.49,
  balón embebida, gracia total/parcial, saldo final 0).
- `tsc --noEmit` sin errores · `eslint` sin errores · `next build` exitoso.

**Innovación (sin romper el sistema)**
- Vista previa en vivo con el motor real (no otra implementación paralela).
- Seguro de desgravamen incorporado a la TCEA para cumplir transparencia SBS.
- Exportación a Excel/PDF del cronograma ya existía y ahora opera sobre datos reales.

### Pendiente para "todo el enunciado"
- **Edición** de vehículos y simulaciones registrados (requisito 3).
- Redacción de los **2 juegos de datos de prueba** en el informe.
