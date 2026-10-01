# Guía Completa de la API REST - SmartFinance Drive Platform

Documentación técnica y exhaustiva de todos los endpoints de **SmartFinance Drive Platform**. La plataforma está construida bajo una arquitectura orientada a **Domain-Driven Design (DDD)** y **CQRS**.

---

## Información General

* **Servidor en Producción (Render)**: `https://smartfinance-drive-platform.onrender.com`
* **Swagger UI**: [https://smartfinance-drive-platform.onrender.com/swagger-ui/index.html](https://smartfinance-drive-platform.onrender.com/swagger-ui/index.html)
* **OpenAPI Specs (JSON)**: `https://smartfinance-drive-platform.onrender.com/v3/api-docs`

---

## Autenticación y Seguridad

Para endpoints protegidos, incluye la cabecera HTTP:

```http
Authorization: Bearer <tu_access_token_jwt>
```

---

## Índice de Bounded Contexts

1. [IAM - Autenticación, Usuarios y Asesores de Ventas (20 Endpoints)](#1-iam---autenticación-usuarios-y-asesores-de-ventas)
2. [Profiles - Perfiles de Cliente (6 Endpoints)](#2-profiles---perfiles-de-cliente)
3. [Catalog - Catálogo de Vehículos, Especificaciones y Marcas (11 Endpoints)](#3-catalog---catálogo-de-vehículos-especificaciones-y-marcas)
4. [Partners - Entidades Financieras, Directorio B2B y SUNAT (16 Endpoints)](#4-partners---entidades-financieras-directorio-b2b-y-sunat)
5. [Financing - Simulaciones de Crédito y Solicitudes Bancarias (9 Endpoints)](#5-financing---simulaciones-de-crédito-y-solicitudes-bancarias)
6. [Scoring - Evaluación Crediticia (5 Endpoints)](#6-scoring---evaluación-crediticia)
7. [Projections - Depreciación de Vehículos (5 Endpoints)](#7-projections---depreciación-de-vehículos)
8. [Billing - Planes, Suscripciones, Facturas PDF, Stripe y Métricas ROI (12 Endpoints)](#8-billing---planes-suscripciones-facturas-pdf-stripe-y-métricas-roi)
9. [Messaging - Mensajería y Chat en Tiempo Real (5 Endpoints + STOMP)](#9-messaging---mensajería-y-chat-en-tiempo-real)
10. [Consultations - Asesor Financiero IA Gemini (3 Endpoints con Soporte Dual Path)](#10-consultations---asesor-financiero-ia)
11. [CRM - Gestión de Prospectos, Timeline y Pruebas de Manejo (11 Endpoints)](#11-crm---gestión-de-prospectos-timeline-y-pruebas-de-manejo)
12. [Analytics - Métricas Consolidadas y Dashboards por Rol (3 Endpoints)](#12-analytics---métricas-consolidadas-y-dashboards-por-rol)

---

## 1. IAM - Autenticación, Usuarios y Asesores de Ventas

### 1.1 Registrar Nuevo Usuario
* **Método**: `POST` | **Ruta**: `/api/v1/auth/registrations` | **Acceso**: Público

```json
// Input Body
{
  "username": "juan.perez@example.com",
  "password": "Password123!",
  "roles": ["ROLE_USER"]
}
```
```json
// Response (HTTP 201 Created)
{
  "id": 101,
  "username": "juan.perez@example.com",
  "roles": ["ROLE_USER"]
}
```

---

### 1.2 Iniciar Sesión (Obtener JWT)
* **Método**: `POST` | **Ruta**: `/api/v1/auth/sessions` | **Acceso**: Público

```json
// Input Body
{
  "username": "juan.perez@example.com",
  "password": "Password123!"
}
```
```json
// Response (HTTP 200 OK)
{
  "id": 101,
  "username": "juan.perez@example.com",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "d7b8c9a0-1234-5678-9abc-def012345678"
}
```

---

### 1.3 Renovar Token Access (Refresh Token)
* **Método**: `POST` | **Ruta**: `/api/v1/auth/tokens` | **Acceso**: Público

```json
// Input Body
{
  "refreshToken": "d7b8c9a0-1234-5678-9abc-def012345678"
}
```
```json
// Response (HTTP 200 OK)
{
  "id": 101,
  "username": "juan.perez@example.com",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "d7b8c9a0-1234-5678-9abc-def012345678"
}
```

---

### 1.4 Cerrar Sesión (Revocar Token)
* **Método**: `DELETE` | **Ruta**: `/api/v1/auth/sessions/current` | **Acceso**: Autenticado

```json
// Response (HTTP 200 OK)
{
  "message": "User signed out successfully"
}
```

---

### 1.5 Solicitar Recuperación de Contraseña
* **Método**: `POST` | **Ruta**: `/api/v1/auth/password-recoveries` | **Acceso**: Público

```json
// Input Body
{
  "username": "juan.perez@example.com"
}
```

---

### 1.6 Restablecer Contraseña con Token
* **Método**: `POST` | **Ruta**: `/api/v1/auth/password-resets` | **Acceso**: Público

```json
// Input Body
{
  "resetToken": "rst_1234567890abcdef",
  "newPassword": "NewSecurePassword123!"
}
```

---

### 1.7 Autenticación con Google OAuth2
* **Método**: `POST` | **Ruta**: `/api/v1/auth/google` | **Acceso**: Público

```json
// Input Body
{
  "idToken": "eyJhbGciOiJSUzI1NiIsImtpZCI6..."
}
```

---

### 1.8 Listar Todos los Usuarios (Paginado)
* **Método**: `GET` | **Ruta**: `/api/v1/users?page=0&size=20` | **Acceso**: `ROLE_ADMIN`

---

### 1.9 Obtener Usuario por ID
* **Método**: `GET` | **Ruta**: `/api/v1/users/{userId}` | **Acceso**: `ROLE_ADMIN` o Mismo usuario

---

### 1.10 Actualizar Rol de Usuario
* **Método**: `PUT` | **Ruta**: `/api/v1/users/{userId}/roles` | **Acceso**: `ROLE_ADMIN`

---

### 1.11 Solicitar Rol de Concesionario (DEALER) via RUC
* **Método**: `POST` | **Ruta**: `/api/v1/users/{userId}/dealer-role-requests` | **Acceso**: Mismo usuario o `ROLE_ADMIN`

---

### 1.12 Solicitar Rol de Entidad Financiera (FINANCIAL_INSTITUTION) via RUC
* **Método**: `POST` | **Ruta**: `/api/v1/users/{userId}/financial-institution-role-requests` | **Acceso**: Mismo usuario o `ROLE_ADMIN`

---

### 1.13 Listar Asesores de Ventas del Concesionario
* **Método**: `GET` | **Ruta**: `/api/v1/dealers/me/sales-agents` | **Acceso**: `ROLE_DEALER`, `ROLE_ADMIN`

---

### 1.14 Crear Asesor de Ventas
* **Método**: `POST` | **Ruta**: `/api/v1/dealers/me/sales-agents` | **Acceso**: `ROLE_DEALER`, `ROLE_ADMIN`

```json
// Input Body
{
  "fullName": "Carlos Mendoza",
  "email": "carlos.mendoza@autoland.pe",
  "phone": "+51987654321"
}
```

---

### 1.15 Actualizar Asesor de Ventas
* **Método**: `PUT` | **Ruta**: `/api/v1/dealers/me/sales-agents/{id}` | **Acceso**: `ROLE_DEALER`, `ROLE_ADMIN`

---

### 1.16 Reasignar Prospects entre Asesores de Ventas
* **Método**: `POST` | **Ruta**: `/api/v1/dealers/me/sales-agents/{id}/reassign-leads` | **Acceso**: `ROLE_DEALER`, `ROLE_ADMIN`

```json
// Input Body
{
  "targetAgentId": "b1c2d3e4-f5a6-7b8c-9d0e-112233445566"
}
```

---

### 1.17 Iniciar Verificación Corporativa B2B con OTP por Correo (/me)
* **Método**: `POST` | **Ruta**: `/api/v1/users/me/corporate-verification/initiate` | **Acceso**: Autenticado
* **Descripción**: Inicia el flujo de verificación institucional para una Entidad Financiera (Banco) o Concesionaria (Dealership). Valida que el RUC exista y se encuentre ACTIVO/HABIDO en SUNAT, verifica que el correo corporativo pertenezca al dominio institucional autorizado (ej. `@viabcp.com`, `@interbank.pe`, `@autoland.com.pe`), genera un código criptográfico OTP de 6 dígitos con hash SHA-256 (TTL de 10 minutos, máx. 3 intentos) y lo despacha por correo transaccional.
* **Seguridad y Protección Anti-Abuso**:
  * **Rate Limiter de Red**: Protegido por `RateLimitingFilter` (máximo 10 peticiones por minuto por IP; exceso retorna `429 Too Many Requests`).
  * **Tope Diario de Sesiones**: Máximo 5 solicitudes de verificación corporativa por usuario en una ventana de 24 horas (`iam.error.corporateVerification.dailyLimitExceeded`).
  * **Cooldown Anti-Spam (60 segundos)**: Si el usuario cuenta con una sesión pendiente iniciada hace menos de 60 segundos, se rechaza la re-emisión para mitigar saturación de correo y consumo innecesario de la API SUNAT (`iam.error.corporateVerification.cooldownActive`).
  * **Invalidación de Sesiones Huérfanas**: Al emitir un nuevo código para el mismo usuario y RUC, las sesiones `PENDING` previas son invalidadas de inmediato a estado `EXPIRED`, neutralizando cualquier intento de alternar o ciclar sesiones.
  * **Políticas Fail-Closed**: En producción, si el servidor SMTP no se encuentra disponible o la entrega falla, la transacción se aborta con error, evitando falsos positivos sin entrega real. Los códigos OTP jamás se registran en los logs del servidor.

```json
// Input Body
{
  "ruc": "20100047218",
  "corporateEmail": "analista.creditos@viabcp.com"
}
```

```json
// Response (HTTP 200 OK)
{
  "sessionId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "sessionActive": true,
  "maskedEmail": "a*****************s@viabcp.com",
  "expiresInSeconds": 600
}
```

---

### 1.18 Confirmar Código OTP de Verificación Corporativa B2B (/me)
* **Método**: `POST` | **Ruta**: `/api/v1/users/me/corporate-verification/confirm` | **Acceso**: Autenticado
* **Descripción**: Valida el código OTP de 6 dígitos ingresado por el usuario con protección en tiempo constante. Al verificarse exitosamente:
  1. Promueve el rol del usuario a `ROLE_FINANCIAL_INSTITUTION` o `ROLE_DEALER` según el tipo de entidad detectado por SUNAT.
  2. Auto-aprovisiona o vincula automáticamente el perfil corporativo (`FinancialEntity` o `Dealership`) con los datos oficiales de SUNAT (Razón Social, RUC, Dirección, Ubigeo, Logos institucionales).

```json
// Input Body
{
  "ruc": "20100047218",
  "code": "849201"
}
```

```json
// Response (HTTP 200 OK)
{
  "verified": true,
  "entityType": "FINANCIAL_INSTITUTION",
  "assignedRole": "ROLE_FINANCIAL_INSTITUTION",
  "profileId": "b1c2d3e4-f5a6-7b8c-9d0e-112233445566",
  "profileName": "BANCO DE CREDITO DEL PERU",
  "message": "Corporate identity verified successfully. Profile provisioned and linked."
}
```

---

### 1.19 Iniciar Verificación Corporativa B2B para Usuario Específico
* **Método**: `POST` | **Ruta**: `/api/v1/users/{userId}/corporate-verification/initiate` | **Acceso**: `ROLE_ADMIN` o Mismo usuario (`@ownershipChecker.isUserSelf`)

```json
// Input Body
{
  "ruc": "20100128056",
  "corporateEmail": "gerencia@autoland.com.pe"
}
```

```json
// Response (HTTP 200 OK)
{
  "sessionId": "b2c3d4e5-f6a7-8901-bcde-fa2345678901",
  "sessionActive": true,
  "maskedEmail": "g******a@autoland.com.pe",
  "expiresInSeconds": 600
}
```

---

### 1.20 Confirmar Código OTP para Usuario Específico
* **Método**: `POST` | **Ruta**: `/api/v1/users/{userId}/corporate-verification/confirm` | **Acceso**: `ROLE_ADMIN` o Mismo usuario (`@ownershipChecker.isUserSelf`)

```json
// Input Body
{
  "ruc": "20100128056",
  "code": "512934"
}
```

```json
// Response (HTTP 200 OK)
{
  "verified": true,
  "entityType": "DEALERSHIP",
  "assignedRole": "ROLE_DEALER",
  "profileId": "c3d4e5f6-a7b8-9012-cdef-ab3456789012",
  "profileName": "AUTOLAND S.A.",
  "message": "Corporate identity verified successfully. Profile provisioned and linked."
}
```

---

## 2. Profiles - Perfiles de Cliente

### 2.1 Crear Perfil de Cliente
* **Método**: `POST` | **Ruta**: `/api/v1/profiles` | **Acceso**: Autenticado

### 2.2 Obtener Perfil por ID
* **Método**: `GET` | **Ruta**: `/api/v1/profiles/{profileId}` | **Acceso**: Propietario o `ROLE_ADMIN`

### 2.3 Obtener Perfil por User ID
* **Método**: `GET` | **Ruta**: `/api/v1/profiles/users/{userId}` | **Acceso**: Propietario o `ROLE_ADMIN`

### 2.4 Actualizar Perfil
* **Método**: `PUT` | **Ruta**: `/api/v1/profiles/{profileId}` | **Acceso**: Propietario o `ROLE_ADMIN`

### 2.5 Eliminar Perfil
* **Método**: `DELETE` | **Ruta**: `/api/v1/profiles/{profileId}` | **Acceso**: Propietario o `ROLE_ADMIN`

---

### 2.6 Consultar Datos de Identidad RENIEC por DNI (Factiliza)
* **Método**: `GET` | **Ruta**: `/api/v1/profiles/reniec/dni/{dni}` | **Acceso**: Autenticado
* **Descripción**: Invoca la API de Factiliza para consultar los datos oficiales de identidad del ciudadano registrados en RENIEC a partir de su DNI (8 dígitos numéricos). Retorna nombres, apellidos, nombre completo oficial, ubicación geográfica y dirección para el autocompletado instantáneo de formularios de perfil de cliente.
* **Seguridad y Control de Abuso**:
  * **Rate Limiter de Red**: Protegido por `RateLimitingFilter` (máximo 10 peticiones por minuto por IP; exceso retorna `429 Too Many Requests`).
  * **Caché en Memoria**: `@Cacheable(value = "reniecDniCache")` para optimizar latencia y consumo de cuota de la API externa.
  * **Validación de Formato**: Requiere exactamente 8 dígitos numéricos (`^\d{8}$`), retornando `400 Bad Request` en caso contrario.

```json
// Response (HTTP 200 OK)
{
  "dni": "27427864",
  "verificationDigit": "7",
  "firstNames": "JOSE PEDRO",
  "paternalSurname": "CASTILLO",
  "maternalSurname": "TERRONES",
  "fullLegalName": "CASTILLO TERRONES, JOSE PEDRO",
  "department": "CAJAMARCA",
  "province": "CHOTA",
  "district": "TACABAMBA",
  "address": "CASERIO PUÑA",
  "fullAddress": "CASERIO PUÑA, CAJAMARCA - CHOTA - TACABAMBA",
  "ubigeoReniec": "060615",
  "ubigeoSunat": "060417",
  "ubigeo": [
    "06",
    "0604",
    "060417"
  ],
  "birthDate": "",
  "gender": ""
}
```

---

## 3. Catalog - Catálogo de Vehículos, Especificaciones y Marcas

### 3.1 Listar y Buscar Vehículos (Fuzzy Search & Paginación)
* **Método**: `GET` | **Ruta**: `/api/v1/vehicles` | **Acceso**: Público

```json
// Response Payload Sample
{
  "content": [
    {
      "id": "c9d8e7f6-5432-1098-7654-3210fe210987",
      "userId": "dealer-user-123",
      "financialEntityId": "b1c2d3e4-f5a6-7b8c-9d0e-112233445566",
      "brand": "Toyota",
      "model": "RAV4 Hybrid",
      "manufactureYear": 2026,
      "condition": "NEW",
      "priceAmount": 34900.00,
      "currency": "USD",
      "imagePath": "https://res.cloudinary.com/smartfinance/image/upload/v12345/rav4.jpg",
      "status": "ACTIVE",
      "mileage": 0,
      "transmission": "AUTOMATIC",
      "engine": "2.5L Hybrid",
      "traction": "AWD",
      "images": ["https://res.cloudinary.com/.../gallery1.jpg"],
      "createdAt": "2026-09-19T12:00:00Z"
    }
  ]
}
```

### 3.2 Listar Mis Vehículos (Concesionaria Autenticada)
* **Método**: `GET` | **Ruta**: `/api/v1/vehicles/my-listings` | **Acceso**: Autenticado (`ROLE_DEALER` o `ROLE_ADMIN`)

### 3.3 Listar Vehículos por ID de Usuario
* **Método**: `GET` | **Ruta**: `/api/v1/vehicles/users/{userId}` | **Acceso**: Propietario o `ROLE_ADMIN`

### 3.4 Listar Marcas Disponibles
* **Método**: `GET` | **Ruta**: `/api/v1/vehicles/brands` | **Acceso**: Público

### 3.4 Registrar Vehículo
* **Método**: `POST` | **Ruta**: `/api/v1/vehicles` | **Acceso**: `ROLE_ADMIN` o `ROLE_DEALER`

### 3.5 Obtener Vehículo por ID
* **Método**: `GET` | **Ruta**: `/api/v1/vehicles/{vehicleId}` | **Acceso**: Público

### 3.6 Actualizar Vehículo
* **Método**: `PUT` | **Ruta**: `/api/v1/vehicles/{vehicleId}` | **Acceso**: Propietario del vehículo

### 3.7 Actualizar Estado del Vehículo (ACTIVE, RESERVED, SOLD)
* **Método**: `PATCH` | **Ruta**: `/api/v1/vehicles/{vehicleId}/status` | **Acceso**: Propietario del vehículo

```json
// Input Body
{
  "status": "RESERVED"
}
```

### 3.8 Eliminar Vehículo
* **Método**: `DELETE` | **Ruta**: `/api/v1/vehicles/{vehicleId}` | **Acceso**: Propietario del vehículo

### 3.9 Cargar Imagen Principal / Cover de Vehículo
* **Método**: `POST` | **Ruta**: `/api/v1/vehicles/{vehicleId}/image` | **Acceso**: Propietario del vehículo (Multipart)

### 3.10 Cargar Imagen Adicional a Galería
* **Método**: `POST` | **Ruta**: `/api/v1/vehicles/{vehicleId}/images` | **Acceso**: Propietario del vehículo (Multipart)

### 3.11 Eliminar Imagen Específica de Galería por Índice
* **Método**: `DELETE` | **Ruta**: `/api/v1/vehicles/{vehicleId}/images/{imageIndex}` | **Acceso**: Propietario del vehículo

---

## 4. Partners - Entidades Financieras, Directorio B2B y SUNAT

### 4.1 Listar Entidades Financieras
* **Método**: `GET` | **Ruta**: `/api/v1/financial-entities` | **Acceso**: Autenticado
* **Privacidad de Identificadores Internos**: El campo `userId` (identificador interno de la cuenta titular) se sanitiza a `null` para consultas públicas o de terceros (roles `USER`, `DEALER`, `FINANCIAL_ANALYST` u otras entidades). Solo el propietario de la entidad o un `ROLE_ADMIN` visualizan el `userId`.

```json
// Output Response (200 OK)
[
  {
    "id": "b1c2d3e4-f5a6-7b8c-9d0e-112233445566",
    "userId": null,
    "ruc": "20100047218",
    "name": "Banco de Credito BCP",
    "logoUrl": "https://res.cloudinary.com/demo/image/upload/v1/banks/logos/bcp.png",
    "bannerUrl": "https://res.cloudinary.com/demo/image/upload/v1/banks/banners/bcp-banner.png",
    "rateBenchmarks": []
  }
]
```

### 4.2 Crear Entidad Financiera
* **Método**: `POST` | **Ruta**: `/api/v1/financial-entities` | **Acceso**: `ROLE_ADMIN`, `ROLE_FINANCIAL_INSTITUTION`
* **Seguridad y Restricciones**:
  * Para usuarios con rol `FINANCIAL_INSTITUTION`, el campo `userId` se enlaza automáticamente al usuario autenticado. Si el cliente envía un `userId` ajeno al del token JWT, la solicitud es rechazada de inmediato con `403 Forbidden` (`partners.error.accessDenied.cannotImpersonateUserId`). Solo `ROLE_ADMIN` puede asociar un `userId` explícito arbitrario.
  * El campo `ruc` (opcional si se crea sin RUC inicial, ej. plantilla institucional) debe tener exactamente 11 dígitos numéricos y ser único en el sistema.
  * Los campos `logoUrl` y `bannerUrl` (opcionales) exigen formato de URL HTTP o HTTPS válido (`^(https?://.+)?$`). Alternativamente, se recomienda emplear los endpoints de subida multipart `/me/logo` y `/me/banner`.

```json
// Input Body
{
  "ruc": "20100047218",
  "name": "Banco de Credito BCP",
  "logoUrl": "https://res.cloudinary.com/demo/image/upload/v1/banks/logos/bcp.png",
  "bannerUrl": "https://res.cloudinary.com/demo/image/upload/v1/banks/banners/bcp-banner.png"
}
```

```json
// Output Response (201 Created)
{
  "id": "b1c2d3e4-f5a6-7b8c-9d0e-112233445566",
  "userId": "15",
  "ruc": "20100047218",
  "name": "Banco de Credito BCP",
  "logoUrl": "https://res.cloudinary.com/demo/image/upload/v1/banks/logos/bcp.png",
  "bannerUrl": "https://res.cloudinary.com/demo/image/upload/v1/banks/banners/bcp-banner.png",
  "rateBenchmarks": []
}
```

### 4.3 Obtener Entidad Financiera por ID
* **Método**: `GET` | **Ruta**: `/api/v1/financial-entities/{id}` | **Acceso**: Autenticado
* **Privacidad**: El campo `userId` solo se expone al propietario de `{id}` o a un `ROLE_ADMIN`; para terceros se sanitiza a `null`.

### 4.4 Actualizar Entidad Financiera
* **Método**: `PUT` | **Ruta**: `/api/v1/financial-entities/{id}` | **Acceso**: `ROLE_ADMIN`, `ROLE_FINANCIAL_INSTITUTION`
* **Seguridad y Control de Propiedad**:
  * Requiere autorización estricta: evaluada mediante SpEL `@PreAuthorize("hasRole('ADMIN') or (hasRole('FINANCIAL_INSTITUTION') and @ownershipChecker.isFinancialEntityOwner(#id, authentication))")` y verificación defensiva interna.
  * Si un usuario con rol `FINANCIAL_INSTITUTION` intenta modificar una entidad que no le pertenece, se rechaza con `403 Forbidden` (`partners.error.accessDenied.notOwner`), neutralizando cualquier intento de secuestro de entidad o bypass de analytics.
  * Los usuarios `FINANCIAL_INSTITUTION` tienen prohibido transferir o alterar el campo `userId`; cualquier intento de enviar un `userId` en el payload genera `403 Forbidden` (`partners.error.accessDenied.cannotTransferOwnership`). Solo `ROLE_ADMIN` puede reasignar la titularidad de una entidad bancaria.
  * Los campos `logoUrl` y `bannerUrl` validan formato de URL HTTP o HTTPS (`^(https?://.+)?$`). Para subir imágenes binarias de forma segura, use los endpoints multipart `/me/logo` y `/me/banner`.

```json
// Input Body
{
  "name": "BBVA Peru",
  "logoUrl": "https://res.cloudinary.com/demo/image/upload/v1/banks/logos/bbva.png",
  "bannerUrl": "https://res.cloudinary.com/demo/image/upload/v1/banks/banners/bbva-banner.png"
}
```

### 4.5 Agregar Tasa de Referencia (Rate Benchmark)
* **Método**: `POST` | **Ruta**: `/api/v1/financial-entities/{id}/rate-benchmarks` | **Acceso**: `ROLE_ADMIN`, `ROLE_FINANCIAL_INSTITUTION` (solo propietario de `{id}`)
* **Seguridad y Control de Propiedad**:
  * Solo el banco propietario (`@ownershipChecker.isFinancialEntityOwner(#id, authentication)`) o un administrador pueden inyectar tasas referenciales en la entidad. Previene la inyección de tasas falsas y manipulación de promedios ponderados TEA en analytics.

```json
// Input Body
{
  "rateType": "TEA",
  "annualRate": 14.50,
  "currency": "PEN",
  "sourceLabel": "SBS Referencial 2026",
  "sourceUrl": "https://sbs.gob.pe/benchmarks",
  "effectiveFrom": "2026-01-01"
}
```

### 4.6 Obtener Mi Entidad Financiera Asociada
* **Método**: `GET` | **Ruta**: `/api/v1/financial-entities/me` | **Acceso**: `ROLE_FINANCIAL_INSTITUTION`, `ROLE_ADMIN`
* **Descripción**: Retorna la entidad financiera vinculada al usuario autenticado (determinada a partir de su ID de usuario en el JWT tras la verificación SUNAT del RUC). Retorna `404 Not Found` si el usuario no tiene entidad asociada.

```json
// Output Response (200 OK)
{
  "id": "b1c2d3e4-f5a6-7b8c-9d0e-112233445566",
  "userId": "15",
  "ruc": "20100047218",
  "name": "Banco de Credito BCP",
  "logoUrl": "https://res.cloudinary.com/demo/image/upload/v1/banks/logos/bcp.png",
  "bannerUrl": "https://res.cloudinary.com/demo/image/upload/v1/banks/banners/bcp-banner.png",
  "rateBenchmarks": []
}
```

### 4.7 Cargar Logo de Entidad Financiera
* **Método**: `POST` | **Rutas**: 
  * `/api/v1/financial-entities/me/logo` (Para la entidad vinculada al usuario autenticado)
  * `/api/v1/financial-entities/{id}/logo` (Por ID, protegido por `@ownershipChecker` para propietario o `ROLE_ADMIN`)
* **Acceso**: `ROLE_FINANCIAL_INSTITUTION`, `ROLE_ADMIN` (Multipart `file`)
* **Content-Type**: `multipart/form-data`
* **Parámetros**: `file` (MultipartFile)
* **Validaciones**: Formatos permitidos (`image/jpeg`, `image/png`, `image/webp`, `image/gif`, `image/svg+xml`), tamaño máximo de 10 MB.
* **Comportamiento**: Carga la imagen en Cloudinary en `smartfinance/financial-entities/logos`, elimina el logo anterior si existía y actualiza el campo `logoUrl`.

```json
// Output Response (200 OK)
{
  "id": "b1c2d3e4-f5a6-7b8c-9d0e-112233445566",
  "userId": "15",
  "ruc": "20100047218",
  "name": "Banco de Credito BCP",
  "logoUrl": "https://res.cloudinary.com/demo/image/upload/v1/smartfinance/financial-entities/logos/bcp-logo.png",
  "bannerUrl": "https://res.cloudinary.com/demo/image/upload/v1/banks/banners/bcp-banner.png",
  "rateBenchmarks": []
}
```

### 4.8 Cargar Banner de Entidad Financiera
* **Método**: `POST` | **Rutas**: 
  * `/api/v1/financial-entities/me/banner` (Para la entidad vinculada al usuario autenticado)
  * `/api/v1/financial-entities/{id}/banner` (Por ID, protegido por `@ownershipChecker` para propietario o `ROLE_ADMIN`)
* **Acceso**: `ROLE_FINANCIAL_INSTITUTION`, `ROLE_ADMIN` (Multipart `file`)
* **Content-Type**: `multipart/form-data`
* **Parámetros**: `file` (MultipartFile)
* **Validaciones**: Formatos permitidos (`image/jpeg`, `image/png`, `image/webp`, `image/gif`, `image/svg+xml`), tamaño máximo de 10 MB.
* **Comportamiento**: Carga la imagen en Cloudinary en `smartfinance/financial-entities/banners`, elimina el banner anterior si existía y actualiza el campo `bannerUrl`.

```json
// Output Response (200 OK)
{
  "id": "b1c2d3e4-f5a6-7b8c-9d0e-112233445566",
  "userId": "15",
  "ruc": "20100047218",
  "name": "Banco de Credito BCP",
  "logoUrl": "https://res.cloudinary.com/demo/image/upload/v1/smartfinance/financial-entities/logos/bcp-logo.png",
  "bannerUrl": "https://res.cloudinary.com/demo/image/upload/v1/smartfinance/financial-entities/banners/bcp-banner.png",
  "rateBenchmarks": []
}
```

### 4.9 Eliminar Entidad Financiera
* **Método**: `DELETE` | **Ruta**: `/api/v1/financial-entities/{id}` | **Acceso**: `ROLE_ADMIN`

### 4.10 Consulta SUNAT RUC
* **Método**: `GET` | **Ruta**: `/api/v1/partners/sunat/ruc/{ruc}` | **Acceso**: Autenticado

### 4.11 Directorio Público de Concesionarias (Paginado & Búsqueda)
* **Método**: `GET` | **Ruta**: `/api/v1/dealerships` | **Acceso**: Público

### 4.12 Obtener Mi Concesionaria B2B
* **Método**: `GET` | **Ruta**: `/api/v1/dealerships/me` | **Acceso**: `ROLE_DEALER`, `ROLE_ADMIN`

```json
// Output Response (200 OK)
{
  "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "userId": "user-uuid",
  "ruc": "20601234567",
  "name": "Autoland Peru",
  "address": "Av. Javier Prado 1234",
  "phone": "+51987654321",
  "email": "contacto@autoland.pe",
  "website": "https://autoland.pe",
  "description": "Concesionaria líder en venta de autos",
  "operatingHours": "Lun-Vie 9am-6pm",
  "rating": 5.0,
  "logoUrl": "https://res.cloudinary.com/demo/image/upload/v1/dealerships/logos/logo.png",
  "bannerUrl": "https://res.cloudinary.com/demo/image/upload/v1/dealerships/banners/banner.png",
  "active": true
}
```

### 4.13 Crear o Actualizar Mi Concesionaria B2B
* **Método**: `PUT` | **Ruta**: `/api/v1/dealerships/me` | **Acceso**: `ROLE_DEALER`, `ROLE_ADMIN`

```json
// Input Body
{
  "ruc": "20601234567",
  "name": "Autoland Peru",
  "address": "Av. Javier Prado 1234",
  "phone": "+51987654321",
  "email": "contacto@autoland.pe",
  "website": "https://autoland.pe",
  "description": "Concesionaria oficial multimarca",
  "operatingHours": "Lun-Sab 9am-7pm",
  "logoUrl": "https://res.cloudinary.com/demo/image/upload/v1/dealerships/logos/logo.png",
  "bannerUrl": "https://res.cloudinary.com/demo/image/upload/v1/dealerships/banners/banner.png"
}
```

```json
// Output Response (200 OK)
{
  "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "userId": "user-uuid",
  "ruc": "20601234567",
  "name": "Autoland Peru",
  "address": "Av. Javier Prado 1234",
  "phone": "+51987654321",
  "email": "contacto@autoland.pe",
  "website": "https://autoland.pe",
  "description": "Concesionaria oficial multimarca",
  "operatingHours": "Lun-Sab 9am-7pm",
  "rating": 5.0,
  "logoUrl": "https://res.cloudinary.com/demo/image/upload/v1/dealerships/logos/logo.png",
  "bannerUrl": "https://res.cloudinary.com/demo/image/upload/v1/dealerships/banners/banner.png",
  "active": true
}
```

### 4.14 Cargar Logo de Concesionaria
* **Método**: `POST` | **Ruta**: `/api/v1/dealerships/me/logo` | **Acceso**: `ROLE_DEALER`, `ROLE_ADMIN` (Multipart `file`)

### 4.15 Cargar Banner de Concesionaria
* **Método**: `POST` | **Ruta**: `/api/v1/dealerships/me/banner` | **Acceso**: `ROLE_DEALER`, `ROLE_ADMIN` (Multipart `file`)

### 4.16 Obtener Concesionaria por ID
* **Método**: `GET` | **Ruta**: `/api/v1/dealerships/{id}` | **Acceso**: Público

### 4.17 Obtener Vehículos de una Concesionaria Específica
* **Método**: `GET` | **Ruta**: `/api/v1/dealerships/{id}/vehicles` | **Acceso**: Público

### 4.18 Consulta SUNAT para Autocompletado de Perfil Corporativo y Dominios Autorizados
* **Método**: `GET` | **Ruta**: `/api/v1/partners/corporate-verification/lookup/{ruc}` | **Acceso**: Autenticado
* **Descripción**: Valida el RUC peruano de 11 dígitos contra el servicio oficial de SUNAT, detecta automáticamente si corresponde a una **Entidad Financiera** (`ROLE_FINANCIAL_INSTITUTION`) o a una **Concesionaria** (`ROLE_DEALER`), y retorna la información oficial requerida para rellenar automáticamente los campos del perfil corporativo (Razón Social, Dirección Fiscal, Ubigeo, Logo) junto con la lista blanca de dominios de correo institucional autorizados para verificación OTP (ej. `@viabcp.com`, `@interbank.pe`, `@autoland.com.pe`, `@derco.pe`).
* **Seguridad y Validación**:
  * **Rate Limiting**: Protegido por `RateLimitingFilter` (10 peticiones/minuto por IP).
  * **Clasificación Estricta**: Empresas que no pertenezcan al catálogo institucional ni posean actividad económica principal automotriz (CIIU 451) o financiera (CIIU 64/66) son clasificadas como `UNKNOWN` y rechazadas con `400 Bad Request` (`iam.error.sunat.notAuthorizedCorporateEntity`). RUCs con CIIU nulo o en blanco no son asumidos como corporativos.

```json
// Output Response (200 OK)
{
  "ruc": "20100047218",
  "entityType": "FINANCIAL_INSTITUTION",
  "targetRole": "ROLE_FINANCIAL_INSTITUTION",
  "suggestedName": "BANCO DE CREDITO DEL PERU",
  "fiscalAddress": "CALLE CENTENARIO NRO. 156 URB. LAS LADERAS DE MELGAREJO",
  "ubigeo": "150118",
  "allowedEmailDomains": [
    "viabcp.com",
    "bcp.com.pe"
  ],
  "suggestedLogoUrl": "https://res.cloudinary.com/demo/image/upload/v1/banks/logos/bcp.png",
  "activeAndHabido": true
}
```

---

## 5. Financing - Simulaciones de Crédito y Solicitudes Bancarias

### 5.1 Crear Simulación de Crédito Vehicular
* **Método**: `POST` | **Ruta**: `/api/v1/simulations` | **Acceso**: Autenticado

### 5.2 Listar Simulaciones (Paginado)
* **Método**: `GET` | **Ruta**: `/api/v1/simulations` | **Acceso**: Autenticado

### 5.3 Obtener Simulación por ID
* **Método**: `GET` | **Ruta**: `/api/v1/simulations/{id}` | **Acceso**: Autenticado

### 5.4 Eliminar Simulación
* **Método**: `DELETE` | **Ruta**: `/api/v1/simulations/{id}` | **Acceso**: Autenticado

### 5.5 Convertir Simulación Directamente en Solicitud de Crédito
* **Método**: `POST` | **Ruta**: `/api/v1/simulations/{id}/apply` | **Acceso**: Autenticado
* **Cuerpo de Solicitud**: N/A (Sin cuerpo; promueve automáticamente la simulación guardada a una solicitud de crédito formal)

### 5.6 Enviar Solicitud de Crédito Formal a Banco
* **Método**: `POST` | **Ruta**: `/api/v1/credit-applications` | **Acceso**: Autenticado

```json
// Input Body
{
  "simulationId": "d1e2f3a4-5678-90ab-cdef-1234567890ab",
  "financialEntityId": "b1c2d3e4-f5a6-7b8c-9d0e-112233445566",
  "vehicleId": "c9d8e7f6-5432-1098-7654-3210fe210987",
  "requestedAmount": 27920.00,
  "currency": "USD",
  "monthlyIncome": 4500.00,
  "employmentStatus": "EMPLOYED",
  "notes": "Adjunto sustento de ingresos"
}
```

### 5.7 Listar Mis Solicitudes de Crédito
* **Método**: `GET` | **Ruta**: `/api/v1/credit-applications/me` | **Acceso**: Autenticado

### 5.8 Obtener Solicitud de Crédito por ID
* **Método**: `GET` | **Ruta**: `/api/v1/credit-applications/{id}` | **Acceso**: Autenticado

### 5.9 Actualizar Estado de Solicitud (PENDING, IN_REVIEW, PRE_APPROVED, APPROVED, REJECTED, DISBURSED)
* **Método**: `PATCH` | **Ruta**: `/api/v1/credit-applications/{id}/status` | **Acceso**: `ROLE_FINANCIAL_INSTITUTION`, `ROLE_ADMIN`

> **Reglas de Transición de Estado**:
> - Los estados terminales `DISBURSED` y `REJECTED` son inmutables.
> - `DISBURSED` requiere pre-aprobación o aprobación previa (`PRE_APPROVED` / `APPROVED`).
> - No se permite revertir una solicitud al estado inicial `PENDING`.

---

## 6. Scoring - Evaluación Crediticia

### 6.1 Evaluar Score Crediticio
* **Método**: `POST` | **Ruta**: `/api/v1/credit-scores` | **Acceso**: Autenticado

### 6.2 Listar Todos los Scores Crediticios (Paginado)
* **Método**: `GET` | **Ruta**: `/api/v1/credit-scores` | **Acceso**: Propietario o `ROLE_ADMIN`

### 6.3 Obtener Score Crediticio por ID
* **Método**: `GET` | **Ruta**: `/api/v1/credit-scores/{id}` | **Acceso**: Propietario o `ROLE_ADMIN`

### 6.4 Obtener Scores Crediticios por Profile ID
* **Método**: `GET` | **Ruta**: `/api/v1/credit-scores/profile/{profileId}` | **Acceso**: Propietario o `ROLE_ADMIN`

### 6.5 Eliminar Score Crediticio
* **Método**: `DELETE` | **Ruta**: `/api/v1/credit-scores/{id}` | **Acceso**: Propietario o `ROLE_ADMIN`

---

## 7. Projections - Depreciación de Vehículos

### 7.1 Calcular Proyección de Depreciación
* **Método**: `POST` | **Ruta**: `/api/v1/depreciation-projections` | **Acceso**: Autenticado

### 7.2 Listar Proyecciones (Paginado)
* **Método**: `GET` | **Ruta**: `/api/v1/depreciation-projections` | **Acceso**: Propietario del vehículo o `ROLE_ADMIN`

### 7.3 Obtener Proyección por ID
* **Método**: `GET` | **Ruta**: `/api/v1/depreciation-projections/{id}` | **Acceso**: Propietario del vehículo o `ROLE_ADMIN`

### 7.4 Obtener Proyecciones por Vehicle ID
* **Método**: `GET` | **Ruta**: `/api/v1/depreciation-projections/vehicle/{vehicleId}` | **Acceso**: Propietario del vehículo o `ROLE_ADMIN`

### 7.5 Eliminar Proyección
* **Método**: `DELETE` | **Ruta**: `/api/v1/depreciation-projections/{id}` | **Acceso**: Propietario del vehículo o `ROLE_ADMIN`

---

## 8. Billing - Planes, Suscripciones, Facturas PDF, Stripe y Métricas ROI

### 8.1 Listar Planes Activos
* **Método**: `GET` | **Ruta**: `/api/v1/billing/plans` | **Acceso**: Autenticado

### 8.2 Obtener Plan por ID
* **Método**: `GET` | **Ruta**: `/api/v1/billing/plans/{planId}` | **Acceso**: Autenticado

### 8.3 Crear Nuevo Plan
* **Método**: `POST` | **Ruta**: `/api/v1/billing/plans` | **Acceso**: `ROLE_ADMIN`

### 8.4 Obtener Suscripción Actual del Usuario
* **Método**: `GET` | **Ruta**: `/api/v1/billing/subscriptions/me` | **Acceso**: Autenticado

### 8.5 Crear Suscripción Directa
* **Método**: `POST` | **Ruta**: `/api/v1/billing/subscriptions` | **Acceso**: Autenticado

### 8.6 Cancelar Suscripción Activa
* **Método**: `DELETE` | **Ruta**: `/api/v1/billing/subscriptions/{subscriptionId}` | **Acceso**: Autenticado

### 8.7 Crear Sesión de Stripe Checkout
* **Método**: `POST` | **Ruta**: `/api/v1/billing/subscriptions/checkout-session` | **Acceso**: Autenticado

### 8.8 Facturas del Usuario Actual
* **Método**: `GET` | **Ruta**: `/api/v1/billing/invoices/me` | **Acceso**: Autenticado

### 8.9 Descargar Factura en PDF
* **Método**: `GET` | **Ruta**: `/api/v1/billing/invoices/{invoiceId}/pdf` | **Acceso**: Autenticado

### 8.10 Pagar / Reconciliar Factura
* **Método**: `PATCH` | **Ruta**: `/api/v1/billing/invoices/{invoiceId}` | **Acceso**: Autenticado

### 8.11 Webhook Receptor de Eventos Stripe
* **Método**: `POST` | **Ruta**: `/api/v1/billing/webhooks/stripe` | **Acceso**: Público

### 8.12 Obtener Métricas de Rendimiento ROI de Concesionario
* **Método**: `GET` | **Ruta**: `/api/v1/dealers/me/metrics` | **Acceso**: `ROLE_DEALER`, `ROLE_ADMIN`

```json
// Response (HTTP 200 OK)
{
  "totalLeadsGenerated": 24,
  "conversionRate": 16.5,
  "totalVehicleViews": 1450,
  "membershipRoi": "5.2x",
  "activeListingsCount": 8,
  "period": "LAST_30_DAYS"
}
```

---

## 9. Messaging - Mensajería y Chat en Tiempo Real

### 9.1 Listar Conversaciones Activas del Usuario
* **Método**: `GET` | **Ruta**: `/api/v1/conversations` | **Acceso**: Autenticado

```json
// Response Payload Sample
[
  {
    "id": "e1f2a3b4-5678-90ab-cdef-1234567890ab",
    "buyerUserId": "buyer-user-123",
    "dealerUserId": "dealer-user-777",
    "vehicleId": "c9d8e7f6-5432-1098-7654-3210fe210987",
    "lastMessageContent": "Hola, ¿el vehículo está disponible para prueba de manejo?",
    "lastMessageTimestamp": "2026-09-19T14:00:00Z",
    "unreadBuyerCount": 0,
    "unreadDealerCount": 1,
    "active": true,
    "createdAt": "2026-09-19T12:00:00Z"
  }
]
```

### 9.2 Historial de Mensajes de una Conversación
* **Método**: `GET` | **Ruta**: `/api/v1/conversations/{id}/messages` | **Acceso**: Autenticado

### 9.3 Iniciar Nueva Conversación
* **Método**: `POST` | **Ruta**: `/api/v1/conversations` | **Acceso**: Autenticado

```json
// Input Body
{
  "dealerUserId": "dealer-user-777",
  "vehicleId": "c9d8e7f6-5432-1098-7654-3210fe210987",
  "initialMessage": "Hola, estoy interesado en este vehículo."
}
```

### 9.4 Enviar Mensaje en Conversación Existente (REST Fallback)
* **Método**: `POST` | **Ruta**: `/api/v1/conversations/{id}/messages` | **Acceso**: Autenticado

### 9.5 WebSocket STOMP - Chat en Tiempo Real Dual
* **Endpoint Handshake WebSocket**: `/ws/chat` (Soporta SockJS fallback)
* **Destination Envío Mensaje**: `/app/chat.sendMessage`
* **Topic Suscripción Broadcast**: `/topic/conversations/{conversationId}`

```json
// Payload enviado a /app/chat.sendMessage
{
  "conversationId": "e1f2a3b4-5678-90ab-cdef-1234567890ab",
  "senderUserId": "buyer-user-123",
  "content": "Hola, confirmo mi asistencia para la prueba de manejo."
}
```

---

## 10. Consultations - Asesor Financiero IA

> **Soporte Dual Path**: Los endpoints del módulo de asesoría IA están mapeados de forma nativa tanto en `/api/v1/consultations` como en su alias `/api/v1/ai/consultations`.

### 10.1 Obtener Recomendaciones de Vehículos Sugeridos por IA
* **Método**: `GET` | **Rutas**: `/api/v1/consultations/recommendations` o `/api/v1/ai/consultations/recommendations` | **Acceso**: Autenticado
* **Cuerpo de Solicitud**: N/A

### 10.2 Enviar Consulta Interactiva al Asesor IA
* **Método**: `POST` | **Rutas**: `/api/v1/consultations`, `/api/v1/consultations/chat` (y sus alias `/api/v1/ai/consultations`, `/api/v1/ai/consultations/chat`) | **Acceso**: Autenticado

```json
// Input Body
{
  "prompt": "¿Qué categoría de vehículo me conviene según mis ingresos?",
  "monthlyIncome": 4500.00,
  "currency": "PEN"
}
```
```json
// Response (HTTP 201 Created)
{
  "id": "a1b2c3d4-e5f6-7a8b-9c0d-112233445566",
  "userId": "user-123",
  "prompt": "¿Qué categoría de vehículo me conviene según mis ingresos?",
  "recommendationText": "Basado en un ingreso mensual de 4500 PEN...",
  "recommendedCategory": "SUV Crossover / Sedan Ejecutivo",
  "estimatedMaxMonthlyFee": 1350.00,
  "createdAt": "2026-09-19T14:00:00Z"
}
```

### 10.3 Historial de Consultas IA del Usuario
* **Método**: `GET` | **Rutas**: `/api/v1/consultations/history` o `/api/v1/ai/consultations/history` | **Acceso**: Autenticado

---

## 11. CRM - Gestión de Prospectos, Timeline y Pruebas de Manejo (11 Endpoints)

### 11.1 Crear Prospecto CRM
* **Método**: `POST` | **Rutas**: `/api/v1/dealers/me/prospects` o `/api/v1/prospects` | **Acceso**: Autenticado

```json
// Input Body
{
  "fullName": "Juan Perez",
  "email": "juan.perez@example.com",
  "phone": "+51999888777",
  "interestedVehicleId": "c9d8e7f6-5432-1098-7654-3210fe210987",
  "salesAgentId": "b1c2d3e4-f5a6-7b8c-9d0e-112233445566"
}
```

### 11.2 Listar Prospectos del Concesionario
* **Método**: `GET` | **Ruta**: `/api/v1/dealers/me/prospects` | **Acceso**: `ROLE_DEALER`, `ROLE_ADMIN`

### 11.3 Obtener Detalle de Prospecto por ID
* **Método**: `GET` | **Ruta**: `/api/v1/dealers/me/prospects/{id}` | **Acceso**: `ROLE_DEALER`, `ROLE_ADMIN`

### 11.4 Agregar Nota al Timeline del Prospecto
* **Método**: `POST` | **Ruta**: `/api/v1/prospects/{id}/notes` | **Acceso**: `ROLE_DEALER`, `ROLE_ADMIN`

### 11.5 Obtener Timeline de Notas del Prospecto
* **Método**: `GET` | **Ruta**: `/api/v1/prospects/{id}/timeline` | **Acceso**: `ROLE_DEALER`, `ROLE_ADMIN`

### 11.6 Actualizar Estado CRM del Prospecto
* **Método**: `PATCH` | **Ruta**: `/api/v1/prospects/{id}/status` | **Acceso**: `ROLE_DEALER`, `ROLE_ADMIN`

```json
// Input Body
{
  "status": "NEGOTIATING"
}
```

### 11.7 Programar Cita de Prueba de Manejo (Test Drive)
* **Método**: `POST` | **Ruta**: `/api/v1/test-drives` | **Acceso**: Autenticado

```json
// Input Body
{
  "vehicleId": "c9d8e7f6-5432-1098-7654-3210fe210987",
  "dealershipId": "a1b2c3d4-e5f6-7a8b-9c0d-112233445566",
  "scheduledDateTime": "2026-10-15T10:00:00",
  "notes": "Prueba de manejo turno mañana"
}
```

### 11.8 Listar Mis Pruebas de Manejo
* **Método**: `GET` | **Ruta**: `/api/v1/test-drives/me` | **Acceso**: Autenticado

### 11.9 Obtener Detalle de Prueba de Manejo por ID
* **Método**: `GET` | **Ruta**: `/api/v1/test-drives/{id}` | **Acceso**: Autenticado

### 11.10 Actualizar Estado de Prueba de Manejo (SCHEDULED, COMPLETED, CANCELLED)
* **Método**: `PATCH` | **Ruta**: `/api/v1/test-drives/{id}/status` | **Acceso**: Autenticado

```json
// Input Body
{
  "status": "COMPLETED"
}
```

### 11.11 Cancelar Cita de Prueba de Manejo
* **Método**: `DELETE` | **Ruta**: `/api/v1/test-drives/{id}` | **Acceso**: Autenticado

---

## 12. Analytics - Métricas Consolidadas y Dashboards por Rol

Métricas analíticas agregadas en tiempo real para Concesionarias, Entidades Financieras y Administradores de la plataforma con estricta validación de propiedad y protección contra IDOR.

### 12.1 Obtener Métricas de Dashboard para Concesionario
* **Método**: `GET` | **Ruta**: `/api/v1/analytics/dealer` | **Acceso**: `ROLE_DEALER`, `ROLE_ADMIN`
* **Seguridad y Control de Propiedad**:
  * Un usuario con rol `DEALER` solo puede consultar sus propias métricas. Si omite el parámetro `dealerUserId` o lo envía vacío, se infiere automáticamente del JWT autenticado. Si especifica un `dealerUserId` correspondiente a otra concesionaria, la solicitud es rechazada con `403 Forbidden` tanto a nivel de SpEL `@PreAuthorize` como en la lógica defensiva del controlador.
  * Los usuarios con rol `ADMIN` pueden consultar las métricas de cualquier concesionario especificando `dealerUserId`.
* **Parámetros Opcionales de Consulta**:
  * `dealerUserId` (string): Identificador de usuario del concesionario.
  * `period` (string): Ventana temporal para métricas operacionales.
    * Valores permitidos: `ALL_TIME` (por defecto si se omite o está en blanco), `LAST_30_DAYS`, `LAST_7_DAYS`.
    * **Validación**: Si se envía un valor no soportado (ej. `?period=INVALID`), el endpoint retorna de forma inmediata `400 Bad Request` con código de error `analytics.error.invalidPeriod: <valor>`.
* **Semántica de Métricas**:
  * **Actividad Operativa Filtrada (`crm`, `testDrives`)**: Los prospectos capturados, estados del embudo y citas de prueba de manejo se filtran dinámicamente según la ventana de tiempo especificada en `period` (últimos 7 días, últimos 30 días o histórico total).
  * **Snapshot en Tiempo Real de Activos y Pipeline (`inventory`, `financing`)**: Las métricas de vehículos en catálogo (conteo por estado AVAILABLE/RESERVED/SOLD y valorización monetaria total en PEN y USD) así como el pipeline de solicitudes de crédito asociadas reflejan el **estado actual en tiempo real** de la concesionaria, garantizando que el dashboard exponga siempre la disponibilidad viva del inventario independientemente del filtro de periodo histórico seleccionado.

```json
// Response (HTTP 200 OK)
{
  "dealerUserId": "dealer-user-123",
  "inventory": {
    "totalVehicles": 12,
    "availableVehicles": 9,
    "reservedVehicles": 2,
    "soldVehicles": 1,
    "totalInventoryValuePen": 450000.00,
    "totalInventoryValueUsd": 35000.00
  },
  "crm": {
    "totalLeads": 24,
    "newLeads": 8,
    "contactedLeads": 6,
    "qualifiedLeads": 4,
    "inNegotiationLeads": 3,
    "closedWonLeads": 2,
    "closedLostLeads": 1,
    "conversionRate": 8.3
  },
  "testDrives": {
    "totalTestDrives": 10,
    "pendingTestDrives": 3,
    "confirmedTestDrives": 4,
    "completedTestDrives": 2,
    "cancelledTestDrives": 1
  },
  "financing": {
    "totalApplicationsReceived": 7,
    "pendingApplications": 3,
    "approvedApplications": 3,
    "rejectedApplications": 1
  },
  "period": "LAST_30_DAYS"
}
```

### 12.2 Obtener Métricas de Dashboard para Entidad Financiera (Banco)
* **Método**: `GET` | **Ruta**: `/api/v1/analytics/financial-institution` | **Acceso**: `ROLE_FINANCIAL_INSTITUTION`, `ROLE_ADMIN`
* **Seguridad y Control de Propiedad**:
  * Para usuarios con rol `FINANCIAL_INSTITUTION`, el parámetro `financialEntityId` es opcional: si se omite, se resuelve automáticamente la entidad financiera asociada a su cuenta de usuario (`userId`). Si el usuario aún no posee una entidad vinculada (ej. registro previo a la validación de RUC), recibe `403 Forbidden` con error `partners.error.financialEntity.notAssociated`.
  * Si se provee explícitamente `financialEntityId`, se valida mediante SpEL `@ownershipChecker.isFinancialEntityOwner` que el usuario sea el propietario registrado de dicha entidad bancaria; si intenta consultar un banco ajeno, se rechaza de inmediato con `403 Forbidden` (`partners.error.accessDenied.notOwner`), mitigando cualquier vulnerabilidad IDOR.
  * Los usuarios con rol `ADMIN` pueden consultar métricas de cualquier entidad financiera proporcionando `financialEntityId`.
* **Parámetros Opcionales de Consulta**:
  * `financialEntityId` (UUID): Identificador único de la entidad financiera.

```json
// Response (HTTP 200 OK)
{
  "financialEntityId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "financialEntityName": "Banco Internacional",
  "totalApplicationsReceived": 48,
  "underReviewApplications": 12,
  "approvedApplications": 24,
  "rejectedApplications": 8,
  "disbursedApplications": 4,
  "approvalRate": 58.3,
  "totalRequestedVolumePen": 2450000.00,
  "totalDisbursedVolumePen": 920000.00,
  "averageTea": 14.85,
  "activeRateBenchmarksCount": 4,
  "totalSimulationsCount": 18
}
```

### 12.3 Obtener Métricas Globales para Administrador de la Plataforma
* **Método**: `GET` | **Ruta**: `/api/v1/analytics/admin` | **Acceso**: `ROLE_ADMIN`
* **Notas de Cálculo y Configuración**:
  * `estimatedMonthlyRecurringRevenueUsd` (MRR): Ingresos recurrentes mensuales consolidados en USD calculados sobre todas las suscripciones de concesionarias activas. Los planes anuales se prorratean mensualmente dividiendo entre 12. Las tarifas contratadas en Soles (PEN) se normalizan a USD mediante la tasa de cambio configurable `analytics.fx.pen-to-usd` (por defecto `3.75`, configurable vía variable de entorno `ANALYTICS_FX_PEN_TO_USD`). El servicio valida estrictamente que la tasa sea un número estrictamente positivo (`> 0`); valores iguales a 0 o negativos son rechazados o protegidos con fallback automático a la tasa base `3.75`, blindando el cálculo de MRR contra excepciones de división por cero (`ArithmeticException`).

```json
// Response (HTTP 200 OK)
{
  "totalDealerships": 15,
  "activeDealerships": 13,
  "totalFinancialEntities": 6,
  "totalRegisteredUsers": 320,
  "totalVehiclesListed": 180,
  "totalCreditApplications": 95,
  "totalSimulationsRun": 412,
  "totalActiveSubscriptions": 12,
  "estimatedMonthlyRecurringRevenueUsd": 2400.00
}
```


