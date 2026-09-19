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

1. [IAM - Autenticación, Usuarios y Asesores de Ventas (16 Endpoints)](#1-iam---autenticación-usuarios-y-asesores-de-ventas)
2. [Profiles - Perfiles de Cliente (5 Endpoints)](#2-profiles---perfiles-de-cliente)
3. [Catalog - Catálogo de Vehículos, Especificaciones y Marcas (11 Endpoints)](#3-catalog---catálogo-de-vehículos-especificaciones-y-marcas)
4. [Partners - Entidades Financieras, Directorio B2B y SUNAT (13 Endpoints)](#4-partners---entidades-financieras-directorio-b2b-y-sunat)
5. [Financing - Simulaciones de Crédito y Solicitudes Bancarias (8 Endpoints)](#5-financing---simulaciones-de-crédito-y-solicitudes-bancarias)
6. [Scoring - Evaluación Crediticia (5 Endpoints)](#6-scoring---evaluación-crediticia)
7. [Projections - Depreciación de Vehículos (5 Endpoints)](#7-projections---depreciación-de-vehículos)
8. [Billing - Planes, Suscripciones, Facturas PDF, Stripe y Métricas ROI (12 Endpoints)](#8-billing---planes-suscripciones-facturas-pdf-stripe-y-métricas-roi)
9. [Messaging - Mensajería y Chat en Tiempo Real (5 Endpoints)](#9-messaging---mensajería-y-chat-en-tiempo-real)
10. [Consultations - Asesor Financiero IA (3 Endpoints)](#10-consultations---asesor-financiero-ia)
11. [CRM - Gestión de Prospectos, Timeline y Pruebas de Manejo (6 Endpoints)](#11-crm---gestión-de-prospectos-timeline-y-pruebas-de-manejo)

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

## 3. Catalog - Catálogo de Vehículos, Especificaciones y Marcas

### 3.1 Listar y Buscar Vehículos (Fuzzy Search & Paginación)
* **Método**: `GET` | **Ruta**: `/api/v1/vehicles` | **Acceso**: Público

### 3.2 Listar Mis Vehículos Publicados
* **Método**: `GET` | **Ruta**: `/api/v1/vehicles/my-listings` | **Acceso**: `ROLE_DEALER`, `ROLE_ADMIN`

### 3.3 Listar Marcas Disponibles
* **Método**: `GET` | **Ruta**: `/api/v1/vehicles/brands` | **Acceso**: Público

### 3.4 Registrar Vehículo
* **Método**: `POST` | **Ruta**: `/api/v1/vehicles` | **Acceso**: `ROLE_ADMIN` o `ROLE_DEALER`

### 3.5 Obtener Vehículo por ID
* **Método**: `GET` | **Ruta**: `/api/v1/vehicles/{vehicleId}` | **Acceso**: Público

### 3.6 Actualizar Vehículo
* **Método**: `PUT` | **Ruta**: `/api/v1/vehicles/{vehicleId}` | **Acceso**: Propietario del vehículo

### 3.7 Actualizar Estado del Vehículo (AVAILABLE, RESERVED, SOLD)
* **Método**: `PATCH` | **Ruta**: `/api/v1/vehicles/{vehicleId}/status` | **Acceso**: Propietario del vehículo

```json
// Input Body
{
  "status": "RESERVED"
}
```

### 3.8 Eliminar Vehículo
* **Método**: `DELETE` | **Ruta**: `/api/v1/vehicles/{vehicleId}` | **Acceso**: Propietario del vehículo

### 3.9 Cargar Imagen Principal de Vehículo
* **Método**: `POST` | **Ruta**: `/api/v1/vehicles/{vehicleId}/image` | **Acceso**: Propietario del vehículo

### 3.10 Cargar Múltiples Imágenes de Galería
* **Método**: `POST` | **Ruta**: `/api/v1/vehicles/{vehicleId}/images/multiple` | **Acceso**: Propietario del vehículo

### 3.11 Eliminar Imagen de Galería
* **Método**: `DELETE` | **Ruta**: `/api/v1/vehicles/{vehicleId}/images` | **Acceso**: Propietario del vehículo

---

## 4. Partners - Entidades Financieras, Directorio B2B y SUNAT

### 4.1 Listar Entidades Financieras
* **Método**: `GET` | **Ruta**: `/api/v1/financial-entities` | **Acceso**: Autenticado

### 4.2 Crear Entidad Financiera
* **Método**: `POST` | **Ruta**: `/api/v1/financial-entities` | **Acceso**: `ROLE_ADMIN`, `ROLE_FINANCIAL_INSTITUTION`

### 4.3 Obtener Entidad Financiera por ID
* **Método**: `GET` | **Ruta**: `/api/v1/financial-entities/{id}` | **Acceso**: Autenticado

### 4.4 Actualizar Entidad Financiera
* **Método**: `PUT` | **Ruta**: `/api/v1/financial-entities/{id}` | **Acceso**: `ROLE_ADMIN`, `ROLE_FINANCIAL_INSTITUTION`

### 4.5 Eliminar Entidad Financiera
* **Método**: `DELETE` | **Ruta**: `/api/v1/financial-entities/{id}` | **Acceso**: `ROLE_ADMIN`

### 4.6 Consulta SUNAT RUC
* **Método**: `GET` | **Ruta**: `/api/v1/partners/sunat/ruc/{ruc}` | **Acceso**: Autenticado

### 4.7 Directorio Público de Concesionarias (Paginado & Búsqueda)
* **Método**: `GET` | **Ruta**: `/api/v1/dealerships` | **Acceso**: Público

### 4.8 Obtener Mi Concesionaria B2B
* **Método**: `GET` | **Ruta**: `/api/v1/dealerships/me` | **Acceso**: `ROLE_DEALER`, `ROLE_ADMIN`

### 4.9 Crear o Actualizar Mi Concesionaria B2B
* **Método**: `PUT` | **Ruta**: `/api/v1/dealerships/me` | **Acceso**: `ROLE_DEALER`, `ROLE_ADMIN`

### 4.10 Cargar Logo de Concesionaria
* **Método**: `POST` | **Ruta**: `/api/v1/dealerships/me/logo` | **Acceso**: `ROLE_DEALER`, `ROLE_ADMIN`

### 4.11 Cargar Banner de Concesionaria
* **Método**: `POST` | **Ruta**: `/api/v1/dealerships/me/banner` | **Acceso**: `ROLE_DEALER`, `ROLE_ADMIN`

### 4.12 Obtener Concesionaria por ID
* **Método**: `GET` | **Ruta**: `/api/v1/dealerships/{id}` | **Acceso**: Público

### 4.13 Obtener Vehículos de una Concesionaria Específica
* **Método**: `GET` | **Ruta**: `/api/v1/dealerships/{id}/vehicles` | **Acceso**: Público

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

### 5.5 Enviar Solicitud de Crédito Formal a Banco
* **Método**: `POST` | **Ruta**: `/api/v1/credit-applications` | **Acceso**: Autenticado

```json
// Input Body
{
  "simulationId": "d1e2f3a4-5678-90ab-cdef-1234567890ab",
  "financialEntityId": "b1c2d3e4-f5a6-7b8c-9d0e-112233445566",
  "vehicleId": "c9d8e7f6-5432-1098-7654-3210fe210987",
  "requestedAmount": 27920.00,
  "currency": "USD",
  "notes": "Adjunto sustento de ingresos"
}
```

### 5.6 Listar Mis Solicitudes de Crédito
* **Método**: `GET` | **Ruta**: `/api/v1/credit-applications/me` | **Acceso**: Autenticado

### 5.7 Obtener Solicitud de Crédito por ID
* **Método**: `GET` | **Ruta**: `/api/v1/credit-applications/{id}` | **Acceso**: Autenticado

### 5.8 Actualizar Estado de Solicitud de Crédito (SUBMITTED, UNDER_REVIEW, APPROVED, REJECTED, DISBURSED)
* **Método**: `PATCH` | **Ruta**: `/api/v1/credit-applications/{id}/status` | **Acceso**: `ROLE_FINANCIAL_INSTITUTION`, `ROLE_ADMIN`

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

### 9.2 Historial de Mensajes de una Conversación
* **Método**: `GET` | **Ruta**: `/api/v1/conversations/{id}/messages` | **Acceso**: Autenticado

### 9.3 Iniciar Nueva Conversación
* **Método**: `POST` | **Ruta**: `/api/v1/conversations` | **Acceso**: Autenticado

```json
// Input Body
{
  "recipientUserId": "dealer-user-777",
  "vehicleId": "c9d8e7f6-5432-1098-7654-3210fe210987",
  "initialMessage": "Hola, estoy interesado en este vehículo."
}
```

### 9.4 Enviar Mensaje en Conversación Existente (REST Fallback)
* **Método**: `POST` | **Ruta**: `/api/v1/conversations/{id}/messages` | **Acceso**: Autenticado

### 9.5 Suscripción WebSocket STOMP para Chat en Tiempo Real
* **Protocolo**: `STOMP via WebSocket` | **Endpoint Handshake**: `/ws/chat`
* **Topic Suscripción**: `/topic/conversations/{conversationId}`
* **Destination Envío**: `/app/chat.sendMessage`

---

## 10. Consultations - Asesor Financiero IA

### 10.1 Enviar Consulta Interactiva al Asesor Financiero IA
* **Método**: `POST` | **Ruta**: `/api/v1/consultations/chat` | **Acceso**: Autenticado

```json
// Input Body
{
  "message": "Que auto me conviene con un ingreso de S/ 4500 al mes?"
}
```
```json
// Response (HTTP 200 OK)
{
  "sessionId": "s-12345",
  "reply": "Con tu ingreso mensual de S/ 4500, te sugiero evaluar cuotas no mayores a S/ 1350...",
  "recommendedVehicleIds": ["c9d8e7f6-5432-1098-7654-3210fe210987"]
}
```

### 10.2 Historial de Consultas IA del Usuario
* **Método**: `GET` | **Ruta**: `/api/v1/consultations/history` | **Acceso**: Autenticado

### 10.3 Obtener Recomendaciones de Vehículos IA
* **Método**: `GET` | **Ruta**: `/api/v1/consultations/recommendations` | **Acceso**: Autenticado

---

## 11. CRM - Gestión de Prospectos, Timeline y Pruebas de Manejo

### 11.1 Listar Prospectos del Concesionario
* **Método**: `GET` | **Ruta**: `/api/v1/dealers/me/prospects` | **Acceso**: `ROLE_DEALER`, `ROLE_ADMIN`

### 11.2 Obtener Prospecto por ID
* **Método**: `GET` | **Ruta**: `/api/v1/dealers/me/prospects/{id}` | **Acceso**: `ROLE_DEALER`, `ROLE_ADMIN`

### 11.3 Agregar Nota al Timeline de un Prospecto
* **Método**: `POST` | **Ruta**: `/api/v1/prospects/{id}/notes` | **Acceso**: `ROLE_DEALER`, `ROLE_ADMIN`

```json
// Input Body
{
  "noteText": "Cliente solicitó evaluación para financiamiento a 48 meses."
}
```

### 11.4 Obtener Timeline de Notas de un Prospecto
* **Método**: `GET` | **Ruta**: `/api/v1/prospects/{id}/timeline` | **Acceso**: `ROLE_DEALER`, `ROLE_ADMIN`

### 11.5 Actualizar Estado CRM del Prospecto
* **Método**: `PATCH` | **Ruta**: `/api/v1/prospects/{id}/status` | **Acceso**: `ROLE_DEALER`, `ROLE_ADMIN`

```json
// Input Body
{
  "status": "NEGOTIATING"
}
```

### 11.6 Programar Prueba de Manejo (Test Drive)
* **Método**: `POST` | **Ruta**: `/api/v1/test-drives` | **Acceso**: Autenticado

```json
// Input Body
{
  "vehicleId": "c9d8e7f6-5432-1098-7654-3210fe210987",
  "dealershipId": "a1b2c3d4-e5f6-7a8b-9c0d-112233445566",
  "scheduledDateTime": "2026-10-15T10:00:00Z",
  "notes": "Prueba de manejo turno mañana"
}
```
