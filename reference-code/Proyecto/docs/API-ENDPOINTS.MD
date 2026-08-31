# Documentación de Endpoints del Backend e Innovación Financiera

En Autify, el backend está desarrollado sobre **Next.js (App Router)**. Para la comunicación entre el frontend y el backend, el proyecto adopta dos enfoques de acuerdo con sus responsabilidades de Clean Architecture:

1. **Route Handlers Tradicionales (API HTTP):** Utilizados para integración con sistemas externos o chequeos de salud automatizados.
2. **Server Actions (RPC del Servidor):** Utilizados para el flujo interno del portal (autenticación, creación de simulaciones, perfil y vehículos), funcionando como endpoints asíncronos fuertemente tipados que se ejecutan directamente en el servidor.

---

## 1. API HTTP (Route Handlers Existentes)

### `GET /api/health`
* **Ubicación:** [`src/app/api/health/route.ts`](file:///home/aldo/Proyectos/Pruebas/Proyecto/src/app/api/health/route.ts)
* **Descripción:** Endpoint público que evalúa el estado del servicio web. Llama internamente al caso de uso `getHealthStatus`.
* **Respuesta Exitosa (JSON):**
  ```json
  {
    "status": "ok",
    "service": "app-finanzas"
  }
  ```

---

## 2. Server Actions (Existentes)

Los Server Actions son declarados con la directiva `"use server"`. Manejan los estados del formulario, interactúan con la capa de aplicación (Casos de Uso) y revalidan el caché de Next.js.

### A. Módulo de Autenticación

#### `loginAction`
* **Ubicación:** [`src/app/(auth)/login/actions.ts`](file:///home/aldo/Proyectos/Pruebas/Proyecto/src/app/(auth)/login/actions.ts)
* **Descripción:** Autentica a un usuario utilizando su correo electrónico y contraseña. Delega el proceso al caso de uso `signInWithPassword`.
* **Flujo:** Si la autenticación es exitosa, Supabase escribe las cookies de sesión en el navegador y el action redirige al usuario a `/dashboard`. En caso de error, retorna un código de error para ser renderizado en la interfaz.

#### `signupAction`
* **Ubicación:** [`src/app/(auth)/signup/actions.ts`](file:///home/aldo/Proyectos/Pruebas/Proyecto/src/app/(auth)/signup/actions.ts)
* **Descripción:** Registra una cuenta nueva de usuario mediante correo electrónico, contraseña y confirmación de contraseña, delegando en `signUpWithPassword`.
* **Flujo:** Al registrarse de forma exitosa (con la confirmación por email desactivada por defecto), el usuario inicia sesión inmediatamente y es redirigido a `/dashboard`. En caso contrario, retorna el código de error correspondiente.

#### `logoutAction`
* **Ubicación:** [`src/app/(portal)/actions.ts`](file:///home/aldo/Proyectos/Pruebas/Proyecto/src/app/(portal)/actions.ts)
* **Descripción:** Finaliza la sesión actual del usuario llamando al caso de uso `signOut` para revocar la sesión activa en el repositorio de autenticación de Supabase.
* **Flujo:** Borra las cookies de sesión y redirige al usuario a la página pública `/login`.

---

### B. Módulo de Simulación de Créditos

#### `createSimulationAction`
* **Ubicación:** [`src/app/(portal)/credit/actions.ts`](file:///home/aldo/Proyectos/Pruebas/Proyecto/src/app/(portal)/credit/actions.ts)
* **Descripción:** Recibe los parámetros de simulación (vehículo, cuotas inicial/balón en porcentaje, tasa, gracia, comisiones y seguros) y guarda un plan de pagos completo.
* **Flujo:** 
  1. Recupera el precio y moneda autoritativos del vehículo directamente de la base de datos (seguridad del servidor).
  2. Convierte los porcentajes de la interfaz a montos nominales.
  3. Ejecuta el caso de uso `createCreditSimulation`, el cual invoca el motor financiero (`buildFinancingPlan`) para generar todo el cronograma de cuotas y calcular VAN, TIR y TCEA.
  4. Guarda de forma atómica la simulación y las filas del cronograma (`payment_schedules`) en Postgres.
  5. Revalida la caché de `/credit`, `/dashboard`, `/payment` y `/financial-metrics` para reflejar los nuevos datos.

#### `deleteSimulationAction`
* **Ubicación:** [`src/app/(portal)/simulations/actions.ts`](file:///home/aldo/Proyectos/Pruebas/Proyecto/src/app/(portal)/simulations/actions.ts)
* **Descripción:** Elimina del sistema una simulación de crédito guardada identificada por su ID.
* **Flujo:** Llama al caso de uso `deleteSimulation`. Debido a la configuración de integridad referencial de la BD en Prisma/Postgres, al eliminar la simulación se eliminan en cascada todos sus periodos de pago asociados en `payment_schedules`. Revalida las páginas del portal.

---

### C. Módulo de Gestión de Vehículos

#### `saveVehicleAction`
* **Ubicación:** [`src/app/(portal)/vehicles/actions.ts`](file:///home/aldo/Proyectos/Pruebas/Proyecto/src/app/(portal)/vehicles/actions.ts)
* **Descripción:** Acción unificada que permite registrar un nuevo vehículo en el catálogo del usuario o actualizar la información de uno ya existente.
* **Flujo:** 
  * Si en el formulario se envía un `vehicleId` no vacío, invoca al caso de uso `updateVehicle` para modificar el registro.
  * Si no se envía un `vehicleId`, invoca a `createVehicle` para dar de alta el vehículo.
  * Al finalizar con éxito, revalida la caché de `/vehicles` y `/dashboard`.

#### `deleteVehicleAction`
* **Ubicación:** [`src/app/(portal)/vehicles/actions.ts`](file:///home/aldo/Proyectos/Pruebas/Proyecto/src/app/(portal)/vehicles/actions.ts)
* **Descripción:** Remueve un vehículo del catálogo del usuario según el ID provisto.
* **Flujo:** Invoca al caso de uso `deleteVehicle` y revalida las páginas `/vehicles` y `/dashboard` para actualizar los catálogos en el cliente.

---

### D. Módulo de Perfil de Usuario

#### `updateProfileAction`
* **Ubicación:** [`src/app/(portal)/settings/actions.ts`](file:///home/aldo/Proyectos/Pruebas/Proyecto/src/app/(portal)/settings/actions.ts)
* **Descripción:** Actualiza los datos de perfil y la información socioeconómica del cliente (identificación nacional, nombres completos, teléfono, ingresos mensuales, estado laboral).
* **Flujo:** Invoca el caso de uso `updateProfile` para persistir los cambios en la tabla `profiles` de Supabase. Revalida la caché de la página `/settings`.

---

## 3. Nuevos Endpoints Propuestos (Para un Escenario Real e Innovador)

Para transformar la aplicación en una plataforma robusta de clase de producción e innovadora, se deberían implementar los siguientes endpoints y Server Actions adicionales:

### A. Módulo de Cotizaciones Multi-Banco y Benchmarking Dinámico

#### `getMultiBankQuotesAction` (Server Action)
* **Ubicación Propuesta:** `src/app/(portal)/credit/actions-multi-bank.ts`
* **Descripción:** Recibe los datos del vehículo y perfil del cliente, y simula en paralelo el plan de pagos bajo las políticas de tasas activas de múltiples entidades financieras asociadas (ej. BCP, BBVA, Interbank).
* **Flujo:** 
  1. Consulta en BD los benchmarks e indicadores vigentes (`financial_entity_rate_benchmarks`) de cada banco.
  2. Ejecuta iterativamente `buildFinancingPlan` parametrizado con las condiciones de cada entidad.
  3. Devuelve un consolidado de cuotas, TCEA, VAN y TIR por banco para que la UI los muestre en un cuadro comparativo dinámico, permitiendo al usuario aplicar directamente al banco más conveniente.

---

### B. Módulo de Credit Scoring (Evaluación de Riesgo Automatizada)

#### `evaluateClientRiskAction` (Server Action)
* **Ubicación Propuesta:** `src/app/(portal)/settings/actions-scoring.ts`
* **Descripción:** Analiza la capacidad de pago del cliente y le asigna un puntaje de riesgo y una TEA personalizada.
* **Flujo:**
  1. Lee los ingresos declarados, la situación laboral y las deudas reportadas (simuladas o integradas con una API externa de buró de crédito).
  2. Calcula el Ratio de Cobertura de Deuda (Cuota Proyectada / Ingreso Mensual Neto).
  3. Devuelve una puntuación (ej. A, B, C) y una tasa de interés ajustada al riesgo (a menor riesgo, la acción activa una reducción automática en la TEA de la simulación).

---

### C. Módulo de Decisiones Inteligentes (Depreciación y VFMG)

#### `getVehicleFutureValueProjection` (Server Action/API HTTP)
* **Ubicación Propuesta:** `src/app/(portal)/simulations/actions-projections.ts`
* **Descripción:** Proyecta la devaluación comercial del vehículo a lo largo del tiempo para asesorar al cliente al final del plan de Compra Inteligente (cuota balón).
* **Flujo:**
  1. Recibe la marca, modelo y año del vehículo.
  2. Consulta un modelo predictivo de depreciación (o tabla histórica de precios de mercado secundario).
  3. Devuelve una curva de valor del auto a 2, 3 y 5 años. La compara contra la cuota balón simulada para sugerir si conviene devolver el auto, pagarlo para quedárselo, o venderlo de manera independiente al vencimiento.

---

### D. Módulo de Crédito Sostenible (Crédito Verde)

#### `applyEcoBonusAction` (Server Action)
* **Ubicación Propuesta:** `src/app/(portal)/credit/actions-green.ts`
* **Descripción:** Verifica si el vehículo seleccionado cuenta con una motorización ecológica (híbrido/eléctrico) para otorgar incentivos.
* **Flujo:**
  1. Verifica el registro del vehículo en BD.
  2. Si califica como ecológico, aplica el "Bono Verde" (subsidio en la cuota inicial o tasa reducida) y recalcula la TCEA con un descuento preferencial en el seguro vehicular.

---

## 4. Consideraciones Técnicas y Regulatorias SBS a Incorporar en el Core

Para que el backend actual refleje un entorno financiero real, los motores financieros y repositorios deben modificarse según estas directivas:

### A. Seguro Vehicular (Todo Riesgo) Financiado u Horizontales
* **Problema Actual:** Solo se modela el seguro de desgravamen (sobre el saldo).
* **Cambio Backend:** `buildFinancingPlan` en [`financing-plan-builder.ts`](file:///home/aldo/Proyectos/Pruebas/Proyecto/src/domain/services/financing-plan-builder.ts) debe incorporar un parámetro para el **seguro vehicular multirriesgo**. Debe poder configurarse como:
  * *Endosado:* Cobro 0 en la cuota (pago externo).
  * *Financiado:* Sumado al capital financiado inicial ($P_0$), incrementando el préstamo neto.
  * *Cargado mensualmente:* Sumado directamente como un costo fijo o variable en cada período, impactando la cuota final y la TCEA.

### B. Cálculo por Días Calendario Exactos (Base de Interés)
* **Problema Actual:** Asume meses fijos de 30 días.
* **Cambio Backend:** La tasa mensual efectiva (TEM) debe calcularse de manera dinámica periodo a periodo basándose en el número real de días calendario transcurridos entre vencimientos (ej: 31 días en diciembre, 28 en febrero) utilizando la convención SBS de base diaria:
  $$\text{Tasa del Período} = (1 + \text{TEA})^{\frac{\text{Días del Período}}{360}} - 1$$
  Esto requiere que la simulación persista fechas reales de vencimiento y no solo números de mes.

### C. Cuotas Dobles (Julio y Diciembre)
* **Problema Actual:** No permite modificar la cuota según gratificaciones.
* **Cambio Backend:** Agregar un arreglo de booleanos `doublePaymentMonths` al input de la simulación. En los meses que coincidan con julio y diciembre, la fórmula del método francés debe ajustarse para cobrar el doble de amortización de capital, modificando el factor de anualidad para equilibrar el cronograma.

### D. Gastos de Constitución en el Período 0
* **Problema Actual:** Los costos notariales y registrales iniciales no se reflejan.
* **Cambio Backend:** Agregar el campo `initialFees` al modelo de simulación. Estos gastos deben restarse del flujo de caja inicial recibido por el deudor en el período 0 ($t=0$) para calcular la **TIR** y la **TCEA** de forma transparente, de modo que reflejen el costo financiero total de la constitución del crédito:
  $$\text{Flujo}_0 = \text{Monto Financiado} - \text{Gastos Iniciales}$$

### E. Capitalización de Intereses en la Balón de Compra Inteligente
* **Problema Actual:** Se resta linealmente la cuota balón del capital inicial para calcular la cuota mensual.
* **Cambio Backend:** Cambiar la lógica para descontar la cuota balón a valor presente y restar este valor presente del capital a amortizar. Esto asegura que la cuota balón devengue intereses mensuales regulares y no quede exenta de interés, como ocurre en los modelos bancarios comerciales tradicionales de "Compra Inteligente".
