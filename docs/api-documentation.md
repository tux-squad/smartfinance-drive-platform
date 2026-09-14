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

#### Descripción
Crea y registra una nueva cuenta de usuario en el sistema IAM asignando su correo electrónico, contraseña con encriptación BCrypt y los roles de seguridad requeridos (`ROLE_USER`, `ROLE_DEALER`, `ROLE_ADMIN`).

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

#### Descripción
Autentica las credenciales del usuario (usuario y contraseña) y emite un par de tokens JWT firmados: un `token` de acceso de corta duración y un `refreshToken` para renovación.

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

#### Descripción
Permite obtener un nuevo token de acceso JWT válido utilizando un token de refresco (`refreshToken`) previamente emitido, evitando que el usuario deba reingresar sus credenciales.

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

#### Descripción
Revoca e invalida el token JWT activo del usuario actual añadiendo su identificador único (JTI) a la lista negra en memoria del servidor (Token Blacklist).

```json
// Response (HTTP 200 OK)
{
  "message": "User signed out successfully"
}
```

---

### 1.5 Solicitar Recuperación de Contraseña
* **Método**: `POST` | **Ruta**: `/api/v1/auth/password-recoveries` | **Acceso**: Público

#### Descripción
Inicia el flujo de recuperación de contraseña para un usuario. Genera de forma segura un token de restablecimiento de contraseña temporal sin enumerar ni exponer datos de cuentas registradas.

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

#### Descripción
Permite al usuario establecer una nueva contraseña de acceso utilizando el token de recuperación recibido en su correo electrónico.

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

#### Descripción
Verifica un token de identidad (`idToken`) de Google OAuth2 emitido desde el cliente web o móvil, crea la cuenta del usuario si es su primer ingreso y genera los tokens JWT de la plataforma.

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

#### Descripción
Obtiene un listado paginado de todas las cuentas de usuario registradas en el sistema IAM. Exclusivo para administradores.

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

#### Descripción
Retorna la información detallada de una cuenta de usuario específica mediante su identificador numérico.

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

#### Descripción
Permite a un administrador del sistema cambiar o asignar un nuevo rol de seguridad (`ROLE_USER`, `ROLE_DEALER`, `ROLE_FINANCIAL_INSTITUTION`, `ROLE_ADMIN`) a un usuario.

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

#### Descripción
Procesa la solicitud de actualización del rol del usuario a Concesionario (`ROLE_DEALER`), verificando en línea la validez de su número de RUC con los padrones oficiales de SUNAT.

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

#### Descripción
Valida el RUC institucional con la base de datos de SUNAT y actualiza el rol de la cuenta a Entidad Financiera (`ROLE_FINANCIAL_INSTITUTION`).

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

#### Descripción
Crea y asocia un perfil de cliente comercial con los datos personales, documento de identidad (DNI/RUC), teléfono de contacto e ingresos mensuales del usuario autenticado.

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

#### Descripción
Obtiene los detalles completos del perfil de un cliente consultando mediante su identificador UUID.

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

#### Descripción
Permite encontrar el perfil de cliente asociado a una cuenta de usuario específica mediante su identificador de usuario (`userId`).

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

#### Descripción
Actualiza los datos personales, número telefónico e ingresos mensuales declarados en el perfil de cliente.

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

#### Descripción
Elimina permanentemente del sistema el perfil de cliente correspondiente al UUID especificado.

```http
Response: HTTP 204 No Content
```

---

## 3. Catalog - Catálogo de Vehículos

### 3.1 Listar y Buscar Vehículos (Búsqueda Inteligente & Paginación)
* **Método**: `GET` | **Ruta**: `/api/v1/vehicles` | **Acceso**: Público
* **Query Params**: `brand`, `model`, `minPrice`, `maxPrice`, `minYear`, `maxYear`, `condition`, `page`, `size`, `sort`.

#### Descripción
Endpoint principal del catálogo de la plataforma. Realiza búsquedas paginadas y filtradas de vehículos. Incluye un motor de **búsqueda difusa de 3 capas (Levenshtein + Trigram)** que corrige automáticamente errores ortográficos en marca/modelo y relaja filtros si no hay resultados directos.

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

#### Descripción
Registra una nueva unidad vehicular en el catálogo público asociándola a la entidad financiera conveniente y asignándole el ID del usuario autenticado.

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

#### Descripción
Retorna los datos y especificaciones técnicas completas de un vehículo registrado mediante su UUID.

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

#### Descripción
Lista todas las unidades vehiculares registradas en el catálogo por un usuario o concesionario específico.

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

#### Descripción
Actualiza las especificaciones, modelo, precio o condición de una publicación de vehículo existente.

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

#### Descripción
Remueve permanentemente del catálogo la publicación de la unidad vehicular especificada.

```http
Response: HTTP 204 No Content
```

---

### 3.7 Cargar Imagen de Vehículo
* **Método**: `POST` | **Ruta**: `/api/v1/vehicles/{vehicleId}/image` | **Acceso**: Propietario del vehículo
* **Content-Type**: `multipart/form-data` | Form Param: `file`

#### Descripción
Recibe un archivo de imagen (`.jpg`, `.png`), lo procesa y almacena en Cloudinary, y actualiza la URL (`imagePath`) en la ficha del vehículo en la base de datos.

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

#### Descripción
Registra un nuevo banco o entidad financiera aliada para ofertar planes de financiamiento vehicular.

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

#### Descripción
Devuelve el listado completo de entidades financieras y bancos registrados en la plataforma junto con sus tasas de referencia.

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

#### Descripción
Obtiene la información detallada de una entidad financiera mediante su UUID.

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

#### Descripción
Agrega una nueva estructura de referencia de tasa de interés efectiva anual (TEA) y seguro de desgravamen según plazo a una entidad financiera.

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

#### Descripción
Modifica la razón social o RUC de una entidad financiera registrada.

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

#### Descripción
Elimina la entidad financiera seleccionada. Exclusivo para administradores.

```http
Response: HTTP 204 No Content
```

---

### 4.7 Consulta SUNAT RUC
* **Método**: `GET` | **Ruta**: `/api/v1/partners/sunat/ruc/{ruc}` | **Acceso**: Autenticado

#### Descripción
Consulta de forma síncrona los datos de contribuyente de un RUC (razón social, estado de contribuyente "ACTIVO", condición "HABIDO") contra los servicios de validación tributaria peruana SUNAT.

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

#### Descripción
Genera una simulación completa de crédito vehicular. Calcula la cuota inicial, saldo a financiar, cronograma detallado de cuotas (método francés/alemán), seguro desgravamen, seguro vehicular, VAN, TIR, TEA y TCEA efectiva.

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
* **Método**: `GET` | **Ruta**: `/api/v1/simulations` | **Acceso**: Propietario de la simulación o `ROLE_ADMIN`

#### Descripción
Obtiene una lista paginada de todas las simulaciones de crédito realizadas por el usuario autenticado.

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
* **Método**: `GET` | **Ruta**: `/api/v1/simulations/{id}` | **Acceso**: Propietario de la simulación o `ROLE_ADMIN`

#### Descripción
Devuelve el desglose completo de una simulación guardada, incluyendo el cronograma mes a mes con intereses, amortizaciones y seguros.

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
* **Método**: `DELETE` | **Ruta**: `/api/v1/simulations/{id}` | **Acceso**: Propietario de la simulación o `ROLE_ADMIN`

#### Descripción
Elimina del registro la simulación de crédito seleccionada.

```http
Response: HTTP 204 No Content
```

---

## 6. Scoring - Evaluación Crediticia

### 6.1 Evaluar Score Crediticio
* **Método**: `POST` | **Ruta**: `/api/v1/credit-scores` | **Acceso**: Autenticado

#### Descripción
Ejecuta el motor de scoring crediticio sobre el perfil de un cliente, calculando su puntaje de riesgo (300 a 850), categoría de riesgo (`LOW_RISK`, `MEDIUM_RISK`, `HIGH_RISK`) y monto máximo de crédito recomendado.

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
* **Método**: `GET` | **Ruta**: `/api/v1/credit-scores` | **Acceso**: Propietario o `ROLE_ADMIN`

#### Descripción
Listado paginado de todas las evaluaciones crediticias pertenecientes al cliente o administradas.

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

#### Descripción
Obtiene el informe detallado de un resultado de evaluación crediticia por su identificador UUID.

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

#### Descripción
Devuelve el historial de evaluaciones crediticias asociadas a un perfil de cliente específico.

```json
// Response (HTTP 200 OK)
[
  { "id": "e9f8e7d6-5432-1098-7654-9876543210fe", "score": 750 }
]
```

---

### 6.5 Eliminar Score Crediticio
* **Método**: `DELETE` | **Ruta**: `/api/v1/credit-scores/{id}` | **Acceso**: Propietario o `ROLE_ADMIN`

#### Descripción
Borra un registro de informe crediticio.

```http
Response: HTTP 204 No Content
```

---

## 7. Projections - Depreciación de Vehículos

### 7.1 Calcular Proyección de Depreciación
* **Método**: `POST` | **Ruta**: `/api/v1/depreciation-projections` | **Acceso**: Autenticado

#### Descripción
Calcula la tabla de depreciación técnica y desvalorización estimada de un vehículo a lo largo de un horizonte de años especificado.

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
* **Método**: `GET` | **Ruta**: `/api/v1/depreciation-projections` | **Acceso**: Propietario del vehículo o `ROLE_ADMIN`

#### Descripción
Obtiene las proyecciones de desvalorización vehiculares guardadas de forma paginada.

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
* **Método**: `GET` | **Ruta**: `/api/v1/depreciation-projections/{id}` | **Acceso**: Propietario del vehículo o `ROLE_ADMIN`

#### Descripción
Devuelve el desglose año a año del valor proyectado de reventa de un vehículo por ID de proyección.

```json
// Response (HTTP 200 OK)
{
  "id": "11223344-5566-7788-9900-aabbccddeeff",
  "initialValue": 34900.00
}
```

---

### 7.4 Obtener Proyecciones por Vehicle ID
* **Método**: `GET` | **Ruta**: `/api/v1/depreciation-projections/vehicle/{vehicleId}` | **Acceso**: Propietario del vehículo o `ROLE_ADMIN`

#### Descripción
Consulta las proyecciones de depreciación calculadas previamente para un vehículo específico.

```json
// Response (HTTP 200 OK)
[
  { "id": "11223344-5566-7788-9900-aabbccddeeff" }
]
```

---

### 7.5 Eliminar Proyección
* **Método**: `DELETE` | **Ruta**: `/api/v1/depreciation-projections/{id}` | **Acceso**: Propietario del vehículo o `ROLE_ADMIN`

#### Descripción
Elimina una proyección de depreciación del sistema.

```http
Response: HTTP 204 No Content
```

---

## 8. Billing - Planes, Suscripciones, Facturas y Stripe

### 8.1 Listar Planes Activos
* **Método**: `GET` | **Ruta**: `/api/v1/billing/plans` | **Acceso**: Autenticado

#### Descripción
Obtiene los planes de suscripción comercial SaaS disponibles (Free, Pro Dealer, Enterprise).

```json
// Response (HTTP 200 OK)
[
  { "id": 1, "name": "Plan Pro Dealer", "price": 49.99, "currency": "USD" }
]
```

---

### 8.2 Obtener Plan por ID
* **Método**: `GET` | **Ruta**: `/api/v1/billing/plans/{planId}` | **Acceso**: Autenticado

#### Descripción
Consulta los límites y características de un plan comercial específico por su ID.

```json
// Response (HTTP 200 OK)
{ "id": 1, "name": "Plan Pro Dealer", "price": 49.99 }
```

---

### 8.3 Crear Nuevo Plan
* **Método**: `POST` | **Ruta**: `/api/v1/billing/plans` | **Acceso**: `ROLE_ADMIN`

#### Descripción
Registra un nuevo plan comercial asignando límites de publicaciones y simulaciones por mes. Exclusivo para administradores.

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

#### Descripción
Devuelve el estado de la suscripción activa del usuario actualmente autenticado.

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

#### Descripción
Suscribe directamente al usuario a un plan en el módulo billing interno.

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

#### Descripción
Cancela la suscripción activa del usuario para impedir la renovación automática.

```json
// Response (HTTP 200 OK)
{ "id": 5, "status": "CANCELLED" }
```

---

### 8.7 Crear Sesión de Stripe Checkout
* **Método**: `POST` | **Ruta**: `/api/v1/billing/subscriptions/checkout-session` | **Acceso**: Autenticado

#### Descripción
Genera una URL cifrada de Stripe Checkout pasarela de pagos web segura para abonar la suscripción mediante tarjeta de crédito o débito.

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

#### Descripción
Lista el historial de facturas y comprobantes emitidos a nombre del usuario autenticado.

```json
// Response (HTTP 200 OK)
[
  { "id": 10, "amount": 49.99, "status": "PAID" }
]
```

---

### 8.9 Pagar / Reconciliar Factura
* **Método**: `PATCH` | **Ruta**: `/api/v1/billing/invoices/{invoiceId}` | **Acceso**: Autenticado

#### Descripción
Actualiza el estado de una factura pendiente marcándola como pagada (`PAID`) en el sistema de facturación.

```json
// Response (HTTP 200 OK)
{ "id": 10, "status": "PAID" }
```

---

### 8.10 Webhook Receptor de Eventos Stripe
* **Método**: `POST` | **Ruta**: `/api/v1/billing/webhooks/stripe` | **Acceso**: Público (Verificado por Header `Stripe-Signature`)

#### Descripción
Receptor asíncrono en tiempo real de notificaciones de eventos emitidos por Stripe (`checkout.session.completed`, `customer.subscription.deleted`, `invoice.payment_failed`).

```json
// Response (HTTP 200 OK)
"Event received"
```
