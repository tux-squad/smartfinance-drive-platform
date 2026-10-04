# Guía Completa de la API REST por Roles - SmartFinance Drive Platform

Documentación técnica y exhaustiva de los **109 endpoints** del backend de **SmartFinance Drive Platform**, estructurada por **Niveles de Acceso y Roles de Usuario** para facilitar la integración directa en aplicaciones Frontend (React, Angular, Vue, Next.js, etc.).

> **Fuente de verdad**: este documento fue generado verificando controlador por controlador en `src/main/java/com/smartfinance/smartfinancedriveplatform/**/interfaces/rest/`. Todas las rutas, métodos HTTP y anotaciones `@PreAuthorize` coinciden 1:1 con el código.

---

## 📌 Información General & Servidor

* **Servidor en Producción (Render)**: `https://smartfinance-drive-platform.onrender.com`
* **Swagger UI (OpenAPI 3.0)**: [https://smartfinance-drive-platform.onrender.com/swagger-ui/index.html](https://smartfinance-drive-platform.onrender.com/swagger-ui/index.html)
* **OpenAPI Specs (JSON)**: `https://smartfinance-drive-platform.onrender.com/v3/api-docs`
* **Prefijo base de toda la API**: `/api/v1`
* **Formato de respuesta**: `application/json` (excepto el PDF de facturas y el webhook de Stripe)

---

## 🔒 Cabeceras de Autenticación y Matriz de Permisos

Para cualquier endpoint que requiera autenticación, el Frontend debe incluir el token JWT en la cabecera HTTP:

```http
Authorization: Bearer <access_token_jwt>
Content-Type: application/json
```

### Reglas de seguridad globales (definidas en `WebSecurityConfig`)

| Regla | Detalle |
| :--- | :--- |
| **Rutas 100 % públicas (`permitAll`)** | `/api/v1/auth/**` y **solo** `GET /api/v1/vehicles` + `GET /api/v1/vehicles/**` |
| **Swagger / OpenAPI** | `permitAll` en perfiles de desarrollo; `hasRole('ADMIN')` en producción |
| **Todo lo demás** | `anyRequest().authenticated()` → exige JWT válido, **aunque el método no tenga `@PreAuthorize`** |
| **Sesión** | `STATELESS` (sin cookies de sesión), `CSRF` deshabilitado |
| **Autorización de método** | `@EnableMethodSecurity` activo: los `@PreAuthorize` se aplican aunque la ruta sea `permitAll` |

> ⚠️ **Nota importante**: un endpoint **sin** `@PreAuthorize` significa *"cualquier usuario autenticado"*, no *"público"*. Por ejemplo, `GET /api/v1/profiles/reniec/dni/{dni}` **sí requiere JWT**.

### Tabla Resumen de Roles de Usuario

Los roles válidos son los del enum `Roles` (`iam/domain/model/valueobjects/Roles.java`):

| Rol en Backend | Descripción | Ámbito de Acción en Frontend |
| :--- | :--- | :--- |
| **Público** | Visitante no autenticado | Registro, Login, Recuperar contraseña, Catálogo de vehículos |
| **`ROLE_USER`** | Cliente Final / Comprador | Perfiles, Simulaciones, Solicitudes de crédito, Score, Depreciación, Chat IA, Test Drives |
| **`ROLE_DEALER`** | Concesionario de Vehículos | Inventario, Asesores de Ventas, CRM (prospectos), Perfil del concesionario, Métricas, Suscripción Stripe |
| **`ROLE_FINANCIAL_INSTITUTION`** | Banco / Entidad Financiera | Evaluar solicitudes de crédito, tasas de referencia, métricas B2B |
| **`ROLE_FINANCIAL_ANALYST`** | Analista Financiero | Lectura de entidades financieras y sus benchmarks de tasas |
| **`ROLE_ADMIN`** | Administrador del Sistema | Control total de usuarios, roles, planos de billing y métricas globales |

> ⚠️ **Corrección respecto a versiones anteriores de este documento**: **no existe `ROLE_SALES_AGENT`**. Los *Asesores de Ventas* son registros internos del concesionario (recurso `SalesAgent`), **no un rol de seguridad**. Un asesor opera siempre dentro de la cuenta de su concesionario (`ROLE_DEALER`).

### 📐 Formato de Respuestas Paginadas (verificado)

Los endpoints que devuelven colecciones paginadas usan `Page<T>` de Spring Data serializado **directo** (el proyecto no habilita `pageSerializationMode = VIA_DTO`). El cuerpo de respuesta es siempre:

```json
{
  "content": [ { "...": "elementos de la página actual" } ],
  "empty": false,
  "first": true,
  "last": false,
  "number": 0,
  "numberOfElements": 10,
  "pageable": {
    "offset": 0,
    "pageNumber": 0,
    "pageSize": 10,
    "paged": true,
    "sort": { "empty": false, "sorted": true, "unsorted": false },
    "unpaged": false
  },
  "size": 10,
  "sort": { "empty": false, "sorted": true, "unsorted": false },
  "totalElements": 45,
  "totalPages": 5
}
```

| Campo | Significado |
| :--- | :--- |
| `content` | Array de elementos de la página actual |
| `totalElements` / `totalPages` | Totales de la colección y de páginas |
| `number` / `size` | Página actual (0-based) y tamaño |
| `numberOfElements` | Elementos reales en esta página |
| `first` / `last` / `empty` | Flags de navegación |
| `pageable` / `sort` | Metadatos de la petición (informativos; el Frontend puede ignorarlos) |

> 🔬 *Formato comprobado serializando un `PageImpl` real con el classpath del proyecto (Spring Data Commons 4.1.1).*

---

## 🌐 1. Endpoints Públicos (Sin Autenticación)

Estos endpoints no requieren token JWT en la cabecera `Authorization`.

---

### 🔐 1.1 Registrar Nuevo Usuario
* **Método**: `POST` | **Ruta**: `/api/v1/auth/registrations` | **Acceso**: `Público`
* **💡 Descripción**: Crea una cuenta de usuario. El campo `roles` permite indicar los roles iniciales.
* **💻 Uso en Frontend**: Formulario de Sign Up. Al recibir HTTP 201, redirigir al Login.
* **📥 Request Body**:
```json
{
  "username": "juan.perez@example.com",
  "password": "Password123!",
  "roles": ["ROLE_USER"]
}
```
* **Validaciones**: `username` → obligatorio, formato email, máx. 100 caracteres. `password` → obligatorio, 8–100 caracteres.
* **📤 Response (HTTP 201 Created)**:
```json
{
  "id": 101,
  "username": "juan.perez@example.com",
  "roles": ["ROLE_USER"]
}
```

---

### 🔐 1.2 Iniciar Sesión (Obtener JWT)
* **Método**: `POST` | **Ruta**: `/api/v1/auth/sessions` | **Acceso**: `Público`
* **💡 Descripción**: Valida credenciales y devuelve `token` (access JWT) y `refreshToken`.
* **💻 Uso en Frontend**: Formulario de Login. Guardar tokens en memoria/secure storage. Redirigir según `roles` retornado.
* **📥 Request Body**:
```json
{
  "username": "juan.perez@example.com",
  "password": "Password123!"
}
```
* **📤 Response (HTTP 200 OK)**:
```json
{
  "id": 101,
  "username": "juan.perez@example.com",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "d7b8c9a0-1234-5678-9abc-def012345678",
  "roles": ["ROLE_USER"]
}
```

---

### 🔐 1.3 Renovar Token de Acceso (Refresh Token)
* **Método**: `POST` | **Ruta**: `/api/v1/auth/tokens` | **Acceso**: `Público`
* **💡 Descripción**: Renueva un access token expirado usando el refresh token.
* **💻 Uso en Frontend**: Axios Interceptor que se dispara en `401 Unauthorized`.
* **📥 Request Body**:
```json
{
  "refreshToken": "d7b8c9a0-1234-5678-9abc-def012345678"
}
```
* **📤 Response (HTTP 200 OK)**: mismo objeto `AuthenticatedUserResource` del login (con `token` y `refreshToken` nuevos).

---

### 🔐 1.4 Cerrar Sesión (Logout / Revocar Token)
* **Método**: `DELETE` | **Ruta**: `/api/v1/auth/sessions/current` | **Acceso**: `Público` *(envía el header `Authorization` si lo tiene)*
* **💡 Descripción**: Revoca el JWT actual. El token deja de ser válido.
* **💻 Uso en Frontend**: Botón "Cerrar Sesión". Limpiar storage local y redirigir al Login.
* **📤 Response (HTTP 200 OK)**:
```json
{ "message": "User signed out successfully" }
```

---

### 🔐 1.5 Recuperar Contraseña (Solicitar Reset)
* **Método**: `POST` | **Ruta**: `/api/v1/auth/password-recoveries` | **Acceso**: `Público`
* **💡 Descripción**: Inicia el flujo de recuperación. **Siempre responde 200** aunque el correo no exista (evita *user enumeration*).
* **💻 Uso en Frontend**: Pantalla "Olvidé mi contraseña". Mostrar mensaje genérico: *"Si el correo existe, recibirás instrucciones"*.
* **📥 Request Body**:
```json
{ "username": "juan.perez@example.com" }
```
* **📤 Response (HTTP 200 OK)**:
```json
{ "message": "If an account with that email exists, password reset instructions have been processed." }
```

---

### 🔐 1.6 Restablecer Contraseña (Confirmar Reset)
* **Método**: `POST` | **Ruta**: `/api/v1/auth/password-resets` | **Acceso**: `Público`
* **💡 Descripción**: Aplica la nueva contraseña usando el token recibido por correo.
* **💻 Uso en Frontend**: Formulario accesible desde el link del correo (token por query param).
* **📥 Request Body**:
```json
{
  "resetToken": "rst_8f9a0b1c2d3e4f5a6b7c8d9e",
  "newPassword": "NuevaClave123!"
}
```
* **Validaciones**: `newPassword` → 8–100 caracteres.
* **📤 Response (HTTP 200 OK)**:
```json
{ "message": "Password reset successfully" }
```

---

### 🔐 1.7 Autenticación con Google OAuth2
* **Método**: `POST` | **Ruta**: `/api/v1/auth/google` | **Acceso**: `Público`
* **💡 Descripción**: Login/registro con Google Identity Services recibiendo el `idToken`.
* **💻 Uso en Frontend**: Botón "Sign in with Google"; enviar el `credential` del popup a esta ruta.
* **📥 Request Body**:
```json
{ "idToken": "eyJhbGciOiJSUzI1NiIsImtpZCI6..." }
```
* **📤 Response (HTTP 200 OK)**: mismo objeto `AuthenticatedUserResource` del login.

---

### 📧 1.8 Enviar Código OTP de Verificación de Correo
* **Método**: `POST` | **Ruta**: `/api/v1/auth/email-verification/send` | **Acceso**: `Público`
* **💡 Descripción**: Genera un código OTP de 6 dígitos criptográficamente seguro y lo envía por Gmail SMTP. **Rate limit: 10 peticiones/minuto por IP** (devuelve `429`).
* **💻 Uso en Frontend**: Botón "Verificar Correo" con cooldown de 60 s tras el clic.
* **📥 Request Body**:
```json
{ "email": "juan.perez@example.com" }
```
* **📤 Response (HTTP 200 OK)**:
```json
{
  "email": "juan.perez@example.com",
  "maskedEmail": "j***z@example.com",
  "sessionActive": true,
  "expiresInSeconds": 600,
  "message": "Verification code sent"
}
```

---

### 📧 1.9 Confirmar Código OTP de Correo
* **Método**: `POST` | **Ruta**: `/api/v1/auth/email-verification/verify` | **Acceso**: `Público`
* **💡 Descripción**: Valida el código de 6 dígitos (en tiempo constante) y emite un `verificationToken`.
* **💻 Uso en Frontend**: Input de 6 dígitos; al recibir 200 marcar el email como verificado.
* **📥 Request Body**:
```json
{
  "email": "juan.perez@example.com",
  "code": "849201"
}
```
* **Validaciones**: `code` → regex exacta `^\d{6}$`.
* **📤 Response (HTTP 200 OK)**:
```json
{
  "verified": true,
  "email": "juan.perez@example.com",
  "status": "VERIFIED",
  "verifiedAt": "2026-10-04T15:30:00Z",
  "verificationToken": "evt_8f9a0b1c2d3e4f5a6b7c8d9e",
  "message": "Email verified successfully"
}
```

---

### 📱 1.10 Verificación Telefónica con Firebase
* **Método**: `POST` | **Rutas**: `/api/v1/auth/phone-verification` **y** `/api/v1/auth/phone-verification/firebase` | **Acceso**: `Público`
* **💡 Descripción**: Valida un **Firebase Phone Authentication ID Token** emitido por el SDK de Firebase en el cliente y emite un `verificationToken` de la plataforma. Devuelve `503` si Firebase no está configurado.
* **💻 Uso en Frontend**: Flujo de OTP con Firebase (reCAPTCHA + SMS); una vez verificado por Firebase, enviar el `idToken` resultante a esta ruta.
* **📥 Request Body**:
```json
{ "firebaseIdToken": "eyJhbGciOiJSUzI1NiIsImtpZCI6IiIsInR5cCI6IkpXVCJ9..." }
```
* **📤 Response (HTTP 200 OK)**:
```json
{
  "verified": true,
  "phoneNumber": "+51987654321",
  "status": "VERIFIED",
  "verifiedAt": "2026-10-04T15:30:00Z",
  "verificationToken": "pvt_8f9a0b1c2d3e4f5a6b7c8d9e",
  "message": "Phone number verified successfully"
}
```

---

### 🚗 1.11 Catálogo Público de Vehículos (Paginado y Filtrado)
* **Método**: `GET` | **Ruta**: `/api/v1/vehicles` | **Acceso**: `Público`
* **💡 Descripción**: Lista el inventario de vehículos con filtros y paginación de Spring Data.
* **💻 Uso en Frontend**: Landing Page / Buscador del Catálogo.
* **📥 Query Params**:

| Param | Tipo | Obligatorio | Descripción |
| :--- | :--- | :--- | :--- |
| `brand` | String | No | Marca |
| `model` | String | No | Modelo |
| `minPrice` | BigDecimal | No | Precio mínimo |
| `maxPrice` | BigDecimal | No | Precio máximo |
| `minYear` | Integer | No | Año mínimo |
| `maxYear` | Integer | No | Año máximo |
| `condition` | String | No | `NEW` o `USED` (en mayúsculas; otro valor ⇒ `400`) |
| `page` | Integer | No | Índice de página (default `0`) |
| `size` | Integer | No | Tamaño (default `10`, máx. `50`) |
| `sort` | String | No | Default `createdAt,asc` |

* **📤 Response (HTTP 200 OK)** — paginación estándar de Spring (`Page<VehicleResource>`, ver *Formato de Respuestas Paginadas*):
```json
{
  "content": [
    {
      "id": "3f1a2b4c-6d8e-4f0a-9b1c-2d3e4f5a6b7c",
      "userId": "101",
      "financialEntityId": "7a1b2c3d-4e5f-6a7b-8c9d-0e1f2a3b4c5d",
      "brand": "Toyota",
      "model": "Corolla Cross",
      "manufactureYear": 2025,
      "condition": "NEW",
      "priceAmount": 26990.00,
      "currency": "USD",
      "imagePath": "https://res.cloudinary.com/demo/image/upload/v1/vehicles/corolla.jpg",
      "status": "ACTIVE",
      "mileage": 0,
      "transmission": "AUTOMATIC",
      "engine": "2.0L",
      "traction": "FWD",
      "images": ["https://res.cloudinary.com/.../1.jpg"],
      "createdAt": "2026-09-20T10:00:00Z"
    }
  ],
  "empty": false,
  "first": true,
  "last": false,
  "number": 0,
  "numberOfElements": 1,
  "pageable": {
    "offset": 0,
    "pageNumber": 0,
    "pageSize": 10,
    "paged": true,
    "sort": { "empty": false, "sorted": true, "unsorted": false },
    "unpaged": false
  },
  "size": 10,
  "sort": { "empty": false, "sorted": true, "unsorted": false },
  "totalElements": 45,
  "totalPages": 5
}
```
* **📤 Response (HTTP 400)**: si `condition` no es `NEW` ni `USED`.

---

### 🚗 1.12 Listar Marcas Disponibles
* **Método**: `GET` | **Ruta**: `/api/v1/vehicles/brands` | **Acceso**: `Público`
* **💡 Descripción**: Devuelve la lista de marcas distintas registradas en el catálogo.
* **💻 Uso en Frontend**: Selector de marca del buscador (`<select>` o chips de filtro).
* **📤 Response (HTTP 200 OK)**:
```json
["Toyota", "Kia", "Suzuki", "Chery"]
```

---

### 🚗 1.13 Obtener Detalle de Vehículo por ID
* **Método**: `GET` | **Ruta**: `/api/v1/vehicles/{vehicleId}` | **Acceso**: `Público`
* **💡 Descripción**: Ficha técnica completa de un vehículo (UUID).
* **💻 Uso en Frontend**: Vista `/catalog/vehicles/:id`.
* **📥 Path Param**: `vehicleId` → `UUID`
* **📤 Response (HTTP 200 OK)**:
```json
{
  "id": "3f1a2b4c-6d8e-4f0a-9b1c-2d3e4f5a6b7c",
  "userId": "101",
  "financialEntityId": "7a1b2c3d-4e5f-6a7b-8c9d-0e1f2a3b4c5d",
  "brand": "Toyota",
  "model": "Corolla Cross",
  "manufactureYear": 2025,
  "condition": "NEW",
  "priceAmount": 26990.00,
  "currency": "USD",
  "imagePath": "https://res.cloudinary.com/demo/image/upload/v1/vehicles/corolla.jpg",
  "status": "ACTIVE",
  "mileage": 1200,
  "transmission": "AUTOMATIC",
  "engine": "2.0L",
  "traction": "FWD",
  "images": [
    "https://res.cloudinary.com/.../1.jpg",
    "https://res.cloudinary.com/.../2.jpg"
  ],
  "createdAt": "2026-09-20T10:00:00Z"
}
```
* **📤 Response (HTTP 404 Not Found)**: si el `vehicleId` no existe.

---

---

## 👤 2. Endpoints del Cliente Final (`ROLE_USER`)

Requieren autenticación con `Authorization: Bearer <token>`. Salvo indicación, cualquier rol autenticado puede consumirlos; el control de propiedad (`ownershipChecker`) impide acceder a datos de otros usuarios.

---

### 👤 Perfiles de Cliente

#### 2.1 Crear Mi Perfil
* **Método**: `POST` | **Ruta**: `/api/v1/profiles` | **Acceso**: Autenticado
* **💡 Descripción**: Registra el perfil personal usando el `userId` del token autenticado.
* **💻 Uso en Frontend**: Wizard "Completa tu perfil" tras el primer login.
* **📥 Request Body**:
```json
{
  "userId": "101",
  "email": "juan.perez@example.com",
  "nationalId": "72849102",
  "fullLegalNames": "Juan Carlos Perez Gomez",
  "dateOfBirth": "1995-04-12",
  "phoneCountryCode": "+51",
  "mobilePhone": "987654321",
  "monthlyIncomeAmount": 4500.00,
  "monthlyIncomeCurrency": "PEN",
  "employmentStatus": "EMPLOYED"
}
```
* **Validaciones**: `nationalId` → `^[0-9A-Za-z]{8,20}$`; `dateOfBirth` → en el pasado; `monthlyIncomeAmount` ≥ 0; `monthlyIncomeCurrency` → exactamente 3 letras; `fullLegalNames` máx. 150.
* **📤 Response (HTTP 201 Created)**: objeto `ProfileResource` (ver 2.2).

#### 2.2 Obtener Perfil por ID
* **Método**: `GET` | **Ruta**: `/api/v1/profiles/{profileId}` | **Acceso**: Propietario o `ROLE_ADMIN`
* **📥 Path Param**: `profileId` → `UUID`
* **📤 Response (HTTP 200 OK)**:
```json
{
  "id": "a1b2c3d4-e5f6-4a7b-8c9d-0e1f2a3b4c5d",
  "userId": "101",
  "email": "juan.perez@example.com",
  "nationalId": "72849102",
  "fullLegalNames": "Juan Carlos Perez Gomez",
  "dateOfBirth": "1995-04-12",
  "phoneCountryCode": "+51",
  "mobilePhone": "987654321",
  "monthlyIncomeAmount": 4500.00,
  "monthlyIncomeCurrency": "PEN",
  "employmentStatus": "EMPLOYED"
}
```

#### 2.3 Obtener Mi Perfil por UserId
* **Método**: `GET` | **Ruta**: `/api/v1/profiles/users/{userId}` | **Acceso**: El propio usuario o `ROLE_ADMIN`
* **💡 Descripción**: **Este es el equivalente real al antiguo `GET /profiles/me`** (el backend no expone ruta `/me` para perfiles).
* **💻 Uso en Frontend**: Pantalla "Mi Perfil": primero obtener el `userId` del claim del JWT y luego llamar a esta ruta.
* **📥 Path Param**: `userId` → `String`
* **📤 Response (HTTP 200 OK)**:
```json
{
  "id": "a1b2c3d4-e5f6-4a7b-8c9d-0e1f2a3b4c5d",
  "userId": "101",
  "email": "juan.perez@example.com",
  "nationalId": "72849102",
  "fullLegalNames": "Juan Carlos Perez Gomez",
  "dateOfBirth": "1995-04-12",
  "phoneCountryCode": "+51",
  "mobilePhone": "987654321",
  "monthlyIncomeAmount": 4500.00,
  "monthlyIncomeCurrency": "PEN",
  "employmentStatus": "EMPLOYED"
}
```
* **📤 Response (HTTP 404 Not Found)**: si aún no existe perfil para ese usuario.

#### 2.4 Actualizar Mi Perfil
* **Método**: `PUT` | **Ruta**: `/api/v1/profiles/{profileId}` | **Acceso**: Propietario del perfil
* **📥 Request Body**: mismos campos de 2.1 **excepto `userId`** (9 campos, mismas validaciones).
* **📤 Response (HTTP 200 OK)**: objeto `ProfileResource` actualizado.

#### 2.5 Eliminar Mi Perfil
* **Método**: `DELETE` | **Ruta**: `/api/v1/profiles/{profileId}` | **Acceso**: Propietario del perfil
* **📤 Response (HTTP 204 No Content)** — sin body.

#### 2.6 Consulta RENIEC por DNI
* **Método**: `GET` | **Ruta**: `/api/v1/profiles/reniec/dni/{dni}` | **Acceso**: Autenticado ⚠️ *(no es público)*
* **💡 Descripción**: Valida un DNI peruano (8 dígitos) consultando RENIEC vía Factiliza. **Rate limit: 10 peticiones/minuto** (`429`).
* **💻 Uso en Frontend**: Al escribir 8 dígitos en un formulario, autocompletar nombres y apellidos.
* **📥 Path Param**: `dni` → `String`, regex `^\d{8}$` (si no coincide → `400`).
* **📤 Response (HTTP 200 OK)**:
```json
{
  "dni": "72849102",
  "verificationDigit": "7",
  "firstNames": "Juan Carlos",
  "paternalSurname": "Perez",
  "maternalSurname": "Gomez",
  "fullLegalName": "Juan Carlos Perez Gomez",
  "department": "LIMA",
  "province": "LIMA",
  "district": "ANCON",
  "address": "AV. GRAN CHIMU 123",
  "fullAddress": "AV. GRAN CHIMU 123, ANCON, LIMA, LIMA",
  "ubigeoReniec": "150102",
  "ubigeoSunat": "150102",
  "ubigeo": ["15", "1501", "150102"],
  "birthDate": "1995-04-12",
  "gender": "M"
}
```

---

### 🏢 Verificación de Identidad y Elevación de Rol (B2B)

#### 2.7 Obtener Usuario por ID
* **Método**: `GET` | **Ruta**: `/api/v1/users/{userId}` | **Acceso**: El propio usuario o `ROLE_ADMIN`
* **📤 Response (HTTP 200 OK)**: `UserResource` → `{ "id": 101, "username": "juan@...", "roles": ["ROLE_USER"] }`

#### 2.8 Solicitar Vinculación RUC → Concesionario
* **Método**: `POST` | **Ruta**: `/api/v1/users/{userId}/dealer-role-requests` | **Acceso**: El propio usuario o `ROLE_ADMIN`
* **💡 Descripción**: Solicita la elevación a `ROLE_DEALER` validando un RUC 20 (11 dígitos).
* **📥 Request Body**:
```json
{
  "ruc": "20601234567",
  "companyName": "AUTOLAND PERU S.A.C."
}
```
* **Validaciones**: `ruc` → `^\d{11}$`.
* **📤 Response (HTTP 200 OK)**: `UserResource` actualizado.

#### 2.9 Solicitar Vinculación RUC → Entidad Financiera
* **Método**: `POST` | **Ruta**: `/api/v1/users/{userId}/financial-institution-role-requests` | **Acceso**: El propio usuario o `ROLE_ADMIN`
* **📥 Request Body**: idéntico a 2.8 (`ruc` + `companyName`).
* **📤 Response (HTTP 200 OK)**: `UserResource` actualizado.

#### 2.10 Consulta SUNAT por RUC
* **Método**: `GET` | **Ruta**: `/api/v1/partners/sunat/ruc/{ruc}` | **Acceso**: Autenticado ⚠️ *(no es público)*
* **💡 Descripción**: Valida un RUC 11 dígitos contra SUNAT (razón social, estado, condición, CIIU).
* **💻 Uso en Frontend**: Formulario B2B; autocompletar Razón Social y Dirección Fiscal.
* **📥 Path Param**: `ruc` → regex `^\d{11}$`.
* **📤 Response (HTTP 200 OK)**:
```json
{
  "ruc": "20601234567",
  "razonSocial": "AUTOLAND PERU S.A.C.",
  "estado": "ACTIVO",
  "condicion": "HABIDO",
  "tipo": "SOCIEDAD ANONIMA CERRADA",
  "ubigeo": "150115",
  "direccion": "AV. JAVIER PRADO ESTE 410, SAN ISIDRO",
  "ciio": "522100",
  "isActiveAndHabido": true,
  "isAutomotiveCiiu": true
}
```

#### 2.11 Lookup de Verificación Corporativa por RUC
* **Método**: `GET` | **Ruta**: `/api/v1/partners/corporate-verification/lookup/{ruc}` | **Acceso**: Autenticado
* **💡 Descripción**: Consulta SUNAT para pre-llenar el perfil corporativo y devolver los **dominios de correo autorizados** para la verificación B2B de bancos y concesionarios.
* **💻 Uso en Frontend**: Paso previo de "Verificación B2B": muestra el rol sugerido y valida si el correo corporativo del usuario pertenece a un dominio permitido.
* **📥 Path Param**: `ruc` → regex `^\d{11}$`.
* **📤 Response (HTTP 200 OK)**:
```json
{
  "ruc": "20601234567",
  "entityType": "DEALERSHIP",
  "targetRole": "ROLE_DEALER",
  "suggestedName": "AUTOLAND PERU S.A.C.",
  "fiscalAddress": "AV. JAVIER PRADO ESTE 410, SAN ISIDRO",
  "ubigeo": "150115",
  "allowedEmailDomains": ["autoland.pe"],
  "logoUrl": "https://...",
  "eligibleForVerification": true
}
```

#### 2.12 Iniciar Verificación Corporativa (Yo)
* **Método**: `POST` | **Ruta**: `/api/v1/users/me/corporate-verification/initiate` | **Acceso**: Autenticado
* **💡 Descripción**: Envía un código OTP al correo corporativo del dominio autorizado (validado contra `allowedEmailDomains` del 2.11).
* **📥 Request Body**:
```json
{
  "ruc": "20601234567",
  "corporateEmail": "juan@autoland.pe"
}
```
* **Validaciones**: `ruc` → `^\d{11}$`; `corporateEmail` → formato email.
* **📤 Response (HTTP 200 OK)**:
```json
{
  "sessionId": "cvs_8f9a0b1c2d3e4f5a6b7c8d9e",
  "sessionActive": true,
  "maskedEmail": "j**n@autoland.pe",
  "expiresInSeconds": 600,
  "message": "Verification code sent to corporate email"
}
```

#### 2.13 Confirmar Verificación Corporativa (Yo)
* **Método**: `POST` | **Ruta**: `/api/v1/users/me/corporate-verification/confirm` | **Acceso**: Autenticado
* **💡 Descripción**: Valida el código de 6 dígitos y **asigna automáticamente el rol** (`ROLE_DEALER` o `ROLE_FINANCIAL_INSTITUTION`) según el tipo de entidad del RUC.
* **📥 Request Body**:
```json
{
  "ruc": "20601234567",
  "code": "849201"
}
```
* **Validaciones**: `code` → `^\d{6}$`.
* **📤 Response (HTTP 200 OK)**:
```json
{
  "verified": true,
  "entityType": "DEALERSHIP",
  "assignedRole": "ROLE_DEALER",
  "profileId": "a1b2c3d4-e5f6-4a7b-8c9d-0e1f2a3b4c5d",
  "profileName": "AUTOLAND PERU S.A.C.",
  "message": "Corporate verification completed"
}
```

#### 2.14 / 2.15 Verificación Corporativa por `{userId}` (Variante con ID)
* **Método**: `POST` | **Rutas**: `/api/v1/users/{userId}/corporate-verification/initiate` y `/api/v1/users/{userId}/corporate-verification/confirm` | **Acceso**: El propio usuario o `ROLE_ADMIN`
* **💡 Descripción**: Mismos flujos que 2.12/2.13 pero apuntando a un usuario específico. **Uso reservado para que un ADMIN complete la verificación de otra cuenta.**
* **📥 Request Body**: idénticos a 2.12 y 2.13 respectivamente.
* **📤 Response**: idénticos a 2.12 y 2.13 respectivamente.

---

### 🚗 Mis Publicaciones de Vehículos

#### 2.16 Listar Mis Vehículos Publicados
* **Método**: `GET` | **Ruta**: `/api/v1/vehicles/my-listings` | **Acceso**: Autenticado
* **💡 Descripción**: Devuelve todos los vehículos cuyo `userId` es el del usuario autenticado.
* **📤 Response (HTTP 200 OK)** — lista (no paginada):
```json
[
  {
    "id": "3f1a2b4c-6d8e-4f0a-9b1c-2d3e4f5a6b7c",
    "userId": "101",
    "financialEntityId": "7a1b2c3d-4e5f-6a7b-8c9d-0e1f2a3b4c5d",
    "brand": "Toyota",
    "model": "Corolla Cross",
    "manufactureYear": 2025,
    "condition": "NEW",
    "priceAmount": 26990.00,
    "currency": "USD",
    "imagePath": "https://res.cloudinary.com/.../corolla.jpg",
    "status": "ACTIVE",
    "mileage": 0,
    "transmission": "AUTOMATIC",
    "engine": "2.0L",
    "traction": "FWD",
    "images": ["https://res.cloudinary.com/.../1.jpg"],
    "createdAt": "2026-09-20T10:00:00Z"
  }
]
```
* **📤 Response (HTTP 200 OK) `[]`**: si el usuario aún no ha publicado vehículos.

#### 2.17 Listar Vehículos de un Usuario
* **Método**: `GET` | **Ruta**: `/api/v1/vehicles/users/{userId}` | **Acceso**: El propio usuario o `ROLE_ADMIN`
* **📥 Path Param**: `userId` → `String`
* **📤 Response (HTTP 200 OK)** — misma estructura de lista que 2.16:
```json
[
  {
    "id": "3f1a2b4c-6d8e-4f0a-9b1c-2d3e4f5a6b7c",
    "userId": "101",
    "financialEntityId": "7a1b2c3d-4e5f-6a7b-8c9d-0e1f2a3b4c5d",
    "brand": "Toyota",
    "model": "Corolla Cross",
    "manufactureYear": 2025,
    "condition": "NEW",
    "priceAmount": 26990.00,
    "currency": "USD",
    "imagePath": "https://res.cloudinary.com/.../corolla.jpg",
    "status": "SOLD",
    "mileage": 1500,
    "transmission": "AUTOMATIC",
    "engine": "2.0L",
    "traction": "FWD",
    "images": [],
    "createdAt": "2026-08-15T09:30:00Z"
  }
]
```
* **📤 Response (HTTP 403 Forbidden)**: si `userId` no corresponde al usuario autenticado y no tiene rol `ADMIN`.

---

### 🏦 Catálogo de Entidades Financieras (Lectura)

#### 2.18 Listar Entidades Financieras
* **Método**: `GET` | **Ruta**: `/api/v1/financial-entities` | **Acceso**: `ROLE_USER`, `ROLE_ADMIN`, `ROLE_FINANCIAL_ANALYST`, `ROLE_FINANCIAL_INSTITUTION`, `ROLE_DEALER`
* **💡 Descripción**: Directorio de bancos/entidades con sus benchmarks de tasas. El `userId` solo se incluye si el caller es admin o dueño.
* **💻 Uso en Frontend**: Selector "Entidad financiera" al crear una solicitud de crédito.
* **📤 Response (HTTP 200 OK)**: lista de `FinancialEntityResource`:
```json
[
  {
    "id": "7a1b2c3d-4e5f-6a7b-8c9d-0e1f2a3b4c5d",
    "userId": null,
    "ruc": "20100047218",
    "name": "Banco de Crédito BCP",
    "logoUrl": "https://...",
    "bannerUrl": "https://...",
    "rateBenchmarks": [
      {
        "id": "b1c2d3e4-...",
        "rateType": "EFFECTIVE_ANNUAL",
        "annualRate": 11.50,
        "currency": "PEN",
        "sourceLabel": "BCP Tasas",
        "sourceUrl": "https://www.bcp.com.pe/tasas",
        "effectiveFrom": "2026-10-01"
      }
    ]
  }
]
```

#### 2.19 Obtener Entidad Financiera por ID
* **Método**: `GET` | **Ruta**: `/api/v1/financial-entities/{id}` | **Acceso**: Mismos roles que 2.18
* **📥 Path Param**: `id` → `UUID`
* **📤 Response (HTTP 200 OK)**:
```json
{
  "id": "7a1b2c3d-4e5f-6a7b-8c9d-0e1f2a3b4c5d",
  "userId": null,
  "ruc": "20100047218",
  "name": "Banco de Crédito BCP",
  "logoUrl": "https://res.cloudinary.com/.../bcp.png",
  "bannerUrl": "https://res.cloudinary.com/.../bcp-banner.png",
  "rateBenchmarks": [
    {
      "id": "b1c2d3e4-5f6a-4b7c-8d9e-0f1a2b3c4d5e",
      "rateType": "EFFECTIVE_ANNUAL",
      "annualRate": 11.50,
      "currency": "PEN",
      "sourceLabel": "BCP Tasas",
      "sourceUrl": "https://www.bcp.com.pe/tasas",
      "effectiveFrom": "2026-10-01"
    }
  ]
}
```
* **📤 Response (HTTP 404 Not Found)**: si la entidad no existe.

---

### 💰 Simulaciones de Crédito

#### 2.20 Crear Simulación Financiera
* **Método**: `POST` | **Ruta**: `/api/v1/simulations` | **Acceso**: Autenticado
* **💡 Descripción**: Calcula el cronograma de pagos completo (TCEA, TIR, VAN, intereses) de un crédito vehicular y lo asocia al usuario autenticado.
* **💻 Uso en Frontend**: Calculadora de crédito; la simulación quedará guardada para "Solicitar financiamiento" (2.23).
* **📥 Request Body**:
```json
{
  "title": "Mi auto ideal",
  "userId": "101",
  "vehicleId": "3f1a2b4c-6d8e-4f0a-9b1c-2d3e4f5a6b7c",
  "financialEntityId": "7a1b2c3d-4e5f-6a7b-8c9d-0e1f2a3b4c5d",
  "vehiclePriceAmount": 25000.00,
  "currency": "USD",
  "downPaymentPercentage": 20.00,
  "balloonPaymentPercentage": 0.00,
  "annualEffectiveRate": 12.50,
  "monthlyCreditLifeInsuranceRate": 0.25,
  "vehicleInsuranceFeeAmount": 350.00,
  "vehicleInsuranceType": "ALL_RISK",
  "loanTermMonths": 36,
  "gracePeriodType": "NONE",
  "gracePeriodMonths": 0,
  "initialFeesAmount": 500.00,
  "discountRate": 0.00,
  "startDate": "2026-11-01"
}
```
* **Validaciones**: `title` máx. 100; `vehiclePriceAmount` ≥ 0.01; `currency` 3 letras; `downPaymentPercentage`/`balloonPaymentPercentage`/`annualEffectiveRate` entre 0 y 100; `loanTermMonths` 1–360; `gracePeriodMonths` 0–60.
* **📤 Response (HTTP 201 Created)**:
```json
{
  "id": "c1d2e3f4-...",
  "title": "Mi auto ideal",
  "userId": "101",
  "vehicleId": "3f1a2b4c-...",
  "financialEntityId": "7a1b2c3d-...",
  "currency": "USD",
  "vehiclePriceAmount": 25000.00,
  "downPaymentPercentage": 20.00,
  "balloonPaymentPercentage": 0.00,
  "annualEffectiveRate": 12.50,
  "loanTermMonths": 36,
  "gracePeriodType": "NONE",
  "gracePeriodMonths": 0,
  "startDate": "2026-11-01",
  "metrics": {
    "financedAmount": 20000.00,
    "downPaymentAmount": 5000.00,
    "balloonPaymentAmount": 0.00,
    "tcea": 13.05,
    "tir": 12.50,
    "van": 0.00,
    "totalInterest": 4066.00,
    "totalAmount": 24066.00
  },
  "paymentSchedule": [
    {
      "id": "d1e2f3a4-...",
      "periodNumber": 1,
      "dueDate": "2026-12-01",
      "daysInPeriod": 30,
      "currency": "USD",
      "initialBalanceAmount": 20000.00,
      "interestPaymentAmount": 163.40,
      "principalAmortizationAmount": 505.10,
      "creditLifeInsuranceAmount": 5.00,
      "vehicleInsuranceAmount": 0.00,
      "totalInstallmentAmount": 673.50,
      "finalBalanceAmount": 19494.90,
      "graceType": "NONE"
    }
  ]
}
```

#### 2.21 Listar Mis Simulaciones
* **Método**: `GET` | **Ruta**: `/api/v1/simulations` | **Acceso**: Autenticado
* **💻 Uso en Frontend**: Panel "Mis simulaciones guardadas".
* **📥 Query Params**: `page`, `size` (default 10), `sort`.
* **📤 Response (HTTP 200 OK)** — `Page<SimulationResource>` (ver *Formato de Respuestas Paginadas*; sin `sort` por defecto):
```json
{
  "content": [
    {
      "id": "c1d2e3f4-5a6b-4c7d-8e9f-0a1b2c3d4e5f",
      "title": "Mi auto ideal",
      "userId": "101",
      "vehicleId": "3f1a2b4c-6d8e-4f0a-9b1c-2d3e4f5a6b7c",
      "financialEntityId": "7a1b2c3d-4e5f-6a7b-8c9d-0e1f2a3b4c5d",
      "currency": "USD",
      "vehiclePriceAmount": 25000.00,
      "downPaymentPercentage": 20.00,
      "balloonPaymentPercentage": 0.00,
      "annualEffectiveRate": 12.50,
      "loanTermMonths": 36,
      "gracePeriodType": "NONE",
      "gracePeriodMonths": 0,
      "startDate": "2026-11-01",
      "metrics": {
        "financedAmount": 20000.00,
        "downPaymentAmount": 5000.00,
        "balloonPaymentAmount": 0.00,
        "tcea": 13.05,
        "tir": 12.50,
        "van": 0.00,
        "totalInterest": 4066.00,
        "totalAmount": 24066.00
      },
      "paymentSchedule": []
    }
  ],
  "empty": false,
  "first": true,
  "last": false,
  "number": 0,
  "numberOfElements": 1,
  "pageable": {
    "offset": 0,
    "pageNumber": 0,
    "pageSize": 10,
    "paged": true,
    "sort": { "empty": true, "sorted": false, "unsorted": true },
    "unpaged": false
  },
  "size": 10,
  "sort": { "empty": true, "sorted": false, "unsorted": true },
  "totalElements": 3,
  "totalPages": 1
}
```

#### 2.22 Obtener Simulación por ID
* **Método**: `GET` | **Ruta**: `/api/v1/simulations/{id}` | **Acceso**: Propietario o `ROLE_ADMIN`
* **📥 Path Param**: `id` → `UUID`
* **📤 Response (HTTP 200 OK)** — objeto completo con `metrics` y el cronograma `paymentSchedule`:
```json
{
  "id": "c1d2e3f4-5a6b-4c7d-8e9f-0a1b2c3d4e5f",
  "title": "Mi auto ideal",
  "userId": "101",
  "vehicleId": "3f1a2b4c-6d8e-4f0a-9b1c-2d3e4f5a6b7c",
  "financialEntityId": "7a1b2c3d-4e5f-6a7b-8c9d-0e1f2a3b4c5d",
  "currency": "USD",
  "vehiclePriceAmount": 25000.00,
  "downPaymentPercentage": 20.00,
  "balloonPaymentPercentage": 0.00,
  "annualEffectiveRate": 12.50,
  "monthlyCreditLifeInsuranceRate": 0.25,
  "vehicleInsuranceFeeAmount": 350.00,
  "vehicleInsuranceType": "ALL_RISK",
  "loanTermMonths": 36,
  "gracePeriodType": "NONE",
  "gracePeriodMonths": 0,
  "initialFeesAmount": 500.00,
  "discountRate": 0.00,
  "startDate": "2026-11-01",
  "metrics": {
    "financedAmount": 20000.00,
    "downPaymentAmount": 5000.00,
    "balloonPaymentAmount": 0.00,
    "tcea": 13.05,
    "tir": 12.50,
    "van": 0.00,
    "totalInterest": 4066.00,
    "totalAmount": 24066.00
  },
  "paymentSchedule": [
    {
      "id": "d1e2f3a4-5b6c-4d7e-8f9a-0b1c2d3e4f5a",
      "periodNumber": 1,
      "dueDate": "2026-12-01",
      "daysInPeriod": 30,
      "currency": "USD",
      "initialBalanceAmount": 20000.00,
      "interestPaymentAmount": 163.40,
      "principalAmortizationAmount": 505.10,
      "creditLifeInsuranceAmount": 5.00,
      "vehicleInsuranceAmount": 0.00,
      "totalInstallmentAmount": 673.50,
      "finalBalanceAmount": 19494.90,
      "graceType": "NONE"
    },
    {
      "id": "e2f3a4b5-6c7d-4e8f-9a0b-1c2d3e4f5a6b",
      "periodNumber": 2,
      "dueDate": "2027-01-01",
      "daysInPeriod": 31,
      "currency": "USD",
      "initialBalanceAmount": 19494.90,
      "interestPaymentAmount": 159.34,
      "principalAmortizationAmount": 509.16,
      "creditLifeInsuranceAmount": 4.87,
      "vehicleInsuranceAmount": 0.00,
      "totalInstallmentAmount": 673.37,
      "finalBalanceAmount": 18985.74,
      "graceType": "NONE"
    }
  ]
}
```
* **📤 Response (HTTP 403 Forbidden)**: si la simulación no pertenece al usuario y no tiene rol `ADMIN`.
* **📤 Response (HTTP 404 Not Found)**: si el `id` no existe.

#### 2.23 Convertir Simulación en Solicitud de Crédito
* **Método**: `POST` | **Ruta**: `/api/v1/simulations/{id}/apply` | **Acceso**: Autenticado
* **💡 Descripción**: Promueve una simulación guardada a una solicitud formal de crédito (201).
* **💻 Uso en Frontend**: Botón "Solicitar este crédito" desde la vista de resultados de la simulación.
* **📥 Request Body**: ninguno.
* **⚠️ Nota de implementación**: el backend fija `monthlyIncome = 3000.00` y `employmentStatus = "EMPLOYED"` con valores hardcodeados en este flujo.
* **📤 Response (HTTP 201 Created)**: objeto `CreditApplicationResource` (ver 2.25).

#### 2.24 Eliminar Simulación
* **Método**: `DELETE` | **Ruta**: `/api/v1/simulations/{id}` | **Acceso**: Propietario *(sin cláusula ADMIN)*
* **📤 Response (HTTP 204 No Content)** — siempre, aunque no exista.

---

### 📄 Solicitudes de Crédito Vehicular

#### 2.25 Crear Solicitud de Crédito
* **Método**: `POST` | **Ruta**: `/api/v1/credit-applications` | **Acceso**: `ROLE_USER` o `ROLE_ADMIN`
* **💡 Descripción**: Genera una solicitud formal de financiamiento dirigida a una entidad bancaria.
* **💻 Uso en Frontend**: Botón "Solicitar Financiamiento" en la ficha del auto.
* **📥 Request Body**:
```json
{
  "vehicleId": "3f1a2b4c-6d8e-4f0a-9b1c-2d3e4f5a6b7c",
  "financialEntityId": "7a1b2c3d-4e5f-6a7b-8c9d-0e1f2a3b4c5d",
  "simulationId": "c1d2e3f4-5a6b-4c7d-8e9f-0a1b2c3d4e5f",
  "requestedAmount": 20000.00,
  "downPayment": 5000.00,
  "termMonths": 36,
  "monthlyIncome": 4500.00,
  "currency": "USD",
  "employmentStatus": "EMPLOYED"
}
```
* **Validaciones**: `vehicleId`/`financialEntityId` obligatorios (`@NotNull`); `requestedAmount` ≥ 0.01; `termMonths` 1–120; `monthlyIncome` ≥ 0.01; `currency` 3 letras; `employmentStatus` obligatorio. `simulationId` y `downPayment` son opcionales (`downPayment` default `0`).
* **📤 Response (HTTP 201 Created)**:
```json
{
  "id": "e1f2a3b4-...",
  "applicantUserId": "101",
  "vehicleId": "3f1a2b4c-...",
  "financialEntityId": "7a1b2c3d-...",
  "simulationId": "c1d2e3f4-...",
  "requestedAmount": 20000.00,
  "downPayment": 5000.00,
  "termMonths": 36,
  "monthlyIncome": 4500.00,
  "currency": "USD",
  "employmentStatus": "EMPLOYED",
  "status": "PENDING",
  "notes": null,
  "createdAt": "2026-10-04T16:00:00Z"
}
```

#### 2.26 Listar Mis Solicitudes de Crédito
* **Método**: `GET` | **Ruta**: `/api/v1/credit-applications/me` | **Acceso**: `ROLE_USER` o `ROLE_ADMIN`
* **💡 Descripción**: Historial y estado de las solicitudes del cliente autenticado.
* **💻 Uso en Frontend**: Pantalla "Mis Solicitudes" (En revisión, Pre-aprobada, Rechazada, Desembolsada).
* **📤 Response (HTTP 200 OK)** — lista (no paginada):
```json
[
  {
    "id": "e1f2a3b4-5c6d-4e7f-8a9b-0c1d2e3f4a5b",
    "applicantUserId": "101",
    "vehicleId": "3f1a2b4c-6d8e-4f0a-9b1c-2d3e4f5a6b7c",
    "financialEntityId": "7a1b2c3d-4e5f-6a7b-8c9d-0e1f2a3b4c5d",
    "simulationId": "c1d2e3f4-5a6b-4c7d-8e9f-0a1b2c3d4e5f",
    "requestedAmount": 20000.00,
    "downPayment": 5000.00,
    "termMonths": 36,
    "monthlyIncome": 4500.00,
    "currency": "USD",
    "employmentStatus": "EMPLOYED",
    "status": "IN_REVIEW",
    "notes": "En evaluación por el área de crédito.",
    "createdAt": "2026-10-04T16:00:00Z"
  },
  {
    "id": "f2a3b4c5-6d7e-4f8a-9b0c-1d2e3f4a5b6c",
    "applicantUserId": "101",
    "vehicleId": "4a2b3c4d-7e9f-4a1b-8c2d-3e4f5a6b7c8d",
    "financialEntityId": "8b2c3d4e-5f6a-4b7c-8d9e-0f1a2b3c4d5e",
    "simulationId": null,
    "requestedAmount": 15000.00,
    "downPayment": 0,
    "termMonths": 48,
    "monthlyIncome": 4500.00,
    "currency": "USD",
    "employmentStatus": "EMPLOYED",
    "status": "DISBURSED",
    "notes": null,
    "createdAt": "2026-09-10T11:20:00Z"
  }
]
```
* **📤 Response (HTTP 200 OK) `[]`**: si el usuario no tiene solicitudes.

#### 2.27 Obtener Solicitud de Crédito por ID
* **Método**: `GET` | **Ruta**: `/api/v1/credit-applications/{id}` | **Acceso**: `ROLE_USER`, `ROLE_FINANCIAL_INSTITUTION`, `ROLE_ADMIN`, `ROLE_DEALER`
* **📥 Path Param**: `id` → `UUID`
* **📤 Response (HTTP 200 OK)**:
```json
{
  "id": "e1f2a3b4-5c6d-4e7f-8a9b-0c1d2e3f4a5b",
  "applicantUserId": "101",
  "vehicleId": "3f1a2b4c-6d8e-4f0a-9b1c-2d3e4f5a6b7c",
  "financialEntityId": "7a1b2c3d-4e5f-6a7b-8c9d-0e1f2a3b4c5d",
  "simulationId": "c1d2e3f4-5a6b-4c7d-8e9f-0a1b2c3d4e5f",
  "requestedAmount": 20000.00,
  "downPayment": 5000.00,
  "termMonths": 36,
  "monthlyIncome": 4500.00,
  "currency": "USD",
  "employmentStatus": "EMPLOYED",
  "status": "PRE_APPROVED",
  "notes": "Crédito pre-aprobado sujeto a verificación domiciliaria.",
  "createdAt": "2026-10-04T16:00:00Z"
}
```
* **📤 Response (HTTP 404 Not Found)**: si el `id` no existe.

---

### 📊 Scoring Crediticio

#### 2.28 Evaluar Score Crediticio
* **Método**: `POST` | **Ruta**: `/api/v1/credit-scores` | **Acceso**: Autenticado
* **💡 Descripción**: Ejecuta el motor de riesgo y crea una evaluación (DTI, nivel de riesgo, ajuste de tasa).
* **💻 Uso en Frontend**: Botón "Calcular mi score" tras guardar una simulación.
* **📥 Request Body**:
```json
{
  "profileId": "a1b2c3d4-e5f6-4a7b-8c9d-0e1f2a3b4c5d",
  "simulationId": "c1d2e3f4-5a6b-4c7d-8e9f-0a1b2c3d4e5f",
  "monthlyIncomeAmount": 4500.00,
  "projectedMonthlyInstallmentAmount": 673.50,
  "currency": "PEN"
}
```
* **Validaciones**: `profileId`/`simulationId` obligatorios; montos ≥ 0.01; `currency` 3 letras.
* **📤 Response (HTTP 201 Created)**:
```json
{
  "id": "f1a2b3c4-...",
  "profileId": "a1b2c3d4-...",
  "simulationId": "c1d2e3f4-...",
  "currency": "PEN",
  "monthlyIncomeAmount": 4500.00,
  "projectedMonthlyInstallmentAmount": 673.50,
  "dtiRatio": 0.15,
  "riskTier": "LOW_RISK",
  "rateAdjustment": -0.50,
  "status": "COMPLETED",
  "assessmentNotes": "DTI within healthy thresholds"
}
```

#### 2.29 Listar Mis Evaluaciones de Score
* **Método**: `GET` | **Ruta**: `/api/v1/credit-scores` | **Acceso**: Autenticado (filtrado por propiedad en servidor)
* **📥 Query Params**: `page`, `size` (default 10), `sort`.
* **📤 Response (HTTP 200 OK)** — `Page<CreditScoreResource>` (ver *Formato de Respuestas Paginadas*; sin `sort` por defecto):
```json
{
  "content": [
    {
      "id": "a2b3c4d5-6e7f-4a8b-9c0d-1e2f3a4b5c6d",
      "profileId": "a1b2c3d4-e5f6-4a7b-8c9d-0e1f2a3b4c5d",
      "simulationId": "c1d2e3f4-5a6b-4c7d-8e9f-0a1b2c3d4e5f",
      "currency": "PEN",
      "monthlyIncomeAmount": 4500.00,
      "projectedMonthlyInstallmentAmount": 673.50,
      "dtiRatio": 0.15,
      "riskTier": "LOW_RISK",
      "rateAdjustment": -0.50,
      "status": "COMPLETED",
      "assessmentNotes": "DTI within healthy thresholds"
    }
  ],
  "empty": false,
  "first": true,
  "last": false,
  "number": 0,
  "numberOfElements": 1,
  "pageable": {
    "offset": 0,
    "pageNumber": 0,
    "pageSize": 10,
    "paged": true,
    "sort": { "empty": true, "sorted": false, "unsorted": true },
    "unpaged": false
  },
  "size": 10,
  "sort": { "empty": true, "sorted": false, "unsorted": true },
  "totalElements": 4,
  "totalPages": 1
}
```
* **⚠️ Nota**: `totalElements` corresponde al total **sin** filtrar (el filtrado por propiedad ocurre en memoria).

#### 2.30 Obtener Score por ID
* **Método**: `GET` | **Ruta**: `/api/v1/credit-scores/{id}` | **Acceso**: Propietario o `ROLE_ADMIN`
* **📥 Path Param**: `id` → `UUID`
* **📤 Response (HTTP 200 OK)**:
```json
{
  "id": "a2b3c4d5-6e7f-4a8b-9c0d-1e2f3a4b5c6d",
  "profileId": "a1b2c3d4-e5f6-4a7b-8c9d-0e1f2a3b4c5d",
  "simulationId": "c1d2e3f4-5a6b-4c7d-8e9f-0a1b2c3d4e5f",
  "currency": "PEN",
  "monthlyIncomeAmount": 4500.00,
  "projectedMonthlyInstallmentAmount": 673.50,
  "dtiRatio": 0.15,
  "riskTier": "LOW_RISK",
  "rateAdjustment": -0.50,
  "status": "COMPLETED",
  "assessmentNotes": "DTI within healthy thresholds"
}
```
* **📤 Response (HTTP 403 Forbidden)**: si no es propietario y no tiene rol `ADMIN`.
* **📤 Response (HTTP 404 Not Found)**: si el `id` no existe.

#### 2.31 Listar Scores por Perfil
* **Método**: `GET` | **Ruta**: `/api/v1/credit-scores/profile/{profileId}` | **Acceso**: Propietario del perfil o `ROLE_ADMIN`
* **📥 Path Param**: `profileId` → `String`
* **📤 Response (HTTP 200 OK)** — lista (no paginada) con el mismo objeto de 2.30:
```json
[
  {
    "id": "a2b3c4d5-6e7f-4a8b-9c0d-1e2f3a4b5c6d",
    "profileId": "a1b2c3d4-e5f6-4a7b-8c9d-0e1f2a3b4c5d",
    "simulationId": "c1d2e3f4-5a6b-4c7d-8e9f-0a1b2c3d4e5f",
    "currency": "PEN",
    "monthlyIncomeAmount": 4500.00,
    "projectedMonthlyInstallmentAmount": 673.50,
    "dtiRatio": 0.15,
    "riskTier": "LOW_RISK",
    "rateAdjustment": -0.50,
    "status": "COMPLETED",
    "assessmentNotes": "DTI within healthy thresholds"
  },
  {
    "id": "b3c4d5e6-7f8a-4b9c-0d1e-2f3a4b5c6d7e",
    "profileId": "a1b2c3d4-e5f6-4a7b-8c9d-0e1f2a3b4c5d",
    "simulationId": "d2e3f4a5-6b7c-4d8e-9f0a-1b2c3d4e5f6a",
    "currency": "PEN",
    "monthlyIncomeAmount": 4500.00,
    "projectedMonthlyInstallmentAmount": 980.00,
    "dtiRatio": 0.22,
    "riskTier": "MEDIUM_RISK",
    "rateAdjustment": 0.75,
    "status": "COMPLETED",
    "assessmentNotes": "DTI elevated for this profile"
  }
]
```
* **📤 Response (HTTP 403 Forbidden)**: si el perfil no pertenece al usuario y no tiene rol `ADMIN`.

#### 2.32 Eliminar Evaluación de Score
* **Método**: `DELETE` | **Ruta**: `/api/v1/credit-scores/{id}` | **Acceso**: Propietario o `ROLE_ADMIN`
* **📤 Response (HTTP 204 No Content)**.

---

### 📉 Depreciación Vehicular Proyectada

#### 2.33 Calcular Depreciación
* **Método**: `POST` | **Ruta**: `/api/v1/depreciation-projections` | **Acceso**: Autenticado
* **💡 Descripción**: Proyecta la pérdida de valor del vehículo (2, 3 y 5 años) con tasa anual de depreciación y recomendación de compra.
* **💻 Uso en Frontend**: Gráfico de depreciación en la vista del vehículo o junto a la simulación.
* **📥 Request Body**:
```json
{
  "vehicleId": "3f1a2b4c-6d8e-4f0a-9b1c-2d3e4f5a6b7c",
  "simulationId": "c1d2e3f4-5a6b-4c7d-8e9f-0a1b2c3d4e5f",
  "initialVehiclePriceAmount": 25000.00,
  "currency": "USD",
  "manufactureYear": 2025,
  "motorizationType": "HYBRID",
  "balloonPaymentAmount": 6000.00
}
```
* **Validaciones**: precios ≥ 0.01; `manufactureYear` 1900–2100; `balloonPaymentAmount` ≥ 0.
* **📤 Response (HTTP 201 Created)**:
```json
{
  "id": "a2b3c4d5-...",
  "vehicleId": "3f1a2b4c-...",
  "simulationId": "c1d2e3f4-...",
  "currency": "USD",
  "initialVehiclePriceAmount": 25000.00,
  "manufactureYear": 2025,
  "motorizationType": "HYBRID",
  "annualDepreciationRate": 0.12,
  "projectedValue2YearsAmount": 19500.00,
  "projectedValue3YearsAmount": 17160.00,
  "projectedValue5YearsAmount": 13305.60,
  "balloonPaymentAmount": 6000.00,
  "recommendedAction": "BUY",
  "advisoryNotes": "Projected value at term end covers balloon payment."
}
```

#### 2.34 Listar Mis Proyecciones de Depreciación
* **Método**: `GET` | **Ruta**: `/api/v1/depreciation-projections` | **Acceso**: Autenticado (filtrado por propiedad en servidor)
* **📥 Query Params**: `page`, `size` (default 10), `sort`.
* **📥 Query Params**: `page`, `size` (default 10), `sort`.
* **📤 Response (HTTP 200 OK)** — `Page<DepreciationProjectionResource>` (ver *Formato de Respuestas Paginadas*; sin `sort` por defecto):
```json
{
  "content": [
    {
      "id": "a2b3c4d5-6e7f-4a8b-9c0d-1e2f3a4b5c6d",
      "vehicleId": "3f1a2b4c-6d8e-4f0a-9b1c-2d3e4f5a6b7c",
      "simulationId": "c1d2e3f4-5a6b-4c7d-8e9f-0a1b2c3d4e5f",
      "currency": "USD",
      "initialVehiclePriceAmount": 25000.00,
      "manufactureYear": 2025,
      "motorizationType": "HYBRID",
      "annualDepreciationRate": 0.12,
      "projectedValue2YearsAmount": 19500.00,
      "projectedValue3YearsAmount": 17160.00,
      "projectedValue5YearsAmount": 13305.60,
      "balloonPaymentAmount": 6000.00,
      "recommendedAction": "BUY",
      "advisoryNotes": "Projected value at term end covers balloon payment."
    }
  ],
  "empty": false,
  "first": true,
  "last": false,
  "number": 0,
  "numberOfElements": 1,
  "pageable": {
    "offset": 0,
    "pageNumber": 0,
    "pageSize": 10,
    "paged": true,
    "sort": { "empty": true, "sorted": false, "unsorted": true },
    "unpaged": false
  },
  "size": 10,
  "sort": { "empty": true, "sorted": false, "unsorted": true },
  "totalElements": 2,
  "totalPages": 1
}
```
* **⚠️ Nota**: `totalElements` corresponde al total **sin** filtrar (el filtrado por propiedad ocurre en memoria).

#### 2.35 Obtener Proyección por ID
* **Método**: `GET` | **Ruta**: `/api/v1/depreciation-projections/{id}` | **Acceso**: Propietario o `ROLE_ADMIN`
* **📥 Path Param**: `id` → `UUID`
* **📤 Response (HTTP 200 OK)**:
```json
{
  "id": "a2b3c4d5-6e7f-4a8b-9c0d-1e2f3a4b5c6d",
  "vehicleId": "3f1a2b4c-6d8e-4f0a-9b1c-2d3e4f5a6b7c",
  "simulationId": "c1d2e3f4-5a6b-4c7d-8e9f-0a1b2c3d4e5f",
  "currency": "USD",
  "initialVehiclePriceAmount": 25000.00,
  "manufactureYear": 2025,
  "motorizationType": "HYBRID",
  "annualDepreciationRate": 0.12,
  "projectedValue2YearsAmount": 19500.00,
  "projectedValue3YearsAmount": 17160.00,
  "projectedValue5YearsAmount": 13305.60,
  "balloonPaymentAmount": 6000.00,
  "recommendedAction": "BUY",
  "advisoryNotes": "Projected value at term end covers balloon payment."
}
```
* **📤 Response (HTTP 403 Forbidden)**: si no es propietario y no tiene rol `ADMIN`.
* **📤 Response (HTTP 404 Not Found)**: si el `id` no existe.

#### 2.36 Listar Proyecciones por Vehículo
* **Método**: `GET` | **Ruta**: `/api/v1/depreciation-projections/vehicle/{vehicleId}` | **Acceso**: Propietario del vehículo o `ROLE_ADMIN`
* **📥 Path Param**: `vehicleId` → `String`
* **📤 Response (HTTP 200 OK)** — lista (no paginada) con el mismo objeto de 2.35:
```json
[
  {
    "id": "a2b3c4d5-6e7f-4a8b-9c0d-1e2f3a4b5c6d",
    "vehicleId": "3f1a2b4c-6d8e-4f0a-9b1c-2d3e4f5a6b7c",
    "simulationId": "c1d2e3f4-5a6b-4c7d-8e9f-0a1b2c3d4e5f",
    "currency": "USD",
    "initialVehiclePriceAmount": 25000.00,
    "manufactureYear": 2025,
    "motorizationType": "HYBRID",
    "annualDepreciationRate": 0.12,
    "projectedValue2YearsAmount": 19500.00,
    "projectedValue3YearsAmount": 17160.00,
    "projectedValue5YearsAmount": 13305.60,
    "balloonPaymentAmount": 6000.00,
    "recommendedAction": "BUY",
    "advisoryNotes": "Projected value at term end covers balloon payment."
  }
]
```

#### 2.37 Eliminar Proyección
* **Método**: `DELETE` | **Ruta**: `/api/v1/depreciation-projections/{id}` | **Acceso**: Propietario o `ROLE_ADMIN`
* **📤 Response (HTTP 204 No Content)**.

---

### 🤖 Asesor Financiero IA (Chatbot)

#### 2.38 Enviar Consulta a la IA
* **Método**: `POST` | **Rutas**: `/api/v1/consultations/chat`, `/api/v1/consultations` *(alias: `/api/v1/ai/consultations/chat`, `/api/v1/ai/consultations`)* | **Acceso**: Autenticado
* **💡 Descripción**: Envía una consulta financiera/vehicular al asesor IA y devuelve una recomendación estructurada.
* **💻 Uso en Frontend**: Widget de Chat Flotante "Asesor IA".
* **📥 Request Body**:
```json
{
  "prompt": "¿Me conviene financiar un auto de $25,000 con mi sueldo de S/ 4,500?",
  "monthlyIncome": 4500.00,
  "maxBudget": 25000.00,
  "currency": "PEN"
}
```
* **Validaciones**: `prompt` obligatorio (`@NotBlank`). `currency` default `PEN` si es `null`.
* **📤 Response (HTTP 201 Created)**:
```json
{
  "id": "b1c2d3e4-...",
  "userId": "101",
  "prompt": "¿Me conviene financiar un auto de $25,000...",
  "monthlyIncome": 4500.00,
  "maxBudget": 25000.00,
  "currency": "PEN",
  "recommendationText": "Basado en tu nivel de ingresos y el costo del vehículo...",
  "recommendedVehicleCategory": "SEDAN_MID",
  "estimatedMaxMonthlyFee": 1350.00,
  "createdAt": "2026-10-04T17:00:00Z"
}
```

#### 2.39 Historial de Consultas IA
* **Método**: `GET` | **Rutas**: `/api/v1/consultations/history` y `/api/v1/ai/consultations/history` | **Acceso**: Autenticado
* **📤 Response (HTTP 200 OK)** — lista (no paginada), de la más reciente a la más antigua:
```json
[
  {
    "id": "b1c2d3e4-5f6a-4b7c-8d9e-0f1a2b3c4d5e",
    "userId": "101",
    "prompt": "¿Me conviene financiar un auto de $25,000 con mi sueldo de S/ 4,500?",
    "monthlyIncome": 4500.00,
    "maxBudget": 25000.00,
    "currency": "PEN",
    "recommendationText": "Basado en tu nivel de ingresos y el costo del vehículo...",
    "recommendedVehicleCategory": "SEDAN_MID",
    "estimatedMaxMonthlyFee": 1350.00,
    "createdAt": "2026-10-04T17:00:00Z"
  },
  {
    "id": "c2d3e4f5-6a7b-4c8d-9e0f-1a2b3c4d5e6f",
    "userId": "101",
    "prompt": "¿Cuánto debo dar de enganche para un auto de $30,000?",
    "monthlyIncome": 4500.00,
    "maxBudget": 30000.00,
    "currency": "PEN",
    "recommendationText": "Un enganche del 20% mantiene tu cuota mensual dentro del 30% de tus ingresos...",
    "recommendedVehicleCategory": "SUV_COMPACT",
    "estimatedMaxMonthlyFee": 1350.00,
    "createdAt": "2026-10-03T09:45:00Z"
  }
]
```
* **📤 Response (HTTP 200 OK) `[]`**: si aún no hay consultas.

#### 2.40 Recomendaciones de Vehículos IA
* **Método**: `GET` | **Rutas**: `/api/v1/consultations/recommendations` y `/api/v1/ai/consultations/recommendations` | **Acceso**: Autenticado
* **💡 Descripción**: Devuelve una selección de vehículos recomendados (hasta 6).
* **⚠️ Nota de implementación**: la implementación actual retorna los primeros 6 vehículos del catálogo **sin filtrar** por perfil ni capacidad financiera (diverge del Javadoc).
* **📤 Response (HTTP 200 OK)** — lista de hasta 6 `VehicleResource`:
```json
[
  {
    "id": "3f1a2b4c-6d8e-4f0a-9b1c-2d3e4f5a6b7c",
    "userId": "101",
    "financialEntityId": "7a1b2c3d-4e5f-6a7b-8c9d-0e1f2a3b4c5d",
    "brand": "Toyota",
    "model": "Corolla Cross",
    "manufactureYear": 2025,
    "condition": "NEW",
    "priceAmount": 26990.00,
    "currency": "USD",
    "imagePath": "https://res.cloudinary.com/.../corolla.jpg",
    "status": "ACTIVE",
    "mileage": 0,
    "transmission": "AUTOMATIC",
    "engine": "2.0L",
    "traction": "FWD",
    "images": ["https://res.cloudinary.com/.../1.jpg"],
    "createdAt": "2026-09-20T10:00:00Z"
  }
]
```

---

### 🎯 CRM: Prospectos y Pruebas de Manejo (como Cliente)

#### 2.41 Registrar Prospecto (Alta)
* **Método**: `POST` | **Rutas**: `/api/v1/prospects` y `/api/v1/dealers/me/prospects` | **Acceso**: Autenticado
* **💡 Descrepción**: Crea un prospecto de compra asociado al usuario autenticado.
* **💻 Uso en Frontend**: Formulario "Estoy interesado" desde la ficha de un vehículo.
* **📥 Request Body**:
```json
{
  "fullName": "Maria Lopez",
  "email": "maria.lopez@example.com",
  "phone": "+51987654321",
  "interestedVehicleId": "3f1a2b4c-6d8e-4f0a-9b1c-2d3e4f5a6b7c",
  "salesAgentId": "c1d2e3f4-5a6b-4c7d-8e9f-0a1b2c3d4e5f"
}
```
* **📤 Response (HTTP 201 Created)**: objeto `ProspectResource` (ver 3.20).

#### 2.42 Agendar Prueba de Manejo
* **Método**: `POST` | **Ruta**: `/api/v1/test-drives` | **Acceso**: Autenticado
* **💡 Descripción**: Programa una cita de test drive para un vehículo en un concesionario.
* **📥 Request Body**:
```json
{
  "vehicleId": "3f1a2b4c-6d8e-4f0a-9b1c-2d3e4f5a6b7c",
  "dealershipId": "9b8c7d6e-5f4a-4b3c-2d1e-0f9a8b7c6d5e",
  "scheduledDateTime": "2026-10-10T10:00:00",
  "notes": "Cliente prefiere probar el vehículo en autopista."
}
```
* **Validaciones**: `vehicleId`, `dealershipId` y `scheduledDateTime` obligatorios.
* **📤 Response (HTTP 201 Created)**:
```json
{
  "id": "c2d3e4f5-...",
  "buyerUserId": "101",
  "vehicleId": "3f1a2b4c-...",
  "dealershipId": "9b8c7d6e-...",
  "scheduledDateTime": "2026-10-10T10:00:00",
  "status": "SCHEDULED",
  "notes": "Cliente prefiere probar el vehículo en autopista."
}
```

#### 2.43 Listar Mis Pruebas de Manejo
* **Método**: `GET` | **Ruta**: `/api/v1/test-drives/me` | **Acceso**: Autenticado
* **📤 Response (HTTP 200 OK)** — lista (no paginada):
```json
[
  {
    "id": "c2d3e4f5-6a7b-4c8d-9e0f-1a2b3c4d5e6f",
    "buyerUserId": "101",
    "vehicleId": "3f1a2b4c-6d8e-4f0a-9b1c-2d3e4f5a6b7c",
    "dealershipId": "9b8c7d6e-5f4a-4b3c-2d1e-0f9a8b7c6d5e",
    "scheduledDateTime": "2026-10-10T10:00:00",
    "status": "SCHEDULED",
    "notes": "Cliente prefiere probar el vehículo en autopista."
  },
  {
    "id": "d3e4f5a6-7b8c-4d9e-0f1a-2b3c4d5e6f7a",
    "buyerUserId": "101",
    "vehicleId": "4a2b3c4d-7e9f-4a1b-8c2d-3e4f5a6b7c8d",
    "dealershipId": "9b8c7d6e-5f4a-4b3c-2d1e-0f9a8b7c6d5e",
    "scheduledDateTime": "2026-09-28T15:30:00",
    "status": "COMPLETED",
    "notes": null
  }
]
```
* **📤 Response (HTTP 200 OK) `[]`**: si el usuario no tiene pruebas de manejo.

#### 2.44 Obtener Prueba de Manejo por ID
* **Método**: `GET` | **Ruta**: `/api/v1/test-drives/{id}` | **Acceso**: Autenticado
* **📥 Path Param**: `id` → `UUID`
* **📤 Response (HTTP 200 OK)**:
```json
{
  "id": "c2d3e4f5-6a7b-4c8d-9e0f-1a2b3c4d5e6f",
  "buyerUserId": "101",
  "vehicleId": "3f1a2b4c-6d8e-4f0a-9b1c-2d3e4f5a6b7c",
  "dealershipId": "9b8c7d6e-5f4a-4b3c-2d1e-0f9a8b7c6d5e",
  "scheduledDateTime": "2026-10-10T10:00:00",
  "status": "SCHEDULED",
  "notes": "Cliente prefiere probar el vehículo en autopista."
}
```
* **📤 Response (HTTP 404 Not Found)**: si el `id` no existe.
* **⚠️ Nota de implementación**: este endpoint solo exige autenticación, **no valida propiedad** del recurso.

#### 2.45 Actualizar Estado de Prueba de Manejo
* **Método**: `PATCH` | **Ruta**: `/api/v1/test-drives/{id}/status` | **Acceso**: Autenticado
* **📥 Request Body**:
```json
{ "status": "CONFIRMED" }
```
* **⚠️ Nota de implementación**: `status` no tiene validación de formato en el backend; tampoco se valida propiedad.
* **📤 Response (HTTP 200 OK)**: objeto `TestDriveResource` actualizado.

#### 2.46 Cancelar / Eliminar Prueba de Manejo
* **Método**: `DELETE` | **Ruta**: `/api/v1/test-drives/{id}` | **Acceso**: Autenticado
* **📤 Response (HTTP 204 No Content)**.

---

### 💳 Planes, Suscripciones y Facturas (Cualquier Usuario Autenticado)

#### 2.47 Listar Planes de Suscripción Activos
* **Método**: `GET` | **Ruta**: `/api/v1/billing/plans` | **Acceso**: Autenticado
* **💻 Uso en Frontend**: Página de precios / selector de plan.
* **📤 Response (HTTP 200 OK)**:
```json
[
  {
    "id": 1,
    "name": "PRO",
    "description": "Para concesionarios en crecimiento",
    "price": 99.00,
    "currency": "USD",
    "billingCycle": "MONTHLY",
    "maxVehicleListings": 50,
    "maxSimulationsPerMonth": 500,
    "active": true,
    "stripePriceId": "price_1AbCdEfGhIjKlMn"
  }
]
```

#### 2.48 Obtener Plan por ID
* **Método**: `GET` | **Ruta**: `/api/v1/billing/plans/{planId}` | **Acceso**: Autenticado
* **📥 Path Param**: `planId` → `Long`
* **📤 Response (HTTP 200 OK)**:
```json
{
  "id": 1,
  "name": "PRO",
  "description": "Para concesionarios en crecimiento",
  "price": 99.00,
  "currency": "USD",
  "billingCycle": "MONTHLY",
  "maxVehicleListings": 50,
  "maxSimulationsPerMonth": 500,
  "active": true,
  "stripePriceId": "price_1AbCdEfGhIjKlMn"
}
```
* **📤 Response (HTTP 404 Not Found)**: si el `planId` no existe.

#### 2.49 Obtener Mi Suscripción Activa
* **Método**: `GET` | **Ruta**: `/api/v1/billing/subscriptions/me` | **Acceso**: Autenticado
* **📤 Response (HTTP 200 OK)**:
```json
{
  "id": 7,
  "userId": "101",
  "plan": { "id": 1, "name": "PRO", "price": 99.00, "currency": "USD", "billingCycle": "MONTHLY" },
  "status": "ACTIVE",
  "startDate": "2026-10-01T00:00:00",
  "endDate": "2026-11-01T00:00:00",
  "autoRenew": true,
  "active": true
}
```

#### 2.50 Suscribirme a un Plan
* **Método**: `POST` | **Ruta**: `/api/v1/billing/subscriptions` | **Acceso**: Autenticado
* **💡 Descripción**: Crea directamente una suscripción (sin pasar por Stripe). Para pagos con tarjeta usar 2.52.
* **📥 Request Body**:
```json
{
  "planId": 1,
  "autoRenew": true
}
```
* **Validaciones**: `planId` obligatorio (`@NotNull`).
* **📤 Response (HTTP 201 Created)**: objeto `SubscriptionResource`.

#### 2.51 Cancelar Mi Suscripción
* **Método**: `DELETE` | **Ruta**: `/api/v1/billing/subscriptions/{subscriptionId}` | **Acceso**: Autenticado
* **📥 Path Param**: `subscriptionId` → `Long`
* **📤 Response (HTTP 200 OK)**: objeto `SubscriptionResource` cancelado. `404` si no existe o no pertenece al usuario.

#### 2.52 Crear Sesión de Checkout Stripe
* **Método**: `POST` | **Ruta**: `/api/v1/billing/subscriptions/checkout-session` | **Acceso**: Autenticado
* **💡 Descripción**: Inicia el pago con Stripe Checkout. Devuelve la URL a la que debe redirigirse el usuario.
* **💻 Uso en Frontend**: Al elegir un plan de pago, invocar este endpoint y `window.location.href = checkoutUrl`.
* **📥 Request Body**:
```json
{
  "stripePriceId": "price_1AbCdEfGhIjKlMn",
  "successUrl": "https://miapp.com/billing/success",
  "cancelUrl": "https://miapp.com/billing/cancel"
}
```
* **Validaciones**: `stripePriceId` obligatorio y no vacío.
* **📤 Response (HTTP 200 OK)**:
```json
{
  "checkoutUrl": "https://checkout.stripe.com/c/pay/cs_test_a1b2c3d4..."
}
```

#### 2.53 Listar Mis Facturas
* **Método**: `GET` | **Ruta**: `/api/v1/billing/invoices/me` | **Acceso**: Autenticado
* **💡 Descripción**: Devuelve hasta **50** facturas del usuario autenticado.
* **📤 Response (HTTP 200 OK)**:
```json
[
  {
    "id": 12,
    "subscriptionId": 7,
    "userId": "101",
    "amount": 99.00,
    "currency": "USD",
    "status": "PAID",
    "issuedAt": "2026-10-01T00:00:00",
    "dueDate": "2026-10-08T00:00:00",
    "paidAt": "2026-10-02T14:30:00"
  }
]
```

#### 2.54 Descargar Factura PDF
* **Método**: `GET` | **Ruta**: `/api/v1/billing/invoices/{invoiceId}/pdf` | **Acceso**: Autenticado
* **💡 Descripción**: Retorna el archivo PDF de la factura (`Content-Type: application/pdf`, `Content-Disposition: inline`).
* **💻 Uso en Frontend**: Botón "Descargar Factura PDF" → abrir en nueva pestaña o descargar binaria (`responseType: 'blob'`).
* **📥 Path Param**: `invoiceId` → `Long`
* **📤 Response (HTTP 200 OK)**: bytes PDF. `404` si no existe.
* **⚠️ Nota de implementación**: no valida que la factura pertenezca al usuario actual (solo exige autenticación).

#### 2.55 Marcar Factura como Pagada (Conciliación Manual)
* **Método**: `PATCH` | **Ruta**: `/api/v1/billing/invoices/{invoiceId}` | **Acceso**: Autenticado
* **💡 Descripción**: Atajo de conciliación manual/offline que marca una factura pendiente como pagada. Para pagos con tarjeta usar 2.52.
* **📥 Path Param**: `invoiceId` → `Long`
* **📤 Response (HTTP 200 OK)**: objeto `InvoiceResource` con `status: "PAID"`.

---

---

## 🚗 3. Endpoints de Concesionarios (`ROLE_DEALER`)

Requieren autenticación con rol `ROLE_DEALER` (o `ROLE_ADMIN`). Los endpoints marcados como *(también ADMIN)* aceptan ambos.

---

### 🚗 Inventario de Vehículos

#### 3.1 Registrar Vehículo en Inventario
* **Método**: `POST` | **Ruta**: `/api/v1/vehicles` | **Acceso**: `ROLE_DEALER`, `ROLE_ADMIN`
* **💡 Descripción**: Publica un nuevo vehículo. El `userId` real siempre se toma del token autenticado (ignora el del body).
* **💻 Uso en Frontend**: Formulario de Publicación de Vehículos en el Portal Concesionario.
* **📥 Request Body**:
```json
{
  "userId": "101",
  "financialEntityId": "7a1b2c3d-4e5f-6a7b-8c9d-0e1f2a3b4c5d",
  "brand": "Toyota",
  "model": "RAV4 Hybrid",
  "manufactureYear": 2026,
  "condition": "NEW",
  "priceAmount": 34990.00,
  "currency": "USD",
  "imagePath": "https://res.cloudinary.com/demo/image/upload/v1/rav4.jpg",
  "status": "ACTIVE",
  "mileage": 0,
  "transmission": "AUTOMATIC",
  "engine": "2.5L Hybrid",
  "traction": "AWD",
  "images": ["https://res.cloudinary.com/.../1.jpg", "https://res.cloudinary.com/.../2.jpg"]
}
```
* **Validaciones**: `brand`/`model` obligatorios (máx. 50); `manufactureYear` 1900–2100; `condition` obligatorio (máx. 30); `priceAmount` ≥ 0.01; `currency` 3 letras; `mileage` ≥ 0; `financialEntityId` obligatorio.
* **📤 Response (HTTP 201 Created)**: objeto `VehicleResource`.

#### 3.2 Actualizar Vehículo de Inventario
* **Método**: `PUT` | **Ruta**: `/api/v1/vehicles/{vehicleId}` | **Acceso**: Propietario del vehículo *(ADMIN no está incluido en esta regla)*
* **📥 Request Body**: mismos campos de 3.1 **excepto `userId`** (14 campos, mismas validaciones).
* **📤 Response (HTTP 200 OK)**: objeto `VehicleResource` actualizado.

#### 3.3 Cambiar Estado del Vehículo
* **Método**: `PATCH` | **Ruta**: `/api/v1/vehicles/{vehicleId}/status` | **Acceso**: Propietario del vehículo
* **💡 Descripción**: Marca el vehículo como disponible, reservado o vendido.
* **📥 Request Body**:
```json
{ "status": "SOLD" }
```
* **Validaciones**: `status` → regex exacta `^(?i)(ACTIVE|SOLD|RESERVED)$`.
* **📤 Response (HTTP 200 OK)**: objeto `VehicleResource`.

#### 3.4 Eliminar / Dar de Baja Vehículo
* **Método**: `DELETE` | **Ruta**: `/api/v1/vehicles/{vehicleId}` | **Acceso**: Propietario del vehículo
* **📤 Response (HTTP 204 No Content)**.

#### 3.5 Subir Imagen de Portada
* **Método**: `POST` | **Ruta**: `/api/v1/vehicles/{vehicleId}/image` | **Acceso**: Propietario del vehículo
* **💡 Descripción**: Sube la foto de portada a **Cloudinary** y actualiza `imagePath`.
* **💻 Uso en Frontend**: `FormData` con `enctype="multipart/form-data"`.
* **📥 Request**: `multipart/form-data`, campo **`file`**.
  * MIME permitidos: `image/jpeg`, `image/png`, `image/webp`, `image/gif`
  * Tamaño máximo: **10 MB**
* **📤 Response (HTTP 200 OK)**: objeto `VehicleResource` con `imagePath` actualizado. `404` si el vehículo no existe.

#### 3.6 Subir Imagen a la Galería
* **Método**: `POST` | **Ruta**: `/api/v1/vehicles/{vehicleId}/images` | **Acceso**: Propietario del vehículo
* **💡 Descripción**: Agrega una imagen adicional al arreglo `images` del vehículo.
* **📥 Request**: `multipart/form-data`, campo **`file`** (mismas restricciones que 3.5).
* **📤 Response (HTTP 200 OK)**: objeto `VehicleResource` con `images` actualizado.

#### 3.7 Eliminar Imagen de la Galería por Índice
* **Método**: `DELETE` | **Ruta**: `/api/v1/vehicles/{vehicleId}/images/{imageIndex}` | **Acceso**: Propietario del vehículo
* **📥 Path Params**: `vehicleId` → `UUID`; `imageIndex` → `int` (base 0).
* **📤 Response (HTTP 200 OK)**: objeto `VehicleResource`. `400` si el índice está fuera de rango.

---

### 👥 Asesores de Ventas (Recurso Interno del Concesionario)

> Estos registros **no tienen rol propio**: un asesor es un registro de la cuenta `ROLE_DEALER`.

#### 3.8 Listar Mis Asesores de Ventas
* **Método**: `GET` | **Ruta**: `/api/v1/dealers/me/sales-agents` | **Acceso**: `ROLE_DEALER`, `ROLE_ADMIN`
* **📤 Response (HTTP 200 OK)**:
```json
[
  {
    "id": "c1d2e3f4-5a6b-4c7d-8e9f-0a1b2c3d4e5f",
    "dealerUserId": "101",
    "fullName": "Carlos Mendoza",
    "email": "carlos.mendoza@autoland.pe",
    "phone": "+51987654321",
    "active": true
  }
]
```

#### 3.9 Crear Asesor de Ventas
* **Método**: `POST` | **Ruta**: `/api/v1/dealers/me/sales-agents` | **Acceso**: `ROLE_DEALER`, `ROLE_ADMIN`
* **📥 Request Body**:
```json
{
  "fullName": "Carlos Mendoza",
  "email": "carlos.mendoza@autoland.pe",
  "phone": "+51987654321",
  "active": true
}
```
* **⚠️ Nota**: este DTO **no tiene validaciones** (`@Valid` no se aplica); el backend no valida formato de email ni campos obligatorios.
* **📤 Response (HTTP 201 Created)**: objeto `SalesAgentResource`.

#### 3.10 Actualizar Asesor de Ventas
* **Método**: `PUT` | **Ruta**: `/api/v1/dealers/me/sales-agents/{id}` | **Acceso**: `ROLE_DEALER`, `ROLE_ADMIN`
* **📥 Path Param**: `id` → `UUID`
* **📥 Request Body**: mismo objeto que 3.9 (`active: null` se interpreta como `true`).
* **📤 Response (HTTP 200 OK)**: objeto `SalesAgentResource`.

#### 3.11 Reasignar Prospectos entre Asesores
* **Método**: `POST` | **Ruta**: `/api/v1/dealers/me/sales-agents/{id}/reassign-leads` | **Acceso**: `ROLE_DEALER`, `ROLE_ADMIN`
* **💡 Descripción**: Transfiere **todos** los prospectos del asesor `{id}` al asesor `targetAgentId`.
* **📥 Path Param**: `id` → `UUID` (asesor origen)
* **📥 Request Body**:
```json
{ "targetAgentId": "e1f2a3b4-5c6d-4e7f-8a9b-0c1d2e3f4a5b" }
```
* **📤 Response (HTTP 204 No Content)**.

---

### 🏢 Perfil del Concesionario (Dealership)

#### 3.12 Directorio de Concesionarios
* **Método**: `GET` | **Ruta**: `/api/v1/dealerships` | **Acceso**: Autenticado
* **📥 Query Params**: `search` (String, opcional), `page`, `size` (default 10), `sort` (default `name,asc`).
* **📤 Response (HTTP 200 OK)** — `Page<DealershipResource>` (ver *Formato de Respuestas Paginadas*):
```json
{
  "content": [
    {
      "id": "9b8c7d6e-5f4a-4b3c-2d1e-0f9a8b7c6d5e",
      "userId": "101",
      "ruc": "20601234567",
      "name": "AUTOLAND PERU S.A.C.",
      "address": "AV. JAVIER PRADO ESTE 410",
      "phone": "+51987654321",
      "email": "contacto@autoland.pe",
      "website": "https://autoland.pe",
      "description": "Concesionario oficial Toyota",
      "operatingHours": "Lun-Sáb 9:00-19:00",
      "rating": 4.7,
      "logoUrl": "https://res.cloudinary.com/.../logo.png",
      "bannerUrl": "https://res.cloudinary.com/.../banner.png",
      "active": true
    }
  ],
  "empty": false,
  "first": true,
  "last": false,
  "number": 0,
  "numberOfElements": 1,
  "pageable": {
    "offset": 0,
    "pageNumber": 0,
    "pageSize": 10,
    "paged": true,
    "sort": { "empty": false, "sorted": true, "unsorted": false },
    "unpaged": false
  },
  "size": 10,
  "sort": { "empty": false, "sorted": true, "unsorted": false },
  "totalElements": 24,
  "totalPages": 3
}
```

#### 3.13 Obtener Mi Perfil de Concesionario
* **Método**: `GET` | **Ruta**: `/api/v1/dealerships/me` | **Acceso**: `ROLE_DEALER`, `ROLE_ADMIN`
* **📤 Response (HTTP 200 OK)**: objeto `DealershipResource`:
```json
{
  "id": "9b8c7d6e-5f4a-4b3c-2d1e-0f9a8b7c6d5e",
  "userId": "101",
  "ruc": "20601234567",
  "name": "AUTOLAND PERU S.A.C.",
  "address": "AV. JAVIER PRADO ESTE 410",
  "phone": "+51987654321",
  "email": "contacto@autoland.pe",
  "website": "https://autoland.pe",
  "description": "Concesionario oficial Toyota",
  "operatingHours": "Lun-Sáb 9:00-19:00",
  "rating": 4.7,
  "logoUrl": "https://res.cloudinary.com/.../logo.png",
  "bannerUrl": "https://res.cloudinary.com/.../banner.png",
  "active": true
}
```
* **📤 Response (HTTP 404)**: si aún no se creó el perfil.

#### 3.14 Crear / Actualizar Mi Perfil de Concesionario
* **Método**: `PUT` | **Ruta**: `/api/v1/dealerships/me` | **Acceso**: `ROLE_DEALER`, `ROLE_ADMIN`
* **💡 Descripción**: Operación *upsert*: crea el perfil si no existe, lo actualiza si existe.
* **📥 Request Body**:
```json
{
  "ruc": "20601234567",
  "name": "AUTOLAND PERU S.A.C.",
  "address": "AV. JAVIER PRADO ESTE 410, SAN ISIDRO",
  "phone": "+51987654321",
  "email": "contacto@autoland.pe",
  "website": "https://autoland.pe",
  "description": "Concesionario oficial Toyota",
  "operatingHours": "Lun-Sáb 9:00-19:00",
  "logoUrl": "https://res.cloudinary.com/.../logo.png",
  "bannerUrl": "https://res.cloudinary.com/.../banner.png"
}
```
* **Validaciones**: `ruc` → `^\d{11}$`; `name` obligatorio (máx. 255); `address` obligatorio (máx. 500); `logoUrl`/`bannerUrl` → deben ser URLs (`^https?://.+$`) o vacías, máx. 1000.
* **📤 Response (HTTP 200 OK)**: objeto `DealershipResource`.

#### 3.15 Subir Logo del Concesionario
* **Método**: `POST` | **Ruta**: `/api/v1/dealerships/me/logo` | **Acceso**: `ROLE_DEALER`, `ROLE_ADMIN`
* **📥 Request**: `multipart/form-data`, campo **`file`** (JPEG/PNG/WebP/GIF, máx. 10 MB).
* **📤 Response (HTTP 200 OK)**: objeto `DealershipResource` con `logoUrl` actualizado (borra la imagen anterior).

#### 3.16 Subir Banner del Concesionario
* **Método**: `POST` | **Ruta**: `/api/v1/dealerships/me/banner` | **Acceso**: `ROLE_DEALER`, `ROLE_ADMIN`
* **📥 Request**: `multipart/form-data`, campo **`file`** (mismas restricciones).
* **📤 Response (HTTP 200 OK)**: objeto `DealershipResource` con `bannerUrl` actualizado.

#### 3.17 Obtener Concesionario por ID
* **Método**: `GET` | **Ruta**: `/api/v1/dealerships/{id}` | **Acceso**: Autenticado
* **📥 Path Param**: `id` → `UUID`
* **📤 Response (HTTP 200 OK)**:
```json
{
  "id": "9b8c7d6e-5f4a-4b3c-2d1e-0f9a8b7c6d5e",
  "userId": "101",
  "ruc": "20601234567",
  "name": "AUTOLAND PERU S.A.C.",
  "address": "AV. JAVIER PRADO ESTE 410",
  "phone": "+51987654321",
  "email": "contacto@autoland.pe",
  "website": "https://autoland.pe",
  "description": "Concesionario oficial Toyota",
  "operatingHours": "Lun-Sáb 9:00-19:00",
  "rating": 4.7,
  "logoUrl": "https://res.cloudinary.com/.../logo.png",
  "bannerUrl": "https://res.cloudinary.com/.../banner.png",
  "active": true
}
```
* **📤 Response (HTTP 404 Not Found)**: si el `id` no existe.

#### 3.18 Listar Vehículos de un Concesionario
* **Método**: `GET` | **Ruta**: `/api/v1/dealerships/{id}/vehicles` | **Acceso**: Autenticado
* **💻 Uso en Frontend**: Página pública del concesionario (`/dealerships/:id`).
* **📥 Path Param**: `id` → `UUID`
* **📤 Response (HTTP 200 OK)** — lista (no paginada):
```json
[
  {
    "id": "3f1a2b4c-6d8e-4f0a-9b1c-2d3e4f5a6b7c",
    "userId": "101",
    "financialEntityId": "7a1b2c3d-4e5f-6a7b-8c9d-0e1f2a3b4c5d",
    "brand": "Toyota",
    "model": "Corolla Cross",
    "manufactureYear": 2025,
    "condition": "NEW",
    "priceAmount": 26990.00,
    "currency": "USD",
    "imagePath": "https://res.cloudinary.com/.../corolla.jpg",
    "status": "ACTIVE",
    "mileage": 0,
    "transmission": "AUTOMATIC",
    "engine": "2.0L",
    "traction": "FWD",
    "images": ["https://res.cloudinary.com/.../1.jpg"],
    "createdAt": "2026-09-20T10:00:00Z"
  }
]
```
* **📤 Response (HTTP 404 Not Found)**: si el concesionario no existe.

---

### 🎯 CRM del Concesionario (Gestión de Prospectos)

#### 3.19 Listar Mis Prospectos
* **Método**: `GET` | **Ruta**: `/api/v1/dealers/me/prospects` | **Acceso**: `ROLE_DEALER`, `ROLE_ADMIN`
* **💡 Descripción**: Bandeja de clientes interesados asignados al concesionario autenticado.
* **💻 Uso en Frontend**: Tabla del CRM en el Portal Concesionario.
* **📤 Response (HTTP 200 OK)**: lista de `ProspectResource`:
```json
[
  {
    "id": "f1a2b3c4-...",
    "dealerUserId": "101",
    "buyerUserId": "102",
    "fullName": "Maria Lopez",
    "email": "maria.lopez@example.com",
    "phone": "+51987654321",
    "interestedVehicleId": "3f1a2b4c-6d8e-4f0a-9b1c-2d3e4f5a6b7c",
    "status": "CONTACTED",
    "salesAgentId": "c1d2e3f4-5a6b-4c7d-8e9f-0a1b2c3d4e5f",
    "createdAt": "2026-10-01T09:00:00Z",
    "notes": []
  }
]
```

#### 3.20 Obtener Prospecto por ID
* **Método**: `GET` | **Ruta**: `/api/v1/dealers/me/prospects/{id}` | **Acceso**: `ROLE_DEALER`, `ROLE_ADMIN`
* **📥 Path Param**: `id` → `UUID`
* **📤 Response (HTTP 200 OK)**:
```json
{
  "id": "f1a2b3c4-5a6b-4c7d-8e9f-0a1b2c3d4e5f",
  "dealerUserId": "101",
  "buyerUserId": "102",
  "fullName": "Maria Lopez",
  "email": "maria.lopez@example.com",
  "phone": "+51987654321",
  "interestedVehicleId": "3f1a2b4c-6d8e-4f0a-9b1c-2d3e4f5a6b7c",
  "status": "CONTACTED",
  "salesAgentId": "c1d2e3f4-5a6b-4c7d-8e9f-0a1b2c3d4e5f",
  "createdAt": "2026-10-01T09:00:00Z",
  "notes": [
    {
      "id": "a1b2c3d4-5e6f-4a7b-8c9d-0e1f2a3b4c5d",
      "prospectId": "f1a2b3c4-5a6b-4c7d-8e9f-0a1b2c3d4e5f",
      "authorUserId": "101",
      "noteText": "Cliente solicita test drive el sábado.",
      "createdAt": "2026-10-04T10:00:00Z"
    }
  ]
}
```
* **📤 Response (HTTP 404 Not Found)**: si el prospecto no existe.

#### 3.21 Agregar Nota al Timeline del Prospecto
* **Método**: `POST` | **Ruta**: `/api/v1/prospects/{id}/notes` | **Acceso**: `ROLE_DEALER`, `ROLE_ADMIN`
* **📥 Path Param**: `id` → `UUID`
* **📥 Request Body**:
```json
{ "noteText": "Cliente solicita test drive el sábado." }
```
* **Validaciones**: `noteText` obligatorio (`@NotBlank`).
* **📤 Response (HTTP 201 Created)**:
```json
{
  "id": "a1b2c3d4-...",
  "prospectId": "f1a2b3c4-...",
  "authorUserId": "101",
  "noteText": "Cliente solicita test drive el sábado.",
  "createdAt": "2026-10-04T10:00:00Z"
}
```

#### 3.22 Obtener Timeline del Prospecto
* **Método**: `GET` | **Ruta**: `/api/v1/prospects/{id}/timeline` | **Acceso**: `ROLE_DEALER`, `ROLE_ADMIN`
* **📥 Path Param**: `id` → `UUID`
* **📤 Response (HTTP 200 OK)** — lista cronológica de notas (no paginada):
```json
[
  {
    "id": "a1b2c3d4-5e6f-4a7b-8c9d-0e1f2a3b4c5d",
    "prospectId": "f1a2b3c4-5a6b-4c7d-8e9f-0a1b2c3d4e5f",
    "authorUserId": "101",
    "noteText": "Primer contacto por WhatsApp.",
    "createdAt": "2026-10-01T09:15:00Z"
  },
  {
    "id": "b2c3d4e5-6f7a-4b8c-9d0e-1f2a3b4c5d6e",
    "prospectId": "f1a2b3c4-5a6b-4c7d-8e9f-0a1b2c3d4e5f",
    "authorUserId": "101",
    "noteText": "Cliente solicita test drive el sábado.",
    "createdAt": "2026-10-04T10:00:00Z"
  }
]
```
* **📤 Response (HTTP 404 Not Found)**: si el prospecto no existe.

#### 3.23 Cambiar Estado del Prospecto (Pipeline CRM)
* **Método**: `PATCH` | **Ruta**: `/api/v1/prospects/{id}/status` | **Acceso**: `ROLE_DEALER`, `ROLE_ADMIN`
* **📥 Request Body**:
```json
{ "status": "NEGOTIATING" }
```
* **Validaciones**: `status` → regex exacta `^(?i)(NEW|CONTACTED|TEST_DRIVE_SCHEDULED|NEGOTIATING|CLOSED_WON|CLOSED_LOST)$`.
* **📤 Response (HTTP 200 OK)**: objeto `ProspectResource` actualizado.

---

### 📊 Métricas del Concesionario

#### 3.24 Métricas de ROI y Conversión
* **Método**: `GET` | **Ruta**: `/api/v1/dealers/me/metrics` | **Acceso**: `ROLE_DEALER`, `ROLE_ADMIN`
* **💡 Descripción**: KPIs de membresía y conversión de leads del concesionario autenticado.
* **📤 Response (HTTP 200 OK)**:
```json
{
  "totalLeadsGenerated": 42,
  "conversionRate": 14.5,
  "totalVehicleViews": 450,
  "membershipRoi": "8.7x",
  "activeListingsCount": 12,
  "period": "LAST_30_DAYS"
}
```
* **⚠️ Nota de implementación**: `totalVehicleViews` y `membershipRoi` son **valores estimados/calculados en código** (no provienen de analítica real de eventos), y `period` siempre devuelve `"LAST_30_DAYS"`.

#### 3.25 Dashboard Analítico del Concesionario
* **Método**: `GET` | **Ruta**: `/api/v1/analytics/dealer` | **Acceso**: `ROLE_ADMIN`, o `ROLE_DEALER` (solo de su propia cuenta)
* **💡 Descripción**: Métricas agregadas de inventario, CRM, test drives y financiamiento.
* **📥 Query Params**:

| Param | Tipo | Default | Descripción |
| :--- | :--- | :--- | :--- |
| `dealerUserId` | String | *(vacío)* | Solo ADMIN puede consultar otro dealer |
| `period` | String | `ALL_TIME` | `LAST_7_DAYS`, `LAST_30_DAYS`, `ALL_TIME` (otro ⇒ `400`) |

* **📤 Response (HTTP 200 OK)**:
```json
{
  "dealerUserId": "101",
  "inventory": {
    "totalVehicles": 25,
    "availableVehicles": 18,
    "reservedVehicles": 4,
    "soldVehicles": 3,
    "totalInventoryValuePen": 0,
    "totalInventoryValueUsd": 674750.00
  },
  "crm": {
    "totalLeads": 42,
    "newLeads": 12,
    "contactedLeads": 10,
    "qualifiedLeads": 8,
    "inNegotiationLeads": 7,
    "closedWonLeads": 3,
    "closedLostLeads": 2,
    "conversionRate": 7.14
  },
  "testDrives": {
    "totalTestDrives": 15,
    "pendingTestDrives": 5,
    "confirmedTestDrives": 6,
    "completedTestDrives": 3,
    "cancelledTestDrives": 1
  },
  "financing": {
    "totalApplicationsReceived": 12,
    "pendingApplications": 4,
    "approvedApplications": 5,
    "rejectedApplications": 3
  },
  "period": "ALL_TIME"
}
```

---

### 💳 Facturación del Concesionario

> La **suscripción B2B** y la **descarga de facturas** son comunes a cualquier usuario autenticado: ver **2.52** (`POST /billing/subscriptions/checkout-session`), **2.49**, **2.53** y **2.54** (`GET /billing/invoices/{id}/pdf`).

---

---

## 🏦 4. Endpoints de Entidades Financieras (`ROLE_FINANCIAL_INSTITUTION`)

Requieren autenticación con rol `ROLE_FINANCIAL_INSTITUTION` (o `ROLE_ADMIN` donde se indique).

---

### 🏦 Gestión de la Entidad Financiera

#### 4.1 Registrar Entidad Financiera / Banco
* **Método**: `POST` | **Ruta**: `/api/v1/financial-entities` | **Acceso**: `ROLE_ADMIN`, `ROLE_FINANCIAL_INSTITUTION`
* **💡 Descripción**: Da de alta la entidad. Un ADMIN puede indicar `userId` para otro usuario; un `FINANCIAL_INSTITUTION` solo puede registrarse a sí mismo.
* **📥 Request Body**:
```json
{
  "userId": "101",
  "ruc": "20100047218",
  "name": "Banco de Crédito BCP",
  "logoUrl": "https://res.cloudinary.com/.../bcp.png",
  "bannerUrl": "https://res.cloudinary.com/.../bcp-banner.png"
}
```
* **Validaciones**: `ruc` → `^\d{11}$` (no obligatorio, pero si viene debe cumplir el patrón); `name` obligatorio (máx. 100); `logoUrl`/`bannerUrl` URLs opcionales (máx. 1000).
* **📤 Response (HTTP 201 Created)**: objeto `FinancialEntityResource` (ver 2.18).

#### 4.2 Obtener Mi Entidad Financiera
* **Método**: `GET` | **Ruta**: `/api/v1/financial-entities/me` | **Acceso**: `ROLE_FINANCIAL_INSTITUTION`, `ROLE_ADMIN`
* **📤 Response (HTTP 200 OK)**:
```json
{
  "id": "7a1b2c3d-4e5f-6a7b-8c9d-0e1f2a3b4c5d",
  "userId": "101",
  "ruc": "20100047218",
  "name": "Banco de Crédito BCP",
  "logoUrl": "https://res.cloudinary.com/.../bcp.png",
  "bannerUrl": "https://res.cloudinary.com/.../bcp-banner.png",
  "rateBenchmarks": [
    {
      "id": "b1c2d3e4-5f6a-4b7c-8d9e-0f1a2b3c4d5e",
      "rateType": "EFFECTIVE_ANNUAL",
      "annualRate": 11.50,
      "currency": "PEN",
      "sourceLabel": "BCP Tasas",
      "sourceUrl": "https://www.bcp.com.pe/tasas",
      "effectiveFrom": "2026-10-01"
    }
  ]
}
```
* **📤 Response (HTTP 404 Not Found)**: si la entidad no está asociada al usuario autenticado.

#### 4.3 Actualizar Entidad Financiera
* **Método**: `PUT` | **Ruta**: `/api/v1/financial-entities/{id}` | **Acceso**: `ROLE_ADMIN`, o `ROLE_FINANCIAL_INSTITUTION` propietario
* **📥 Request Body**: mismos campos de 4.1 (un `FINANCIAL_INSTITUTION` no puede transferir `userId`).
* **📤 Response (HTTP 200 OK)**: objeto `FinancialEntityResource`. `404` si no existe.

#### 4.4 Agregar Benchmark de Tasa de Interés
* **Método**: `POST` | **Ruta**: `/api/v1/financial-entities/{id}/rate-benchmarks` | **Acceso**: `ROLE_ADMIN`, o `ROLE_FINANCIAL_INSTITUTION` propietario
* **💡 Descripción**: Publica una tasa de referencia que usará el Frontend para ofertar créditos.
* **💻 Uso en Frontend**: Tabla "Mis Tasas" en el portal del banco.
* **📥 Request Body**:
```json
{
  "rateType": "EFFECTIVE_ANNUAL",
  "annualRate": 11.50,
  "currency": "PEN",
  "sourceLabel": "BCP Tasas Octubre",
  "sourceUrl": "https://www.bcp.com.pe/tasas",
  "effectiveFrom": "2026-10-01"
}
```
* **Validaciones**: `rateType` obligatorio; `annualRate` 0–100; `currency` 3 letras.
* **📤 Response (HTTP 201 Created)**: objeto `FinancialEntityResource` con el nuevo benchmark en `rateBenchmarks`.

#### 4.5 / 4.6 Subir Logo y Banner (Entidad Propia)
* **Método**: `POST` | **Rutas**: `/api/v1/financial-entities/me/logo` y `/api/v1/financial-entities/me/banner` | **Acceso**: `ROLE_FINANCIAL_INSTITUTION`, `ROLE_ADMIN`
* **📥 Request**: `multipart/form-data`, campo **`file`** (JPEG/PNG/WebP/GIF, máx. 10 MB).
* **📤 Response (HTTP 200 OK)**: objeto `FinancialEntityResource` con la URL actualizada.

#### 4.7 / 4.8 Subir Logo y Banner por ID
* **Método**: `POST` | **Rutas**: `/api/v1/financial-entities/{id}/logo` y `/api/v1/financial-entities/{id}/banner` | **Acceso**: `ROLE_ADMIN`, o `ROLE_FINANCIAL_INSTITUTION` propietario
* **📥 Path Param**: `id` → `UUID`
* **📥 Request**: `multipart/form-data`, campo **`file`** (mismas restricciones).
* **📤 Response (HTTP 200 OK)**: objeto `FinancialEntityResource`.

---

### 📋 Evaluación de Solicitudes de Crédito

#### 4.9 Aprobar / Rechazar Solicitud de Crédito
* **Método**: `PATCH` | **Ruta**: `/api/v1/credit-applications/{applicationId}/status` | **Acceso**: `ROLE_FINANCIAL_INSTITUTION`, `ROLE_ADMIN`
* **💡 Descripción**: Actualiza el estado del ciclo de evaluación bancaria.
* **💻 Uso en Frontend**: Bandeja de evaluación del banco (4.9 se usa junto a 2.27 para ver el detalle).
* **📥 Path Param**: `applicationId` → `UUID`
* **📥 Request Body**:
```json
{
  "status": "PRE_APPROVED",
  "notes": "Crédito pre-aprobado sujeto a verificación domiciliaria."
}
```
* **Validaciones**: `status` → regex exacta `^(?i)(PENDING|IN_REVIEW|PRE_APPROVED|REJECTED|DISBURSED)$`.
  > ⚠️ **No existe el estado `APPROVED`**: usar `PRE_APPROVED` o `DISBURSED`. Tampoco existen los campos `approvedAmount` ni `assignedEffectiveRate` (la tasa asignada se gestiona vía `rateBenchmarks`, 4.4).
* **📤 Response (HTTP 200 OK)**: objeto `CreditApplicationResource` actualizado. `404` si no existe.

> 📥 **Bandeja de solicitudes recibidas**: no existe un listado exclusivo por entidad. El flujo real es: `GET /api/v1/credit-applications/{id}` (4.9 admite este rol) o consultar el agregado `GET /api/v1/analytics/financial-institution` (4.10) para ver volúmenes.

---

### 📊 Métricas B2B

#### 4.10 Dashboard Analítico de la Entidad Financiera
* **Método**: `GET` | **Ruta**: `/api/v1/analytics/financial-institution` | **Acceso**: `ROLE_ADMIN`, o `ROLE_FINANCIAL_INSTITUTION` (solo de su propia entidad)
* **📥 Query Params**:

| Param | Tipo | Default | Descripción |
| :--- | :--- | :--- | :--- |
| `financialEntityId` | UUID | *(vacío)* | Si se omite, se resuelve por el usuario autenticado. Solo ADMIN puede consultar otra entidad. |

* **📤 Response (HTTP 200 OK)**:
```json
{
  "financialEntityId": "7a1b2c3d-4e5f-6a7b-8c9d-0e1f2a3b4c5d",
  "financialEntityName": "Banco de Crédito BCP",
  "totalApplicationsReceived": 120,
  "underReviewApplications": 30,
  "approvedApplications": 60,
  "rejectedApplications": 25,
  "disbursedApplications": 45,
  "approvalRate": 0.50,
  "totalRequestedVolumePen": 2400000.00,
  "totalDisbursedVolumePen": 900000.00,
  "averageTea": 11.90,
  "activeRateBenchmarksCount": 4,
  "totalSimulationsCount": 850
}
```

---

## 👔 5. Nota sobre los Asesores de Ventas (`ROLE_SALES_AGENT`)

**El rol `ROLE_SALES_AGENT` no existe en el backend.** El enum `Roles` solo contiene: `ROLE_USER`, `ROLE_ADMIN`, `ROLE_FINANCIAL_ANALYST`, `ROLE_DEALER`, `ROLE_FINANCIAL_INSTITUTION`.

Un *Asesor de Ventas* es un **registro** (`SalesAgent`) perteneciente a la cuenta de un concesionario, **no un usuario con sesión propia**. En la práctica:

| Funcionalidad que cumpliría un asesor | Endpoint real | Acceso |
| :--- | :--- | :--- |
| Listar mis prospectos asignados | `GET /api/v1/dealers/me/prospects` **(3.19)** | `ROLE_DEALER` |
| Ver detalle de un prospecto | `GET /api/v1/dealers/me/prospects/{id}` **(3.20)** | `ROLE_DEALER` |
| Registrar nota / seguimiento | `POST /api/v1/prospects/{id}/notes` **(3.21)** | `ROLE_DEALER` |
| Cambiar etapa del pipeline | `PATCH /api/v1/prospects/{id}/status` **(3.23)** | `ROLE_DEALER` |
| Agendar prueba de manejo | `POST /api/v1/test-drives` **(2.42)** | Autenticado |
| Gestionar asesores | `GET/POST/PUT /api/v1/dealers/me/sales-agents` **(3.8–3.11)** | `ROLE_DEALER` |

> Si en el futuro se necesita un rol de asesor con sesión propia, deberá agregarse `ROLE_SALES_AGENT` al enum `Roles` y a los `@PreAuthorize` correspondientes.

---

## ⚙️ 6. Endpoints de Administración (`ROLE_ADMIN`)

Requieren el rol `ROLE_ADMIN`. *(Los endpoints de los módulos anteriores que aceptan `ROLE_ADMIN` también están listados en su sección.)*

#### 6.1 Listar Todos los Usuarios del Sistema
* **Método**: `GET` | **Ruta**: `/api/v1/users` | **Acceso**: `ROLE_ADMIN`
* **📥 Query Params**: `page` (default 0), `size` (default 20, **máx. 50**), `sort`.
* **📤 Response (HTTP 200 OK)** — `Page<UserResource>` (ver *Formato de Respuestas Paginadas*; `size` por defecto 20):
```json
{
  "content": [
    { "id": 101, "username": "juan.perez@example.com", "roles": ["ROLE_USER"] },
    { "id": 102, "username": "admin@smartfinance.pe", "roles": ["ROLE_ADMIN"] },
    { "id": 103, "username": "bcp@bcp.com.pe", "roles": ["ROLE_FINANCIAL_INSTITUTION"] }
  ],
  "empty": false,
  "first": true,
  "last": false,
  "number": 0,
  "numberOfElements": 3,
  "pageable": {
    "offset": 0,
    "pageNumber": 0,
    "pageSize": 20,
    "paged": true,
    "sort": { "empty": true, "sorted": false, "unsorted": true },
    "unpaged": false
  },
  "size": 20,
  "sort": { "empty": true, "sorted": false, "unsorted": true },
  "totalElements": 150,
  "totalPages": 8
}
```

#### 6.2 Modificar Rol de Usuario
* **Método**: `PUT` | **Ruta**: `/api/v1/users/{userId}/roles` | **Acceso**: `ROLE_ADMIN`
* **📥 Path Param**: `userId` → `Long`
* **📥 Request Body**:
```json
{ "role": "ROLE_DEALER" }
```
* **Validaciones**: `role` obligatorio (`@NotNull` + `@NotBlank`); debe ser un valor del enum `Roles`, si no → error de dominio (`400`).
  > ⚠️ A diferencia de versiones anteriores de este documento, el body es **`role` (string)**, no `roles` (arreglo).
* **📤 Response (HTTP 200 OK)**: `UserResource` con los roles actualizados.

#### 6.3 Crear Plan de Suscripción
* **Método**: `POST` | **Ruta**: `/api/v1/billing/plans` | **Acceso**: `ROLE_ADMIN`
* **💡 Descripción**: Crea un nivel de plan (BASIC, PRO, ENTERPRISE) y su `stripePriceId`.
* **📥 Request Body**:
```json
{
  "name": "PRO",
  "description": "Para concesionarios en crecimiento",
  "price": 99.00,
  "currency": "USD",
  "billingCycle": "MONTHLY",
  "maxVehicleListings": 50,
  "maxSimulationsPerMonth": 500,
  "stripePriceId": "price_1AbCdEfGhIjKlMn"
}
```
* **Validaciones**: `name` 2–100 caracteres; `price` ≥ 0; `currency` 3 letras; `maxVehicleListings` ≥ 1; `maxSimulationsPerMonth` ≥ 1.
* **📤 Response (HTTP 201 Created)**: objeto `PlanResource`.

#### 6.4 Eliminar Entidad Financiera
* **Método**: `DELETE` | **Ruta**: `/api/v1/financial-entities/{id}` | **Acceso**: `ROLE_ADMIN`
* **📥 Path Param**: `id` → `UUID`
* **📤 Response (HTTP 204 No Content)**.

#### 6.5 Dashboard Consolidado de Analíticas
* **Método**: `GET` | **Ruta**: `/api/v1/analytics/admin` | **Acceso**: `ROLE_ADMIN`
* **💡 Descripción**: Métricas globales de toda la plataforma. *(Esta es la ruta real; no existe `/analytics/overview`.)*
* **📤 Response (HTTP 200 OK)**:
```json
{
  "totalDealerships": 24,
  "activeDealerships": 20,
  "totalFinancialEntities": 6,
  "totalRegisteredUsers": 1520,
  "totalVehiclesListed": 430,
  "totalCreditApplications": 310,
  "totalSimulationsRun": 2150,
  "totalActiveSubscriptions": 18,
  "estimatedMonthlyRecurringRevenueUsd": 1782.00
}
```

---

## 🔧 7. Endpoints Técnicos / Sistema

#### 7.1 Webhook de Stripe
* **Método**: `POST` | **Ruta**: `/api/v1/billing/webhooks/stripe` | **Acceso**: Técnico (firma Stripe, no JWT)
* **💡 Descripción**: Recibe eventos asíncronos de Stripe. La autenticación se valida con el header `Stripe-Signature` contra `${stripe.webhook-secret}`.
* **💻 Uso en Frontend**: **Nunca se llama desde el Frontend.** Se configura en el dashboard de Stripe (endpoint del servidor).
* **📥 Headers**: `Stripe-Signature` (obligatorio para la verificación).
* **📥 Request Body**: payload JSON crudo de Stripe (no es un DTO de la plataforma).
* **Eventos procesados**: `checkout.session.completed`, `customer.subscription.deleted`, `invoice.payment_failed` (el resto solo se registra en log).
* **📤 Response**: texto plano.

| Escenario | Respuesta |
| :--- | :--- |
| Secret no configurado | `400 "Webhook secret is not configured"` |
| Header `Stripe-Signature` ausente | `400 "Missing Stripe-Signature header"` |
| Firma inválida | `400 "Invalid signature"` |
| Evento recibido correctamente | `200 "Event received"` |

---

## 📎 Anexo A. Tabla Resumen de los 109 Endpoints

| # | Método | Ruta | Acceso principal |
| :-- | :--- | :--- | :--- |
| 1 | POST | `/api/v1/auth/registrations` | Público |
| 2 | POST | `/api/v1/auth/sessions` | Público |
| 3 | POST | `/api/v1/auth/tokens` | Público |
| 4 | DELETE | `/api/v1/auth/sessions/current` | Público |
| 5 | POST | `/api/v1/auth/password-recoveries` | Público |
| 6 | POST | `/api/v1/auth/password-resets` | Público |
| 7 | POST | `/api/v1/auth/google` | Público |
| 8 | POST | `/api/v1/auth/email-verification/send` | Público |
| 9 | POST | `/api/v1/auth/email-verification/verify` | Público |
| 10 | POST | `/api/v1/auth/phone-verification` y `/api/v1/auth/phone-verification/firebase` | Público |
| 11 | GET | `/api/v1/vehicles` | Público |
| 12 | GET | `/api/v1/vehicles/brands` | Público |
| 13 | GET | `/api/v1/vehicles/{vehicleId}` | Público |
| 14 | GET | `/api/v1/vehicles/my-listings` | Autenticado |
| 15 | GET | `/api/v1/vehicles/users/{userId}` | Self / ADMIN |
| 16 | POST | `/api/v1/vehicles` | DEALER / ADMIN |
| 17 | PUT | `/api/v1/vehicles/{vehicleId}` | Propietario |
| 18 | PATCH | `/api/v1/vehicles/{vehicleId}/status` | Propietario |
| 19 | DELETE | `/api/v1/vehicles/{vehicleId}` | Propietario |
| 20 | POST | `/api/v1/vehicles/{vehicleId}/image` | Propietario |
| 21 | POST | `/api/v1/vehicles/{vehicleId}/images` | Propietario |
| 22 | DELETE | `/api/v1/vehicles/{vehicleId}/images/{imageIndex}` | Propietario |
| 23 | POST | `/api/v1/profiles` | Autenticado |
| 24 | GET | `/api/v1/profiles/{profileId}` | Propietario / ADMIN |
| 25 | GET | `/api/v1/profiles/users/{userId}` | Self / ADMIN |
| 26 | PUT | `/api/v1/profiles/{profileId}` | Propietario |
| 27 | DELETE | `/api/v1/profiles/{profileId}` | Propietario |
| 28 | GET | `/api/v1/profiles/reniec/dni/{dni}` | Autenticado |
| 29 | GET | `/api/v1/users/{userId}` | Self / ADMIN |
| 30 | POST | `/api/v1/users/{userId}/dealer-role-requests` | Self / ADMIN |
| 31 | POST | `/api/v1/users/{userId}/financial-institution-role-requests` | Self / ADMIN |
| 32 | POST | `/api/v1/users/me/corporate-verification/initiate` | Autenticado |
| 33 | POST | `/api/v1/users/me/corporate-verification/confirm` | Autenticado |
| 34 | POST | `/api/v1/users/{userId}/corporate-verification/initiate` | Self / ADMIN |
| 35 | POST | `/api/v1/users/{userId}/corporate-verification/confirm` | Self / ADMIN |
| 36 | GET | `/api/v1/users` | ADMIN |
| 37 | PUT | `/api/v1/users/{userId}/roles` | ADMIN |
| 38 | GET | `/api/v1/partners/sunat/ruc/{ruc}` | Autenticado |
| 39 | GET | `/api/v1/partners/corporate-verification/lookup/{ruc}` | Autenticado |
| 40 | GET | `/api/v1/dealerships` | Autenticado |
| 41 | GET | `/api/v1/dealerships/{id}` | Autenticado |
| 42 | GET | `/api/v1/dealerships/{id}/vehicles` | Autenticado |
| 43 | GET | `/api/v1/dealerships/me` | DEALER / ADMIN |
| 44 | PUT | `/api/v1/dealerships/me` | DEALER / ADMIN |
| 45 | POST | `/api/v1/dealerships/me/logo` | DEALER / ADMIN |
| 46 | POST | `/api/v1/dealerships/me/banner` | DEALER / ADMIN |
| 47 | GET | `/api/v1/dealers/me/sales-agents` | DEALER / ADMIN |
| 48 | POST | `/api/v1/dealers/me/sales-agents` | DEALER / ADMIN |
| 49 | PUT | `/api/v1/dealers/me/sales-agents/{id}` | DEALER / ADMIN |
| 50 | POST | `/api/v1/dealers/me/sales-agents/{id}/reassign-leads` | DEALER / ADMIN |
| 51 | GET | `/api/v1/dealers/me/metrics` | DEALER / ADMIN |
| 52 | GET | `/api/v1/financial-entities` | USER / ADMIN / ANALYST / FI / DEALER |
| 53 | GET | `/api/v1/financial-entities/{id}` | USER / ADMIN / ANALYST / FI / DEALER |
| 54 | POST | `/api/v1/financial-entities` | ADMIN / FI |
| 55 | GET | `/api/v1/financial-entities/me` | FI / ADMIN |
| 56 | PUT | `/api/v1/financial-entities/{id}` | ADMIN / FI propietario |
| 57 | DELETE | `/api/v1/financial-entities/{id}` | ADMIN |
| 58 | POST | `/api/v1/financial-entities/{id}/rate-benchmarks` | ADMIN / FI propietario |
| 59 | POST | `/api/v1/financial-entities/me/logo` | FI / ADMIN |
| 60 | POST | `/api/v1/financial-entities/me/banner` | FI / ADMIN |
| 61 | POST | `/api/v1/financial-entities/{id}/logo` | ADMIN / FI propietario |
| 62 | POST | `/api/v1/financial-entities/{id}/banner` | ADMIN / FI propietario |
| 63 | POST | `/api/v1/simulations` | Autenticado |
| 64 | GET | `/api/v1/simulations` | Autenticado |
| 65 | GET | `/api/v1/simulations/{id}` | Propietario / ADMIN |
| 66 | POST | `/api/v1/simulations/{id}/apply` | Autenticado |
| 67 | DELETE | `/api/v1/simulations/{id}` | Propietario |
| 68 | POST | `/api/v1/credit-applications` | USER / ADMIN |
| 69 | GET | `/api/v1/credit-applications/me` | USER / ADMIN |
| 70 | GET | `/api/v1/credit-applications/{id}` | USER / FI / ADMIN / DEALER |
| 71 | PATCH | `/api/v1/credit-applications/{id}/status` | FI / ADMIN |
| 72 | POST | `/api/v1/credit-scores` | Autenticado |
| 73 | GET | `/api/v1/credit-scores` | Autenticado |
| 74 | GET | `/api/v1/credit-scores/{id}` | Propietario / ADMIN |
| 75 | GET | `/api/v1/credit-scores/profile/{profileId}` | Propietario / ADMIN |
| 76 | DELETE | `/api/v1/credit-scores/{id}` | Propietario / ADMIN |
| 77 | POST | `/api/v1/depreciation-projections` | Autenticado |
| 78 | GET | `/api/v1/depreciation-projections` | Autenticado |
| 79 | GET | `/api/v1/depreciation-projections/{id}` | Propietario / ADMIN |
| 80 | GET | `/api/v1/depreciation-projections/vehicle/{vehicleId}` | Propietario / ADMIN |
| 81 | DELETE | `/api/v1/depreciation-projections/{id}` | Propietario / ADMIN |
| 82 | POST | `/api/v1/consultations` y `/api/v1/consultations/chat` y `/api/v1/ai/consultations` y `/api/v1/ai/consultations/chat` | Autenticado |
| 83 | GET | `/api/v1/consultations/history` y `/api/v1/ai/consultations/history` | Autenticado |
| 84 | GET | `/api/v1/consultations/recommendations` y `/api/v1/ai/consultations/recommendations` | Autenticado |
| 85 | POST | `/api/v1/prospects` y `/api/v1/dealers/me/prospects` | Autenticado |
| 86 | GET | `/api/v1/dealers/me/prospects` | DEALER / ADMIN |
| 87 | GET | `/api/v1/dealers/me/prospects/{id}` | DEALER / ADMIN |
| 88 | POST | `/api/v1/prospects/{id}/notes` | DEALER / ADMIN |
| 89 | GET | `/api/v1/prospects/{id}/timeline` | DEALER / ADMIN |
| 90 | PATCH | `/api/v1/prospects/{id}/status` | DEALER / ADMIN |
| 91 | POST | `/api/v1/test-drives` | Autenticado |
| 92 | GET | `/api/v1/test-drives/me` | Autenticado |
| 93 | GET | `/api/v1/test-drives/{id}` | Autenticado |
| 94 | PATCH | `/api/v1/test-drives/{id}/status` | Autenticado |
| 95 | DELETE | `/api/v1/test-drives/{id}` | Autenticado |
| 96 | GET | `/api/v1/billing/plans` | Autenticado |
| 97 | GET | `/api/v1/billing/plans/{planId}` | Autenticado |
| 98 | POST | `/api/v1/billing/plans` | ADMIN |
| 99 | GET | `/api/v1/billing/subscriptions/me` | Autenticado |
| 100 | POST | `/api/v1/billing/subscriptions` | Autenticado |
| 101 | DELETE | `/api/v1/billing/subscriptions/{subscriptionId}` | Autenticado |
| 102 | POST | `/api/v1/billing/subscriptions/checkout-session` | Autenticado |
| 103 | GET | `/api/v1/billing/invoices/me` | Autenticado |
| 104 | GET | `/api/v1/billing/invoices/{invoiceId}/pdf` | Autenticado |
| 105 | PATCH | `/api/v1/billing/invoices/{invoiceId}` | Autenticado |
| 106 | POST | `/api/v1/billing/webhooks/stripe` | Firma Stripe |
| 107 | GET | `/api/v1/analytics/dealer` | DEALER / ADMIN |
| 108 | GET | `/api/v1/analytics/financial-institution` | FI / ADMIN |
| 109 | GET | `/api/v1/analytics/admin` | ADMIN |

> Las rutas marcadas con `[+ /alias]` admiten varias URLs equivalentes (documentadas en su sección).

---

## 📎 Anexo B. Cambios clave respecto a versiones anteriores de esta guía

| Antes (incorrecto) | Ahora (real) |
| :--- | :--- |
| `/api/v1/catalog/vehicles` | **`/api/v1/vehicles`** |
| `/api/v1/financing/simulations` | **`/api/v1/simulations`** |
| `/api/v1/financing/credit-applications` | **`/api/v1/credit-applications`** |
| `PUT /credit-applications/{id}/status` | **`PATCH`** `/credit-applications/{id}/status` |
| `/api/v1/scoring/me` | **`GET /api/v1/credit-scores`** |
| `/api/v1/projections/depreciations` | **`/api/v1/depreciation-projections`** |
| `/api/v1/consultations/gemini/chat` | **`POST /api/v1/consultations/chat`** |
| `GET/PUT /api/v1/profiles/me` | **`GET/PUT /api/v1/profiles/{profileId}`** o **`GET /profiles/users/{userId}`** |
| `/api/v1/profiles/reniec-dni/{dni}` | **`/api/v1/profiles/reniec/dni/{dni}`** |
| `/api/v1/partners/sunat-ruc/{ruc}` | **`/api/v1/partners/sunat/ruc/{ruc}`** |
| `/api/v1/billing/dealer-metrics/me` | **`/api/v1/dealers/me/metrics`** |
| `/api/v1/partners/financial-entities` | **`/api/v1/financial-entities`** |
| `GET /credit-applications/financial-entity/me` | **No existe** → usar `GET /credit-applications/{id}` + `GET /analytics/financial-institution` |
| `GET /api/v1/crm/leads/me` | **No existe** → usar **`GET /api/v1/dealers/me/prospects`** |
| `POST /crm/leads/{leadId}/test-drives` | **`POST /api/v1/test-drives`** |
| `POST /partners/corporate-verifications/{id}/approve` | **No existe** → flujo `users/.../corporate-verification/initiate|confirm` |
| `GET /api/v1/analytics/overview` | **`GET /api/v1/analytics/admin`** |
| Rol `ROLE_SALES_AGENT` | **No existe** en el enum `Roles` |
| RENIEC y SUNAT "Público" | **Requieren JWT** (`isAuthenticated`) |
| Simulador "Público" | **Requiere JWT**; solo `GET /vehicles` es público |



