# 📚 Documentación de la API REST - SmartFinance Drive Platform

Bienvenido a la documentación técnica de la API REST de **SmartFinance Drive Platform**. Esta API está construida bajo una arquitectura orientada a la **Domain-Driven Design (DDD)** y el patrón **CQRS**, expuesta a través de servicios RESTful seguros e interactivos.

---

## 🌐 Información General

* **Servidor en Producción (Render)**: `https://smartfinance-drive-platform.onrender.com`
* **Swagger UI (Documentación Interactiva)**: [https://smartfinance-drive-platform.onrender.com/swagger-ui/index.html](https://smartfinance-drive-platform.onrender.com/swagger-ui/index.html)
* **OpenAPI Specs (JSON)**: `https://smartfinance-drive-platform.onrender.com/v3/api-docs`

---

## 🔑 Autenticación y Seguridad

La API utiliza autenticación **JWT (JSON Web Token)** sin estado.

### Envió del Token de Autenticación
Para los endpoints protegidos, se debe incluir la cabecera HTTP `Authorization`:

```http
Authorization: Bearer <tu_access_token_jwt>
```

---

## 🗺️ Índice de Bounded Contexts

1. [IAM - Autenticación y Usuarios](#1-iam---autenticación-y-usuarios)
2. [Profiles - Perfiles de Cliente](#2-profiles---perfiles-de-cliente)
3. [Catalog - Catálogo de Vehículos y Búsqueda Inteligente](#3-catalog---catálogo-de-vehículos-y-búsqueda-inteligente)
4. [Partners - Entidades Financieras y Validación SUNAT RUC](#4-partners---entidades-financieras-y-validación-sunat-ruc)
5. [Financing - Simulaciones de Crédito Vehicular](#5-financing---simulaciones-de-crédito-vehicular)
6. [Scoring - Evaluación de Score Crediticio](#6-scoring---evaluación-de-score-crediticio)
7. [Projections - Proyecciones de Depreciación](#7-projections---proyecciones-de-depreciación)
8. [Billing - Suscripciones, Planes y Pagos con Stripe](#8-billing---suscripciones-planes-y-pagos-con-stripe)

---

## 1. IAM - Autenticación y Usuarios

### 1.1 Registrar Nuevo Usuario
Crea una cuenta en la plataforma asignando roles de acceso (`ROLE_USER`, `ROLE_DEALER`, `ROLE_ADMIN`).

* **Método**: `POST`
* **Ruta**: `/api/v1/auth/registrations`
* **Acceso**: Público

#### Entrada (Ejemplo Body):
```json
{
  "username": "juan.perez@example.com",
  "password": "Password123!",
  "roles": ["ROLE_USER"]
}
```

#### Salida (Ejemplo Response - HTTP 201 Created):
```json
{
  "id": "e4a3b2c1-8f9e-4d5c-b6a7-123456789abc",
  "username": "juan.perez@example.com",
  "roles": ["ROLE_USER"]
}
```

---

### 1.2 Iniciar Sesión (Obtener JWT)
Autentica credenciales y emite tokens `token` (access token) y `refreshToken`.

* **Método**: `POST`
* **Ruta**: `/api/v1/auth/sessions`
* **Acceso**: Público

#### Entrada (Ejemplo Body):
```json
{
  "username": "juan.perez@example.com",
  "password": "Password123!"
}
```

#### Salida (Ejemplo Response - HTTP 200 OK):
```json
{
  "id": "e4a3b2c1-8f9e-4d5c-b6a7-123456789abc",
  "username": "juan.perez@example.com",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "d7b8c9a0-1234-5678-9abc-def012345678"
}
```

---

### 1.3 Renovar Token (Refresh Token)
Obtiene un nuevo `access_token` cuando el actual haya expirado.

* **Método**: `POST`
* **Ruta**: `/api/v1/auth/tokens`
* **Acceso**: Público

#### Entrada (Ejemplo Body):
```json
{
  "refreshToken": "d7b8c9a0-1234-5678-9abc-def012345678"
}
```

#### Salida (Ejemplo Response - HTTP 200 OK):
```json
{
  "id": "e4a3b2c1-8f9e-4d5c-b6a7-123456789abc",
  "username": "juan.perez@example.com",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "d7b8c9a0-1234-5678-9abc-def012345678"
}
```

---

### 1.4 Cerrar Sesión (Revocar Token)
Invalida el token JWT activo agregándolo a la lista negra (Blacklist).

* **Método**: `DELETE`
* **Ruta**: `/api/v1/auth/sessions/current`
* **Acceso**: Autenticado (Bearer Token)

#### Salida (Ejemplo Response - HTTP 200 OK):
```json
{
  "message": "User signed out successfully"
}
```

---

### 1.5 Autenticación con Google OAuth2
Inicia sesión verificando un ID Token de Google.

* **Método**: `POST`
* **Ruta**: `/api/v1/auth/google`
* **Acceso**: Público

#### Entrada (Ejemplo Body):
```json
{
  "idToken": "eyJhbGciOiJSUzI1NiIsImtpZCI6..."
}
```

#### Salida (Ejemplo Response - HTTP 200 OK):
```json
{
  "id": "e4a3b2c1-8f9e-4d5c-b6a7-123456789abc",
  "username": "juan.perez@gmail.com",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "d7b8c9a0-1234-5678-9abc-def012345678"
}
```

---

## 2. Profiles - Perfiles de Cliente

### 2.1 Crear Perfil de Cliente
Registra los datos personales y de contacto del usuario autenticado.

* **Método**: `POST`
* **Ruta**: `/api/v1/profiles`
* **Acceso**: Autenticado (`ROLE_USER`, `ROLE_ADMIN`)

#### Entrada (Ejemplo Body):
```json
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

#### Salida (Ejemplo Response - HTTP 201 Created):
```json
{
  "id": "a1b2c3d4-e5f6-7a8b-9c0d-112233445566",
  "userId": "e4a3b2c1-8f9e-4d5c-b6a7-123456789abc",
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

## 3. Catalog - Catálogo de Vehículos y Búsqueda Inteligente

### 3.1 Listar y Buscar Vehículos (Con Búsqueda Difusa y Paginación)
Consulta la flota general de vehículos con soporte para:
* **Filtros**: `brand`, `model`, `minPrice`, `maxPrice`, `minYear`, `maxYear`, `condition` (`NEW` / `USED`).
* **Paginación**: `page`, `size`, `sort`.
* **Motor Fuzzy (Levenshtein + Trigram)**: Tolerancia automática a errores tipográficos (ej. `"toyta"` ➡️ `"Toyota"`).

* **Método**: `GET`
* **Ruta**: `/api/v1/vehicles`
* **Acceso**: Público

#### Ejemplo Request (URL con Query Params):
`GET /api/v1/vehicles?brand=toyta&minPrice=15000&maxPrice=35000&page=0&size=10`

#### Salida (Ejemplo Response - HTTP 200 OK):
```json
{
  "content": [
    {
      "id": "f8c9b0a1-2345-6789-abcd-ef0123456789",
      "userId": "e4a3b2c1-8f9e-4d5c-b6a7-123456789abc",
      "financialEntityId": "b1c2d3e4-f5a6-7b8c-9d0e-112233445566",
      "brand": "Toyota",
      "model": "Corolla Cross",
      "manufactureYear": 2024,
      "condition": "NEW",
      "priceAmount": 28500.00,
      "currency": "USD",
      "imagePath": "https://res.cloudinary.com/dtczrhrm/image/upload/v1/smartfinance/vehicles/corolla.jpg"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10
  },
  "totalElements": 1,
  "totalPages": 1,
  "last": true
}
```

---

### 3.2 Registrar Vehículo
Registra un nuevo vehículo en el catálogo.

* **Método**: `POST`
* **Ruta**: `/api/v1/vehicles`
* **Acceso**: Autenticado (`ROLE_ADMIN`, `ROLE_DEALER`)

#### Entrada (Ejemplo Body):
```json
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

#### Salida (Ejemplo Response - HTTP 201 Created):
```json
{
  "id": "c9d8e7f6-5432-1098-7654-3210fe210987",
  "userId": "e4a3b2c1-8f9e-4d5c-b6a7-123456789abc",
  "financialEntityId": "b1c2d3e4-f5a6-7b8c-9d0e-112233445566",
  "brand": "Toyota",
  "model": "RAV4",
  "manufactureYear": 2024,
  "condition": "NEW",
  "priceAmount": 34900.00,
  "currency": "USD",
  "imagePath": null
}
```

---

### 3.3 Subir Imagen de Vehículo
Carga una imagen del vehículo a Cloudinary.

* **Método**: `POST`
* **Ruta**: `/api/v1/vehicles/{vehicleId}/image`
* **Content-Type**: `multipart/form-data`
* **Acceso**: Autenticado (Propietario del vehículo)

#### Entrada (Form Data):
* `file`: Archivo binario de imagen (`.jpg`, `.png`).

#### Salida (Ejemplo Response - HTTP 200 OK):
```json
{
  "id": "c9d8e7f6-5432-1098-7654-3210fe210987",
  "brand": "Toyota",
  "model": "RAV4",
  "imagePath": "https://res.cloudinary.com/dtczrhrm/image/upload/v1726300000/smartfinance/vehicles/rav4.jpg"
}
```

---

## 4. Partners - Entidades Financieras y Validación SUNAT RUC

### 4.1 Consulta de RUC en SUNAT
Valida el RUC peruano de una empresa contra los padrones oficiales de SUNAT.

* **Método**: `GET`
* **Ruta**: `/api/v1/partners/sunat/ruc/{ruc}`
* **Acceso**: Autenticado

#### Ejemplo Request:
`GET /api/v1/partners/sunat/ruc/20601234567`

#### Salida (Ejemplo Response - HTTP 200 OK):
```json
{
  "ruc": "20601234567",
  "businessName": "BANCO DE CREDITO DEL PERU",
  "status": "ACTIVO",
  "condition": "HABIDO",
  "address": "AV. CENTENARIO NRO. 156 LA MOLINA",
  "department": "LIMA",
  "province": "LIMA",
  "district": "LA MOLINA"
}
```

---

## 5. Financing - Simulaciones de Crédito Vehicular

### 5.1 Generar Simulación de Crédito Vehicular
Calcula el plan de pagos completo (Francés/Alemán), TEA, TCEA, seguro desgravamen y periodos de gracia.

* **Método**: `POST`
* **Ruta**: `/api/v1/simulations`
* **Acceso**: Autenticado

#### Entrada (Ejemplo Body):
```json
{
  "title": "Simulación Toyota RAV4 2024",
  "userId": "e4a3b2c1-8f9e-4d5c-b6a7-123456789abc",
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

#### Salida (Ejemplo Response - HTTP 201 Created):
```json
{
  "id": "d1e2f3a4-5678-90ab-cdef-1234567890ab",
  "title": "Simulación Toyota RAV4 2024",
  "loanAmount": 27920.00,
  "currency": "USD",
  "monthlyPaymentAmount": 895.42,
  "tcea": 11.25,
  "van": 1420.50,
  "tir": 0.88,
  "schedule": [
    {
      "periodNumber": 1,
      "dueDate": "2026-11-01",
      "initialBalance": 27920.00,
      "principal": 674.20,
      "interest": 221.03,
      "creditLifeInsurance": 13.96,
      "vehicleInsurance": 80.00,
      "totalInstallment": 989.19,
      "finalBalance": 27245.80
    }
  ]
}
```

---

## 6. Scoring - Evaluación de Score Crediticio

### 6.1 Evaluar Score Crediticio
Evalúa la capacidad de pago y riesgo crediticio del cliente (Scoring 300 - 850).

* **Método**: `POST`
* **Ruta**: `/api/v1/credit-scores/evaluate`
* **Acceso**: Autenticado

#### Entrada (Ejemplo Body):
```json
{
  "profileId": "a1b2c3d4-e5f6-7a8b-9c0d-112233445566"
}
```

#### Salida (Ejemplo Response - HTTP 200 OK):
```json
{
  "id": "e9f8e7d6-5432-1098-7654-9876543210fe",
  "profileId": "a1b2c3d4-e5f6-7a8b-9c0d-112233445566",
  "score": 750,
  "riskTier": "LOW_RISK",
  "maxRecommendedLoanAmount": 45000.00,
  "currency": "USD",
  "evaluatedAt": "2026-09-14T09:15:00Z"
}
```

---

## 7. Projections - Proyecciones de Depreciación

### 7.1 Calcular Proyección de Depreciación de Vehículo
Calcula la curva de desvalorización del vehículo a lo largo de los años.

* **Método**: `POST`
* **Ruta**: `/api/v1/depreciation-projections/calculate`
* **Acceso**: Autenticado

#### Entrada (Ejemplo Body):
```json
{
  "vehicleId": "c9d8e7f6-5432-1098-7654-3210fe210987",
  "years": 5
}
```

#### Salida (Ejemplo Response - HTTP 201 Created):
```json
{
  "id": "11223344-5566-7788-9900-aabbccddeeff",
  "vehicleId": "c9d8e7f6-5432-1098-7654-3210fe210987",
  "initialValue": 34900.00,
  "projectedValues": [
    { "year": 1, "value": 27920.00, "depreciationPercentage": 20.0 },
    { "year": 2, "value": 23732.00, "depreciationPercentage": 32.0 },
    { "year": 3, "value": 20172.20, "depreciationPercentage": 42.2 },
    { "year": 4, "value": 17146.37, "depreciationPercentage": 50.8 },
    { "year": 5, "value": 14574.41, "depreciationPercentage": 58.2 }
  ]
}
```

---

## 8. Billing - Suscripciones, Planes y Pagos con Stripe

### 8.1 Crear Sesión de Checkout en Stripe
Inicia una transacción segura en Stripe para adquirir un plan de suscripción.

* **Método**: `POST`
* **Ruta**: `/api/v1/billing/subscriptions/checkout`
* **Acceso**: Autenticado

#### Entrada (Ejemplo Body):
```json
{
  "planId": "PLAN_PRO_MONTHLY",
  "successUrl": "https://smartfinance-drive-platform.onrender.com/billing/success",
  "cancelUrl": "https://smartfinance-drive-platform.onrender.com/billing/cancel"
}
```

#### Salida (Ejemplo Response - HTTP 200 OK):
```json
{
  "sessionId": "cs_test_a1b2c3d4e5f6g7h8i9j0",
  "checkoutUrl": "https://checkout.stripe.com/c/pay/cs_test_a1b2c3d4e5f6g7h8i9j0"
}
```

---

### 8.2 Webhook de Stripe
Endpoint receptor de eventos en tiempo real de Stripe (`checkout.session.completed`, `invoice.payment_succeeded`).

* **Método**: `POST`
* **Ruta**: `/api/v1/billing/stripe/webhook`
* **Headers**: `Stripe-Signature: t=...,v1=...`
* **Acceso**: Público (Validado por firma criptográfica de Stripe)

#### Salida (Ejemplo Response - HTTP 200 OK):
```json
{
  "status": "success",
  "event": "checkout.session.completed"
}
```
