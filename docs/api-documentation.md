# 📚 Guía Completa de la API REST - SmartFinance Drive Platform

Documentación técnica y exhaustiva de todos los endpoints de **SmartFinance Drive Platform**. La plataforma está construida bajo una arquitectura orientada a **Domain-Driven Design (DDD)** y **CQRS**.

---

## 🌐 Información General

* **Servidor en Producción (Render)**: `https://smartfinance-drive-platform.onrender.com`
* **Swagger UI**: [https://smartfinance-drive-platform.onrender.com/swagger-ui/index.html](https://smartfinance-drive-platform.onrender.com/swagger-ui/index.html)
* **OpenAPI Specs (JSON)**: `https://smartfinance-drive-platform.onrender.com/v3/api-docs`

---

## 🔑 Autenticación y Seguridad

Para endpoints protegidos, incluye la cabecera HTTP:

```http
Authorization: Bearer <tu_access_token_jwt>
```

---

## 🗺️ Índice de Bounded Contexts

1. [IAM - Autenticación y Usuarios (12 Endpoints)](#1-iam---autenticación-y-usuarios)
2. [Profiles - Perfiles de Cliente (5 Endpoints)](#2-profiles---perfiles-de-cliente)
3. [Catalog - Catálogo de Vehículos (7 Endpoints)](#3-catalog---catálogo-de-vehículos)
4. [Partners - Entidades Financieras y SUNAT (7 Endpoints)](#4-partners---entidades-financieras-y-sunat)
5. [Financing - Simulaciones de Crédito (4 Endpoints)](#5-financing---simulaciones-de-crédito)
6. [Scoring - Evaluación Crediticia (5 Endpoints)](#6-scoring---evaluación-crediticia)
7. [Projections - Depreciación de Vehículos (5 Endpoints)](#7-projections---depreciación-de-vehículos)
8. [Billing - Planes, Suscripciones, Facturas y Stripe (10 Endpoints)](#8-billing---planes-suscripciones-facturas-y-stripe)

---

## 1. IAM - Autenticación y Usuarios

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
```json
// Response (HTTP 200 OK)
{
  "message": "If an account with that email exists, password reset instructions have been processed."
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
```json
// Response (HTTP 200 OK)
{
  "message": "Password reset successfully"
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
```json
// Response (HTTP 200 OK)
{
  "id": 101,
  "username": "juan.perez@gmail.com",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "d7b8c9a0-1234-5678-9abc-def012345678"
}
```

---

### 1.8 Listar Todos los Usuarios (Paginado)
* **Método**: `GET` | **Ruta**: `/api/v1/users?page=0&size=20` | **Acceso**: `ROLE_ADMIN`

```json
// Response (HTTP 200 OK)
{
  "content": [
    { "id": 101, "username": "juan.perez@example.com", "roles": ["ROLE_USER"] }
  ],
  "totalElements": 1,
  "totalPages": 1
}
```

---

### 1.9 Obtener Usuario por ID
* **Método**: `GET` | **Ruta**: `/api/v1/users/{userId}` | **Acceso**: `ROLE_ADMIN` o Mismo usuario

```json
// Response (HTTP 200 OK)
{
  "id": 101,
  "username": "juan.perez@example.com",
  "roles": ["ROLE_USER"]
}
```

---

### 1.10 Actualizar Rol de Usuario
* **Método**: `PUT` | **Ruta**: `/api/v1/users/{userId}/roles` | **Acceso**: `ROLE_ADMIN`

```json
// Input Body
{
  "role": "ROLE_DEALER"
}
```
```json
// Response (HTTP 200 OK)
{
  "id": 101,
  "username": "juan.perez@example.com",
  "roles": ["ROLE_DEALER"]
}
```

---

### 1.11 Solicitar Rol de Concesionario (DEALER) via RUC
* **Método**: `POST` | **Ruta**: `/api/v1/users/{userId}/dealer-role-requests` | **Acceso**: Mismo usuario o `ROLE_ADMIN`

```json
// Input Body
{
  "ruc": "20601234567"
}
```
```json
// Response (HTTP 200 OK)
{
  "id": 101,
  "username": "juan.perez@example.com",
  "roles": ["ROLE_USER", "ROLE_DEALER"]
}
```

---

### 1.12 Solicitar Rol de Entidad Financiera (FINANCIAL_INSTITUTION) via RUC
* **Método**: `POST` | **Ruta**: `/api/v1/users/{userId}/financial-institution-role-requests` | **Acceso**: Mismo usuario o `ROLE_ADMIN`

```json
// Input Body
{
  "ruc": "20100047218"
}
```
```json
// Response (HTTP 200 OK)
{
  "id": 101,
  "username": "bcp@example.com",
  "roles": ["ROLE_USER", "ROLE_FINANCIAL_INSTITUTION"]
}
```

---

## 2. Profiles - Perfiles de Cliente

### 2.1 Crear Perfil de Cliente
* **Método**: `POST` | **Ruta**: `/api/v1/profiles` | **Acceso**: Autenticado

```json
// Input Body
{
  "firstName": "Juan",
  "lastName": "Pérez",
  "email": "juan.perez@example.com",
  "dni": "72819203",
  "phoneNumber": "+51987654321",
  "monthlyIncomeAmount": 4500.00,
  "currency": "PEN"
}
```
```json
// Response (HTTP 201 Created)
{
  "id": "a1b2c3d4-e5f6-7a8b-9c0d-112233445566",
  "userId": "101",
  "firstName": "Juan",
  "lastName": "Pérez",
  "email": "juan.perez@example.com",
  "dni": "72819203",
  "phoneNumber": "+51987654321",
  "monthlyIncomeAmount": 4500.00,
  "currency": "PEN"
}
```

---

### 2.2 Obtener Perfil por ID
* **Método**: `GET` | **Ruta**: `/api/v1/profiles/{profileId}` | **Acceso**: Propietario o `ROLE_ADMIN`

```json
// Response (HTTP 200 OK)
{
  "id": "a1b2c3d4-e5f6-7a8b-9c0d-112233445566",
  "userId": "101",
  "firstName": "Juan",
  "lastName": "Pérez",
  "email": "juan.perez@example.com",
  "dni": "72819203",
  "monthlyIncomeAmount": 4500.00,
  "currency": "PEN"
}
```

---

### 2.3 Obtener Perfil por User ID
* **Método**: `GET` | **Ruta**: `/api/v1/profiles/users/{userId}` | **Acceso**: Propietario o `ROLE_ADMIN`

```json
// Response (HTTP 200 OK)
{
  "id": "a1b2c3d4-e5f6-7a8b-9c0d-112233445566",
  "userId": "101",
  "firstName": "Juan",
  "lastName": "Pérez"
}
```

---

### 2.4 Actualizar Perfil
* **Método**: `PUT` | **Ruta**: `/api/v1/profiles/{profileId}` | **Acceso**: Propietario o `ROLE_ADMIN`

```json
// Input Body
{
  "firstName": "Juan Carlos",
  "lastName": "Pérez Prado",
  "email": "juan.perez@example.com",
  "dni": "72819203",
  "phoneNumber": "+51999888777",
  "monthlyIncomeAmount": 5500.00,
  "currency": "PEN"
}
```
```json
// Response (HTTP 200 OK)
{
  "id": "a1b2c3d4-e5f6-7a8b-9c0d-112233445566",
  "firstName": "Juan Carlos",
  "monthlyIncomeAmount": 5500.00
}
```

---

### 2.5 Eliminar Perfil
* **Método**: `DELETE` | **Ruta**: `/api/v1/profiles/{profileId}` | **Acceso**: Propietario o `ROLE_ADMIN`

```http
Response: HTTP 204 No Content
```

---

## 3. Catalog - Catálogo de Vehículos

### 3.1 Listar y Buscar Vehículos (Búsqueda Inteligente & Paginación)
* **Método**: `GET` | **Ruta**: `/api/v1/vehicles` | **Acceso**: Público
* **Query Params**: `brand`, `model`, `minPrice`, `maxPrice`, `minYear`, `maxYear`, `condition`, `page`, `size`, `sort`.

```json
// Response (HTTP 200 OK)
{
  "content": [
    {
      "id": "f8c9b0a1-2345-6789-abcd-ef0123456789",
      "userId": "101",
      "financialEntityId": "b1c2d3e4-f5a6-7b8c-9d0e-112233445566",
      "brand": "Toyota",
      "model": "Corolla",
      "manufactureYear": 2024,
      "condition": "NEW",
      "priceAmount": 22500.00,
      "currency": "USD",
      "imagePath": "https://res.cloudinary.com/dtczrhrm/image/upload/v1/smartfinance/vehicles/corolla.jpg"
    }
  ],
  "totalElements": 1,
  "totalPages": 1
}
```

---

### 3.2 Registrar Vehículo
* **Método**: `POST` | **Ruta**: `/api/v1/vehicles` | **Acceso**: `ROLE_ADMIN` o `ROLE_DEALER`

```json
// Input Body
{
  "financialEntityId": "b1c2d3e4-f5a6-7b8c-9d0e-112233445566",
  "brand": "Toyota",
  "model": "RAV4",
  "manufactureYear": 2024,
  "condition": "NEW",
  "priceAmount": 34900.00,
  "currency": "USD"
}
```
```json
// Response (HTTP 201 Created)
{
  "id": "c9d8e7f6-5432-1098-7654-3210fe210987",
  "brand": "Toyota",
  "model": "RAV4",
  "priceAmount": 34900.00
}
```

---

### 3.3 Obtener Vehículo por ID
* **Método**: `GET` | **Ruta**: `/api/v1/vehicles/{vehicleId}` | **Acceso**: Propietario o `ROLE_ADMIN`

```json
// Response (HTTP 200 OK)
{
  "id": "c9d8e7f6-5432-1098-7654-3210fe210987",
  "brand": "Toyota",
  "model": "RAV4",
  "priceAmount": 34900.00
}
```

---

### 3.4 Vehículos de un Usuario
* **Método**: `GET` | **Ruta**: `/api/v1/vehicles/users/{userId}` | **Acceso**: Mismo usuario o `ROLE_ADMIN`

```json
// Response (HTTP 200 OK)
[
  {
    "id": "c9d8e7f6-5432-1098-7654-3210fe210987",
    "brand": "Toyota",
    "model": "RAV4"
  }
]
```

---

### 3.5 Actualizar Vehículo
* **Método**: `PUT` | **Ruta**: `/api/v1/vehicles/{vehicleId}` | **Acceso**: Propietario del vehículo

```json
// Input Body
{
  "financialEntityId": "b1c2d3e4-f5a6-7b8c-9d0e-112233445566",
  "brand": "Toyota",
  "model": "RAV4 Hybrid",
  "manufactureYear": 2024,
  "condition": "NEW",
  "priceAmount": 37900.00,
  "currency": "USD"
}
```
```json
// Response (HTTP 200 OK)
{
  "id": "c9d8e7f6-5432-1098-7654-3210fe210987",
  "model": "RAV4 Hybrid",
  "priceAmount": 37900.00
}
```

---

### 3.6 Eliminar Vehículo
* **Método**: `DELETE` | **Ruta**: `/api/v1/vehicles/{vehicleId}` | **Acceso**: Propietario del vehículo

```http
Response: HTTP 204 No Content
```

---

### 3.7 Cargar Imagen de Vehículo
* **Método**: `POST` | **Ruta**: `/api/v1/vehicles/{vehicleId}/image` | **Acceso**: Propietario del vehículo
* **Content-Type**: `multipart/form-data` | Form Param: `file`

```json
// Response (HTTP 200 OK)
{
  "id": "c9d8e7f6-5432-1098-7654-3210fe210987",
  "imagePath": "https://res.cloudinary.com/dtczrhrm/image/upload/v1/smartfinance/vehicles/rav4.jpg"
}
```

---

## 4. Partners - Entidades Financieras y SUNAT

### 4.1 Crear Entidad Financiera
* **Método**: `POST` | **Ruta**: `/api/v1/financial-entities` | **Acceso**: `ROLE_ADMIN`, `ROLE_FINANCIAL_INSTITUTION`

```json
// Input Body
{
  "name": "Banco de Crédito del Perú (BCP)",
  "ruc": "20100047218"
}
```
```json
// Response (HTTP 201 Created)
{
  "id": "b1c2d3e4-f5a6-7b8c-9d0e-112233445566",
  "name": "Banco de Crédito del Perú (BCP)",
  "ruc": "20100047218",
  "rateBenchmarks": []
}
```

---

### 4.2 Listar Entidades Financieras
* **Método**: `GET` | **Ruta**: `/api/v1/financial-entities` | **Acceso**: Autenticado

```json
// Response (HTTP 200 OK)
[
  {
    "id": "b1c2d3e4-f5a6-7b8c-9d0e-112233445566",
    "name": "Banco de Crédito del Perú (BCP)",
    "rateBenchmarks": [
      { "loanTermMonths": 36, "annualEffectiveRate": 9.50 }
    ]
  }
]
```

---

### 4.3 Obtener Entidad Financiera por ID
* **Método**: `GET` | **Ruta**: `/api/v1/financial-entities/{id}` | **Acceso**: Autenticado

```json
// Response (HTTP 200 OK)
{
  "id": "b1c2d3e4-f5a6-7b8c-9d0e-112233445566",
  "name": "Banco de Crédito del Perú (BCP)"
}
```

---

### 4.4 Agregar Benchmark de Tasa a Entidad
* **Método**: `POST` | **Ruta**: `/api/v1/financial-entities/{id}/rate-benchmarks` | **Acceso**: `ROLE_ADMIN`, `ROLE_FINANCIAL_INSTITUTION`

```json
// Input Body
{
  "loanTermMonths": 36,
  "annualEffectiveRate": 9.50,
  "monthlyCreditLifeInsuranceRate": 0.05
}
```
```json
// Response (HTTP 201 Created)
{
  "id": "b1c2d3e4-f5a6-7b8c-9d0e-112233445566",
  "name": "Banco de Crédito del Perú (BCP)",
  "rateBenchmarks": [
    { "loanTermMonths": 36, "annualEffectiveRate": 9.50 }
  ]
}
```

---

### 4.5 Actualizar Entidad Financiera
* **Método**: `PUT` | **Ruta**: `/api/v1/financial-entities/{id}` | **Acceso**: `ROLE_ADMIN`, `ROLE_FINANCIAL_INSTITUTION`

```json
// Input Body
{
  "name": "BCP Banco de Crédito",
  "ruc": "20100047218"
}
```
```json
// Response (HTTP 200 OK)
{
  "id": "b1c2d3e4-f5a6-7b8c-9d0e-112233445566",
  "name": "BCP Banco de Crédito"
}
```

---

### 4.6 Eliminar Entidad Financiera
* **Método**: `DELETE` | **Ruta**: `/api/v1/financial-entities/{id}` | **Acceso**: `ROLE_ADMIN`

```http
Response: HTTP 204 No Content
```

---

### 4.7 Consulta SUNAT RUC
* **Método**: `GET` | **Ruta**: `/api/v1/partners/sunat/ruc/{ruc}` | **Acceso**: Autenticado

```json
// Response (HTTP 200 OK)
{
  "ruc": "20100047218",
  "businessName": "BANCO DE CREDITO DEL PERU",
  "status": "ACTIVO",
  "condition": "HABIDO"
}
```

---

## 5. Financing - Simulaciones de Crédito

### 5.1 Crear Simulación de Crédito Vehicular
* **Método**: `POST` | **Ruta**: `/api/v1/simulations` | **Acceso**: Autenticado

```json
// Input Body
{
  "title": "Simulación RAV4",
  "userId": "101",
  "vehicleId": "c9d8e7f6-5432-1098-7654-3210fe210987",
  "financialEntityId": "b1c2d3e4-f5a6-7b8c-9d0e-112233445566",
  "vehiclePriceAmount": 34900.00,
  "currency": "USD",
  "downPaymentPercentage": 20.00,
  "balloonPaymentPercentage": 0.00,
  "annualEffectiveRate": 9.50,
  "monthlyCreditLifeInsuranceRate": 0.05,
  "vehicleInsuranceFeeAmount": 80.00,
  "vehicleInsuranceType": "FULL_COVERAGE",
  "loanTermMonths": 36,
  "gracePeriodType": "NONE",
  "gracePeriodMonths": 0,
  "initialFeesAmount": 150.00,
  "discountRate": 8.00,
  "startDate": "2026-10-01"
}
```
```json
// Response (HTTP 201 Created)
{
  "id": "d1e2f3a4-5678-90ab-cdef-1234567890ab",
  "title": "Simulación RAV4",
  "loanAmount": 27920.00,
  "monthlyPaymentAmount": 895.42,
  "tcea": 11.25,
  "schedule": []
}
```

---

### 5.2 Listar Simulaciones (Paginado)
* **Método**: `GET` | **Ruta**: `/api/v1/simulations` | **Acceso**: Autenticado

```json
// Response (HTTP 200 OK)
{
  "content": [
    { "id": "d1e2f3a4-5678-90ab-cdef-1234567890ab", "title": "Simulación RAV4" }
  ],
  "totalElements": 1,
  "totalPages": 1
}
```

---

### 5.3 Obtener Simulación por ID
* **Método**: `GET` | **Ruta**: `/api/v1/simulations/{id}` | **Acceso**: Autenticado

```json
// Response (HTTP 200 OK)
{
  "id": "d1e2f3a4-5678-90ab-cdef-1234567890ab",
  "title": "Simulación RAV4",
  "schedule": []
}
```

---

### 5.4 Eliminar Simulación
* **Método**: `DELETE` | **Ruta**: `/api/v1/simulations/{id}` | **Acceso**: Autenticado

```http
Response: HTTP 204 No Content
```

---

## 6. Scoring - Evaluación Crediticia

### 6.1 Evaluar Score Crediticio
* **Método**: `POST` | **Ruta**: `/api/v1/credit-scores` | **Acceso**: Autenticado

```json
// Input Body
{
  "profileId": "a1b2c3d4-e5f6-7a8b-9c0d-112233445566"
}
```
```json
// Response (HTTP 201 Created)
{
  "id": "e9f8e7d6-5432-1098-7654-9876543210fe",
  "profileId": "a1b2c3d4-e5f6-7a8b-9c0d-112233445566",
  "score": 750,
  "riskTier": "LOW_RISK",
  "maxRecommendedLoanAmount": 45000.00,
  "currency": "USD"
}
```

---

### 6.2 Listar Todos los Scores Crediticios (Paginado)
* **Método**: `GET` | **Ruta**: `/api/v1/credit-scores` | **Acceso**: Autenticado

```json
// Response (HTTP 200 OK)
{
  "content": [
    { "id": "e9f8e7d6-5432-1098-7654-9876543210fe", "score": 750 }
  ],
  "totalElements": 1
}
```

---

### 6.3 Obtener Score Crediticio por ID
* **Método**: `GET` | **Ruta**: `/api/v1/credit-scores/{id}` | **Acceso**: Propietario o `ROLE_ADMIN`

```json
// Response (HTTP 200 OK)
{
  "id": "e9f8e7d6-5432-1098-7654-9876543210fe",
  "score": 750,
  "riskTier": "LOW_RISK"
}
```

---

### 6.4 Obtener Scores Crediticios por Profile ID
* **Método**: `GET` | **Ruta**: `/api/v1/credit-scores/profile/{profileId}` | **Acceso**: Propietario o `ROLE_ADMIN`

```json
// Response (HTTP 200 OK)
[
  { "id": "e9f8e7d6-5432-1098-7654-9876543210fe", "score": 750 }
]
```

---

### 6.5 Eliminar Score Crediticio
* **Método**: `DELETE` | **Ruta**: `/api/v1/credit-scores/{id}` | **Acceso**: Propietario o `ROLE_ADMIN`

```http
Response: HTTP 204 No Content
```

---

## 7. Projections - Depreciación de Vehículos

### 7.1 Calcular Proyección de Depreciación
* **Método**: `POST` | **Ruta**: `/api/v1/depreciation-projections` | **Acceso**: Autenticado

```json
// Input Body
{
  "vehicleId": "c9d8e7f6-5432-1098-7654-3210fe210987",
  "years": 5
}
```
```json
// Response (HTTP 201 Created)
{
  "id": "11223344-5566-7788-9900-aabbccddeeff",
  "vehicleId": "c9d8e7f6-5432-1098-7654-3210fe210987",
  "initialValue": 34900.00,
  "projectedValues": [
    { "year": 1, "value": 27920.00 }
  ]
}
```

---

### 7.2 Listar Proyecciones (Paginado)
* **Método**: `GET` | **Ruta**: `/api/v1/depreciation-projections` | **Acceso**: Autenticado

```json
// Response (HTTP 200 OK)
{
  "content": [
    { "id": "11223344-5566-7788-9900-aabbccddeeff" }
  ]
}
```

---

### 7.3 Obtener Proyección por ID
* **Método**: `GET` | **Ruta**: `/api/v1/depreciation-projections/{id}` | **Acceso**: Propietario o `ROLE_ADMIN`

```json
// Response (HTTP 200 OK)
{
  "id": "11223344-5566-7788-9900-aabbccddeeff",
  "initialValue": 34900.00
}
```

---

### 7.4 Obtener Proyecciones por Vehicle ID
* **Método**: `GET` | **Ruta**: `/api/v1/depreciation-projections/vehicle/{vehicleId}` | **Acceso**: Propietario o `ROLE_ADMIN`

```json
// Response (HTTP 200 OK)
[
  { "id": "11223344-5566-7788-9900-aabbccddeeff" }
]
```

---

### 7.5 Eliminar Proyección
* **Método**: `DELETE` | **Ruta**: `/api/v1/depreciation-projections/{id}` | **Acceso**: Propietario o `ROLE_ADMIN`

```http
Response: HTTP 204 No Content
```

---

## 8. Billing - Planes, Suscripciones, Facturas y Stripe

### 8.1 Listar Planes Activos
* **Método**: `GET` | **Ruta**: `/api/v1/billing/plans` | **Acceso**: Autenticado

```json
// Response (HTTP 200 OK)
[
  { "id": 1, "name": "Plan Pro Dealer", "price": 49.99, "currency": "USD" }
]
```

---

### 8.2 Obtener Plan por ID
* **Método**: `GET` | **Ruta**: `/api/v1/billing/plans/{planId}` | **Acceso**: Autenticado

```json
// Response (HTTP 200 OK)
{ "id": 1, "name": "Plan Pro Dealer", "price": 49.99 }
```

---

### 8.3 Crear Nuevo Plan
* **Método**: `POST` | **Ruta**: `/api/v1/billing/plans` | **Acceso**: `ROLE_ADMIN`

```json
// Input Body
{
  "name": "Enterprise Dealer Plan",
  "description": "Acceso ilimitado a publicaciones",
  "price": 99.99,
  "currency": "USD",
  "billingCycle": "MONTHLY",
  "maxVehicleListings": 100,
  "maxSimulationsPerMonth": 500,
  "stripePriceId": "price_1P..."
}
```
```json
// Response (HTTP 201 Created)
{ "id": 2, "name": "Enterprise Dealer Plan", "price": 99.99 }
```

---

### 8.4 Obtener Suscripción Actual del Usuario
* **Método**: `GET` | **Ruta**: `/api/v1/billing/subscriptions/me` | **Acceso**: Autenticado

```json
// Response (HTTP 200 OK)
{
  "id": 5,
  "planId": 1,
  "status": "ACTIVE",
  "autoRenew": true
}
```

---

### 8.5 Crear Suscripción Directa
* **Método**: `POST` | **Ruta**: `/api/v1/billing/subscriptions` | **Acceso**: Autenticado

```json
// Input Body
{
  "planId": 1,
  "autoRenew": true
}
```
```json
// Response (HTTP 201 Created)
{ "id": 5, "planId": 1, "status": "ACTIVE" }
```

---

### 8.6 Cancelar Suscripción Activa
* **Método**: `DELETE` | **Ruta**: `/api/v1/billing/subscriptions/{subscriptionId}` | **Acceso**: Autenticado

```json
// Response (HTTP 200 OK)
{ "id": 5, "status": "CANCELLED" }
```

---

### 8.7 Crear Sesión de Stripe Checkout
* **Método**: `POST` | **Ruta**: `/api/v1/billing/subscriptions/checkout-session` | **Acceso**: Autenticado

```json
// Input Body
{
  "stripePriceId": "price_1P...",
  "successUrl": "https://smartfinance-drive-platform.onrender.com/billing/success",
  "cancelUrl": "https://smartfinance-drive-platform.onrender.com/billing/cancel"
}
```
```json
// Response (HTTP 200 OK)
{
  "checkoutUrl": "https://checkout.stripe.com/c/pay/cs_test_a1b2c3d4..."
}
```

---

### 8.8 Facturas del Usuario Actual
* **Método**: `GET` | **Ruta**: `/api/v1/billing/invoices/me` | **Acceso**: Autenticado

```json
// Response (HTTP 200 OK)
[
  { "id": 10, "amount": 49.99, "status": "PAID" }
]
```

---

### 8.9 Pagar / Reconciliar Factura
* **Método**: `PATCH` | **Ruta**: `/api/v1/billing/invoices/{invoiceId}` | **Acceso**: Autenticado

```json
// Response (HTTP 200 OK)
{ "id": 10, "status": "PAID" }
```

---

### 8.10 Webhook Receptor de Eventos Stripe
* **Método**: `POST` | **Ruta**: `/api/v1/billing/webhooks/stripe` | **Acceso**: Público (Verificado por Header `Stripe-Signature`)

```json
// Response (HTTP 200 OK)
"Event received"
```
