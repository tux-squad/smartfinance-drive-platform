# Flujo de Autenticación y Guía de Integración para Agentes y Frontend - SmartFinance Drive Platform

Esta guía define de forma textual, estructurada y sin ambigüedades la secuencia exacta de llamadas HTTP, prerrequisitos, payloads, reglas de decisión y **guía de vistas/redirección para el Frontend**, para que un desarrollador o un **Agente IA / Cliente Frontend** pueda implementar las pantallas e integrar la autenticación en la plataforma.

---

## 1. Reglas Globales de Autenticación (Para Agentes y Clientes)

1. **URL Base:** `https://smartfinance-drive-platform.onrender.com` (o `http://localhost:8080` en desarrollo).
2. **Endpoints Públicos (sin Token):** Todos los que inician con `/api/v1/auth/**`.
3. **Endpoints Protegidos (con Token):** Requieren la cabecera HTTP:
   ```http
   Authorization: Bearer <accessToken>
   ```
4. **Manejo de Expiración:** Cuando una petición a un endpoint protegido retorna `HTTP 401 Unauthorized`, se debe invocar la renovación de token vía `POST /api/v1/auth/tokens` con el `refreshToken`.

---

## 2. Flujo 1: Registro e Inicio de Sesión de Usuario / Comprador (`ROLE_USER`)

### Secuencia de Ejecución Falsa/Real (Paso a Paso):

#### **Paso 1.1: Verificación Previa de Correo por Código OTP (Brevo)**
* **Objetivo:** Confirmar que la persona posee el correo indicado.
* **Acción:** Enviar petición `POST /api/v1/auth/email-verification/send`
  * **Input (JSON):**
    ```json
    {
      "email": "usuario@ejemplo.com"
    }
    ```
  * **Output Esperado (HTTP 200 OK):**
    ```json
    {
      "email": "usuario@ejemplo.com",
      "status": "SENT",
      "message": "Código OTP enviado exitosamente a usuario@ejemplo.com",
      "expiresInSeconds": 900
    }
    ```
* **Acción:** El usuario revisa su correo y el frontend llama a `POST /api/v1/auth/email-verification/verify`
  * **Input (JSON):**
    ```json
    {
      "email": "usuario@ejemplo.com",
      "code": "123456"
    }
    ```
  * **Output Esperado (HTTP 200 OK):**
    ```json
    {
      "email": "usuario@ejemplo.com",
      "verified": true,
      "verificationToken": "evt_9a8b7c6d5e4f3a2b1c",
      "message": "Correo electrónico verificado correctamente"
    }
    ```

#### **Paso 1.2: Registro de Cuenta**
* **Objetivo:** Crear el usuario en la base de datos con rol `ROLE_USER`.
* **Opción A (Registro Tradicional):** Enviar petición `POST /api/v1/auth/registrations`
  * **Input (JSON):**
    ```json
    {
      "email": "usuario@ejemplo.com",
      "password": "Password123!",
      "firstName": "Juan",
      "lastName": "Pérez"
    }
    ```
  * **Output Esperado (HTTP 201 Created):**
    ```json
    {
      "id": 1,
      "email": "usuario@ejemplo.com",
      "firstName": "Juan",
      "lastName": "Pérez",
      "roles": ["ROLE_USER"]
    }
    ```
* **Opción B (Registro/Login con Google):** Enviar petición `POST /api/v1/auth/google`
  * **Input (JSON):** `{"idToken": "<GOOGLE_ID_TOKEN>"}`

#### **Paso 1.3: Verificación de Teléfono Celular (SMS Firebase)**
* **Objetivo:** Validar el número de celular para contacto o evaluación crediticia.
* **Acción:** Tras validar el SMS mediante la SDK de Firebase en el cliente, enviar petición `POST /api/v1/auth/phone-verification/firebase`
  * **Input (JSON):**
    ```json
    {
      "firebaseIdToken": "<FIREBASE_SMS_ID_TOKEN>"
    }
    ```
  * **Output Esperado (HTTP 200 OK):**
    ```json
    {
      "verified": true,
      "phoneNumber": "+51987654321",
      "status": "VERIFIED",
      "verificationToken": "pvt_3f2e1d0c9b8a7"
    }
    ```

#### **Paso 1.4: Inicio de Sesión (Login)**
* **Acción:** Enviar petición `POST /api/v1/auth/sessions`
  * **Input (JSON):**
    ```json
    {
      "email": "usuario@ejemplo.com",
      "password": "Password123!"
    }
    ```
  * **Output Esperado (HTTP 200 OK):**
    ```json
    {
      "token": "eyJhbGciOi...",
      "refreshToken": "4a7b9c1d-8eef-4123-90ab-cdef12345678",
      "user": {
        "id": 1,
        "email": "usuario@ejemplo.com",
        "roles": ["ROLE_USER"]
      }
    }
    ```
* **Regla de Guardado:** Almacenar `token` en memoria/state y `refreshToken` en almacenamiento seguro.

---

## 3. Flujo 2: Elevación a Rol Concesionario (`ROLE_DEALER`)

Este flujo se ejecuta cuando un usuario con rol `ROLE_USER` desea convertirse en vendedor/concesionario de vehículos.

### Secuencia de Ejecución:

#### **Paso 2.1: Autenticación Previa**
* El cliente DEBE estar autenticado con un token Bearer de `ROLE_USER`.

#### **Paso 2.2: Enviar Solicitud de Elevación por RUC**
* **Acción:** Enviar petición `POST /api/v1/users/{userId}/dealer-role-requests`
  * **Header:** `Authorization: Bearer <accessToken>`
  * **Input (JSON):**
    ```json
    {
      "ruc": "20123456789",
      "companyName": "Automotriz Lima SAC"
    }
    ```
  * **Lógica Interna del Backend:**
    1. El backend consulta SUNAT.
    2. SI el RUC está **ACTIVO**, **HABIDO** y su actividad principal es **CIIU 451xx** (Venta de vehículos), la solicitud se aprueba INMEDIATAMENTE y el usuario recibe el rol `ROLE_DEALER`.
    3. SI no cumple la regla automática, queda en estado `PENDIENTE` para revisión del administrador.

#### **Paso 2.3: Actualización de Token de Sesión**
* **Acción:** Invocar refresco de token vía `POST /api/v1/auth/tokens`
  * **Input (JSON):** `{"refreshToken": "<refreshToken>"}`
  * **Resultado:** El nuevo `token` de acceso contendrá el arreglo de roles `["ROLE_USER", "ROLE_DEALER"]`.

---

## 4. Flujo 3: Elevación a Rol Entidad Financiera (`ROLE_FINANCIAL_INSTITUTION`)

Este flujo permite a un usuario registrar una entidad bancaria o financiera.

### Secuencia de Ejecución:

#### **Paso 3.1: Autenticación Previa**
* El cliente DEBE estar autenticado con token Bearer de `ROLE_USER`.

#### **Paso 3.2: Enviar Solicitud Institucional**
* **Acción:** Enviar petición `POST /api/v1/users/{userId}/financial-institution-role-requests`
  * **Header:** `Authorization: Bearer <accessToken>`
  * **Input (JSON):**
    ```json
    {
      "ruc": "20987654321",
      "institutionName": "Banco CrediAuto SA"
    }
    ```
* **Paso 3.3: Aprobación por Admin (si requiere revisión manual):**
  * El Administrador ejecuta `POST /api/v1/partners/corporate-verifications/{requestId}/approve`.

#### **Paso 3.4: Actualización de Token**
* El usuario llama a `POST /api/v1/auth/tokens` para obtener su nuevo token con rol `ROLE_FINANCIAL_INSTITUTION`.

---

## 5. Flujo 4: Registro de Agente de Ventas (`ROLE_SALES_AGENT`)

Este flujo se ejecuta cuando un Concesionario (`ROLE_DEALER`) da de alta a un vendedor.

### Secuencia de Ejecución:

#### **Paso 4.1: Creación del Agente por el Dealer**
* **Header Required:** `Authorization: Bearer <dealerAccessToken>`
* **Acción:** Enviar petición `POST /api/v1/dealers/me/sales-agents`
  * **Input (JSON):**
    ```json
    {
      "email": "agente@dealer.com",
      "firstName": "Carlos",
      "lastName": "Gómez"
    }
    ```
  * **Efecto:** El sistema crea la cuenta con `ROLE_SALES_AGENT`, la vincula al `dealerId` y envía un correo vía Brevo para establecer la clave.

#### **Paso 4.2: Primer Login del Agente**
* Una vez establecida su clave, el agente realiza login en `POST /api/v1/auth/sessions`.
* El JWT retornado contiene `roles: ["ROLE_SALES_AGENT"]`.

---

## 6. Diagrama de Decisiones del Agente Frontend / IA

```
[Inicio: Usuario llega a la App]
          │
          ├── ¿Desea iniciar sesión? 
          │     └── SI: Llama a POST /api/v1/auth/sessions
          │           └── ¿Respuesta HTTP 200? -> Guarda token JWT y redirige según roles.
          │
          ├── ¿Desea registrarse?
          │     └── SI: 
          │           1. (Opcional) POST /api/v1/auth/email-verification/send
          │           2. (Opcional) POST /api/v1/auth/email-verification/verify
          │           3. POST /api/v1/auth/registrations (o /api/v1/auth/google)
          │           4. (Opcional) POST /api/v1/auth/phone-verification/firebase
          │           5. POST /api/v1/auth/sessions -> Guarda token JWT.
          │
          └── ¿Es Usuario (ROLE_USER) y quiere ser Dealer o Banco?
                ├── Para Dealer: POST /api/v1/users/{id}/dealer-role-requests
                ├── Para Banco:  POST /api/v1/users/{id}/financial-institution-role-requests
                └── Refresca sesión: POST /api/v1/auth/tokens -> Obtiene nuevo JWT con rol elevado.
```

---

## 7. Tabla Consolidada de Endpoints y Permisos Requeridos

| Operación | Método | Ruta Endpoint | Requiere Header Bearer? | Rol Necesario |
| :--- | :--- | :--- | :--- | :--- |
| Enviar OTP Email | `POST` | `/api/v1/auth/email-verification/send` | **NO** (`permitAll`) | Cualquiera (Público) |
| Verificar OTP Email | `POST` | `/api/v1/auth/email-verification/verify` | **NO** (`permitAll`) | Cualquiera (Público) |
| Validar SMS Firebase | `POST` | `/api/v1/auth/phone-verification/firebase` | **NO** (`permitAll`) | Cualquiera (Público) |
| Registrar Usuario | `POST` | `/api/v1/auth/registrations` | **NO** (`permitAll`) | Cualquiera (Público) |
| Iniciar Sesión | `POST` | `/api/v1/auth/sessions` | **NO** (`permitAll`) | Cualquiera (Público) |
| Auth con Google | `POST` | `/api/v1/auth/google` | **NO** (`permitAll`) | Cualquiera (Público) |
| Refrescar Token | `POST` | `/api/v1/auth/tokens` | **NO** (`permitAll`) | Cualquiera (Público) |
| Cerrar Sesión | `DELETE`| `/api/v1/auth/sessions/current` | **SÍ** | Cualquiera autenticado |
| Solicitar Dealer | `POST` | `/api/v1/users/{userId}/dealer-role-requests` | **SÍ** | `ROLE_USER` |
| Solicitar Banco | `POST` | `/api/v1/users/{userId}/financial-institution-role-requests` | **SÍ** | `ROLE_USER` |
| Crear Agente Ventas | `POST` | `/api/v1/dealers/me/sales-agents` | **SÍ** | `ROLE_DEALER` |
| Aprobar Empresa | `POST` | `/api/v1/partners/corporate-verifications/{requestId}/approve` | **SÍ** | `ROLE_ADMIN` |

---

## 8. Guía de Diseño de Vistas y Redirección para el Frontend (Para Agentes UI / Frontend)

Para que el Agente Frontend construya la interfaz correctamente, debe seguir la siguiente arquitectura de componentes y reglas de navegación:

### 8.1. Arquitectura de Vistas Públicas (Login y Registro)

1. **Vista de Login (`/login`):**
   * **Es ÚNICA para todos los usuarios** (Compradores, Concesionarios, Bancos, Agentes de Venta y Admins).
   * Formulario: Email y Contraseña + Botón "Ingresar" + Botón "Continuar con Google".
   * **Acción:** `POST /api/v1/auth/sessions`.
   * **Lógica de Redirección según Roles del Payload retornado:**
     * `user.roles` incluye `ROLE_ADMIN` -> Redirige a `/admin/dashboard`.
     * `user.roles` incluye `ROLE_FINANCIAL_INSTITUTION` -> Redirige a `/bank/dashboard`.
     * `user.roles` incluye `ROLE_DEALER` -> Redirige a `/dealer/dashboard`.
     * `user.roles` incluye `ROLE_SALES_AGENT` -> Redirige a `/agent/dashboard`.
     * `user.roles` incluye solo `ROLE_USER` -> Redirige a `/catalog` (Portal Comprador).

2. **Vista de Registro (`/register`):**
   * **Selector inicial de tipo de cuenta:**
     * **Opción A: Comprador / Usuario Personal:** 
       * Muestra formulario de Nombre, Apellido, Email y Contraseña.
       * Invoca `POST /api/v1/auth/registrations`.
       * Redirige al catálogo o solicitud de verificación OTP.
     * **Opción B: Concesionario (Empresa):**
       * Formulario de 2 pasos:
         1. Datos de usuario creador (Nombre, Email, Clave).
         2. Datos corporativos (RUC 20, Razón Social).
       * Invoca `POST /api/v1/auth/registrations` y luego `POST /api/v1/users/{userId}/dealer-role-requests`.
       * Redirige a `/dealer/onboarding-status` (esperando aprobación o confirmado por SUNAT).
     * **Opción C: Entidad Financiera / Banco:**
       * Formulario de 2 pasos:
         1. Datos de usuario creador.
         2. Datos institucionales (RUC 20, Nombre de la Institución).
       * Invoca `POST /api/v1/auth/registrations` y luego `POST /api/v1/users/{userId}/financial-institution-role-requests`.
       * Redirige a `/bank/onboarding-status`.

3. **Vista de Activación de Agente de Ventas (`/set-password`):**
   * **No hay registro público para vendedores.**
   * El vendedor llega a esta vista mediante un enlace con token en su correo (generado cuando el Concesionario llama a `POST /api/v1/dealers/me/sales-agents`).
   * Define su contraseña e inicia sesión por la pantalla común `/login`.

---

### 8.2. Matriz de Redirecciones del Frontend

| Rol Retornado en JWT (`user.roles`) | Ruta de Redirección en Frontend | Vista / Módulo a Renderizar |
| :--- | :--- | :--- |
| `["ROLE_ADMIN"]` | `/admin/dashboard` | Gestión Global, Aprobaciones RUC, Usuarios |
| `["ROLE_FINANCIAL_INSTITUTION"]` | `/bank/dashboard` | Evaluaciones Crediticias, Políticas de Crédito |
| `["ROLE_DEALER"]` | `/dealer/dashboard` | Gestión de Catálogo de Vehículos, Alta de Agentes |
| `["ROLE_SALES_AGENT"]` | `/agent/dashboard` | Cotizaciones Asignadas, Atencion de Citas |
| `["ROLE_USER"]` | `/catalog` | Explorar Autos, Simular Crédito, Solicitar Evaluación |

---
*Especificación textual de integración técnica para agentes de IA y aplicaciones cliente - SmartFinance Drive Platform.*
