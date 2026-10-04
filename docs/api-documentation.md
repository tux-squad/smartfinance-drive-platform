# Guía Completa de la API REST por Roles - SmartFinance Drive Platform

Documentación técnica y exhaustiva de todos los endpoints de **SmartFinance Drive Platform**, estructurada por **Niveles de Acceso y Roles de Usuario** para facilitar la integración directa en aplicaciones Frontend (React, Angular, Vue, Next.js, etc.).

---

## 📌 Información General & Servidor

* **Servidor en Producción (Render)**: `https://smartfinance-drive-platform.onrender.com`
* **Swagger UI (OpenAPI 3.0)**: [https://smartfinance-drive-platform.onrender.com/swagger-ui/index.html](https://smartfinance-drive-platform.onrender.com/swagger-ui/index.html)
* **OpenAPI Specs (JSON)**: `https://smartfinance-drive-platform.onrender.com/v3/api-docs`

---

## 🔒 Cabeceras de Autenticación y Matriz de Permisos

Para cualquier endpoint que requiera autenticación, el Frontend debe incluir el token JWT en la cabecera HTTP:

```http
Authorization: Bearer <access_token_jwt>
Content-Type: application/json
```

### Tabla Resumen de Roles de Usuario

| Rol en Backend | Descripción | Ámbito de Acción en Frontend |
| :--- | :--- | :--- |
| **Público** | Visitante no autenticado | Registro, Login, Catálogo, Simulador Libre, Verificaciones SUNAT/RENIEC |
| **`ROLE_USER`** | Cliente Final / Comprador | Solicitar créditos, Perfil, Score crediticio, Depreciación, Chat IA Gemini |
| **`ROLE_DEALER`** | Concesionario de Vehículos | Gestor de Inventario, Asesores de Ventas, CRM, Suscripción Stripe |
| **`ROLE_FINANCIAL_INSTITUTION`** | Banco / Entidad Financiera | Evaluar solicitudes de crédito, definir tasas de interés, métricas B2B |
| **`ROLE_SALES_AGENT`** | Asesor de Ventas | Atender prospectos asignados, agendar pruebas de manejo en CRM |
| **`ROLE_ADMIN`** | Administrador del Sistema | Control total de usuarios, roles, aprobación B2B RUC y métricas |

---

## 🌐 1. Endpoints Públicos (Sin Autenticación)

Estos endpoints no requieren token JWT en la cabecera `Authorization`.

---

### 1.1 Registrar Nuevo Usuario
* **Método**: `POST` | **Ruta**: `/api/v1/auth/registrations` | **Acceso**: `Público`
* **💡 Descripción**: Crea una cuenta inicial de cliente final (`ROLE_USER`).
* **💻 Uso en Frontend**: Formulario de Sign Up. Al recibir HTTP 201, redirigir al formulario de Login o enviar automáticamente la sesión.
* **📥 Request Body**:
```json
{
  "username": "juan.perez@example.com",
  "password": "Password123!",
  "roles": ["ROLE_USER"]
}
```
* **📤 Response (HTTP 201 Created)**:
```json
{
  "id": 101,
  "username": "juan.perez@example.com",
  "roles": ["ROLE_USER"]
}
```

---

### 1.2 Iniciar Sesión (Obtener JWT)
* **Método**: `POST` | **Ruta**: `/api/v1/auth/sessions` | **Acceso**: `Público`
* **💡 Descripción**: Valida las credenciales del usuario y genera un `token` de acceso (expira en 15 min) y un `refreshToken` (expira en 7 días).
* **💻 Uso en Frontend**: Formulario de Login. Guardar `token` y `refreshToken` en memoria/secure storage o cookies `HttpOnly`. Redirigir según el rol retornado en el payload del JWT.
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
  "refreshToken": "d7b8c9a0-1234-5678-9abc-def012345678"
}
```

---

### 1.3 Renovar Token de Acceso (Refresh Token)
* **Método**: `POST` | **Ruta**: `/api/v1/auth/tokens` | **Acceso**: `Público`
* **💡 Descripción**: Renueva un access token expirado sin obligar al usuario a reingresar su contraseña.
* **💻 Uso en Frontend**: Ejecutar automáticamente mediante un **Axios Interceptor** cuando una petición retorne `401 Unauthorized`.
* **📥 Request Body**:
```json
{
  "refreshToken": "d7b8c9a0-1234-5678-9abc-def012345678"
}
```
* **📤 Response (HTTP 200 OK)**:
```json
{
  "id": 101,
  "username": "juan.perez@example.com",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "d7b8c9a0-1234-5678-9abc-def012345678"
}
```

---

### 1.4 Enviar Código OTP de Verificación de Correo (Brevo REST API + EmailVerify.io)
* **Método**: `POST` | **Ruta**: `/api/v1/auth/email-verification/send` | **Acceso**: `Público`
* **💡 Descripción**: Valida la existencia real del buzón mediante EmailVerify.io (puerto 443) y envía un código OTP de 6 dígitos al correo vía Brevo REST API.
* **💻 Uso en Frontend**: Botón "Verificar Correo". Deshabilitar el botón por 60 segundos (cooldown) tras hacer clic.
* **📥 Request Body**:
```json
{
  "email": "juan.perez@example.com"
}
```
* **📤 Response (HTTP 200 OK)**:
```json
{
  "email": "juan.perez@example.com",
  "maskedEmail": "j***z@example.com",
  "sessionActive": true,
  "expiresInSeconds": 600,
  "message": "Código de verificación enviado exitosamente a tu correo electrónico."
}
```

---

### 1.5 Confirmar Código OTP de Correo
* **Método**: `POST` | **Ruta**: `/api/v1/auth/email-verification/verify` | **Acceso**: `Público`
* **💡 Descripción**: Comprueba el código de 6 dígitos ingresado por el usuario y genera un `verificationToken` de confirmación.
* **💻 Uso en Frontend**: Input de 6 dígitos. Al recibir HTTP 200, marcar el campo de email como verificado en el estado de la vista.
* **📥 Request Body**:
```json
{
  "email": "juan.perez@example.com",
  "code": "849201"
}
```
* **📤 Response (HTTP 200 OK)**:
```json
{
  "verified": true,
  "email": "juan.perez@example.com",
  "status": "VERIFIED",
  "verifiedAt": "2026-10-04T15:30:00Z",
  "verificationToken": "evt_8f9a0b1c2d3e4f5a6b7c8d9e",
  "message": "Correo electrónico verificado exitosamente."
}
```

---

### 1.6 Autenticación con Google OAuth2
* **Método**: `POST` | **Ruta**: `/api/v1/auth/google` | **Acceso**: `Público`
* **💡 Descripción**: Permite inicio de sesión o registro con Google Identity Services recibiendo el `idToken` emitido por Google.
* **💻 Uso en Frontend**: Componente "Sign in with Google". Al completar el popup de Google, enviar el `credential` retornado a esta ruta.
* **📥 Request Body**:
```json
{
  "idToken": "eyJhbGciOiJSUzI1NiIsImtpZCI6..."
}
```

---

### 1.7 Catálogo Público de Vehículos (Paginado y Filtrado)
* **Método**: `GET` | **Ruta**: `/api/v1/catalog/vehicles?brand=Toyota&minPrice=15000&maxPrice=35000&page=0&size=12` | **Acceso**: `Público`
* **💡 Descripción**: Lista el inventario vehicular activo con filtros por marca, modelo, precio, año y tipo de combustible.
* **💻 Uso en Frontend**: Landing Page o Buscador del Catálogo con tarjetas de vehículos.
* **📤 Response (HTTP 200 OK)**:
```json
{
  "content": [
    {
      "id": 1,
      "brand": "Toyota",
      "model": "Corolla Cross",
      "year": 2025,
      "price": 26990.00,
      "currency": "USD",
      "imageUrl": "https://res.cloudinary.com/demo/image/upload/v1/vehicles/corolla.jpg",
      "dealershipId": 10
    }
  ],
  "page": 0,
  "size": 12,
  "totalElements": 45
}
```

---

### 1.8 Obtener Detalle de Vehículo por ID
* **Método**: `GET` | **Ruta**: `/api/v1/catalog/vehicles/{vehicleId}` | **Acceso**: `Público`
* **💡 Descripción**: Devuelve la ficha técnica completa de un automóvil específico.
* **💻 Uso en Frontend**: Vista `/catalog/vehicles/:id`. Muestra especificaciones, fotos en alta resolución y botón "Simular Crédito".

---

### 1.9 Simulación Financiera Libre (Calculadora de Cuotas)
* **Método**: `POST` | **Ruta**: `/api/v1/financing/simulations` | **Acceso**: `Público`
* **💡 Descripción**: Calcula el cronograma de pagos, cuota mensual, TEA, TEM y desembolso inicial para una simulación vehicular sin requerir login.
* **💻 Uso en Frontend**: Calculadora Interactiva de Crédito Vehicular en la web.
* **📥 Request Body**:
```json
{
  "vehiclePrice": 25000.00,
  "downPaymentPercent": 20.0,
  "tenureMonths": 36,
  "effectiveAnnualRate": 12.5
}
```
* **📤 Response (HTTP 200 OK)**:
```json
{
  "loanAmount": 20000.00,
  "monthlyInstallment": 668.50,
  "totalInterest": 4066.00,
  "effectiveMonthlyRate": 0.98,
  "schedule": [
    { "period": 1, "installment": 668.50, "principal": 505.10, "interest": 163.40, "remainingBalance": 19494.90 }
  ]
}
```

---

### 1.10 Consulta RENIEC por DNI
* **Método**: `GET` | **Ruta**: `/api/v1/profiles/reniec-dni/{dni}` | **Acceso**: `Público`
* **💡 Descripción**: Valida el número de DNI peruano (8 dígitos) consultando la base de datos oficial de RENIEC via Factiliza.
* **💻 Uso en Frontend**: Input DNI en formularios. Al escribir 8 dígitos, autocompletar Nombres y Apellidos en el formulario.
* **📤 Response (HTTP 200 OK)**:
```json
{
  "dni": "72849102",
  "nombres": "Juan Carlos",
  "apellidoPaterno": "Perez",
  "apellidoMaterno": "Gomez",
  "nombreCompleto": "Juan Carlos Perez Gomez"
}
```

---

### 1.11 Consulta SUNAT por RUC
* **Método**: `GET` | **Ruta**: `/api/v1/partners/sunat-ruc/{ruc}` | **Acceso**: `Público`
* **💡 Descripción**: Valida un RUC peruano (11 dígitos) ante la SUNAT consultando Razón Social, Estado (HABIDO) y Condición.
* **💻 Uso en Frontend**: Formulario de Registro B2B (Concesionarios o Bancos). Autocompleta Razón Social y Dirección Fiscal.
* **📤 Response (HTTP 200 OK)**:
```json
{
  "ruc": "20601234567",
  "razonSocial": "AUTOLAND PERU S.A.C.",
  "estado": "ACTIVO",
  "condicion": "HABIDO",
  "direccion": "AV. JAVIER PRADO ESTE 410, SAN ISIDRO"
}
```

---

## 👤 2. Endpoints del Cliente Final (`ROLE_USER`)

Requieren autenticación con rol `ROLE_USER` en la cabecera HTTP (`Authorization: Bearer <token>`).

---

### 2.1 Obtener Perfil de Cliente
* **Método**: `GET` | **Ruta**: `/api/v1/profiles/me` | **Acceso**: `ROLE_USER`
* **💡 Descripción**: Retorna los datos personales, financieros e historial del cliente autenticado.
* **💻 Uso en Frontend**: Módulo "Mi Cuenta / Mi Perfil".
* **📤 Response (HTTP 200 OK)**:
```json
{
  "id": 42,
  "userId": 101,
  "firstName": "Juan",
  "lastName": "Perez",
  "dni": "72849102",
  "phone": "+51987654321",
  "monthlyIncome": 4500.00
}
```

---

### 2.2 Crear / Actualizar Perfil de Cliente
* **Método**: `PUT` | **Ruta**: `/api/v1/profiles/me` | **Acceso**: `ROLE_USER`
* **💡 Descripción**: Guarda o actualiza los datos personales e ingresos mensuales del usuario.
* **📥 Request Body**:
```json
{
  "firstName": "Juan",
  "lastName": "Perez",
  "dni": "72849102",
  "phone": "+51987654321",
  "monthlyIncome": 4800.00
}
```

---

### 2.3 Crear Solicitud de Crédito Vehicular
* **Método**: `POST` | **Ruta**: `/api/v1/financing/credit-applications` | **Acceso**: `ROLE_USER`
* **💡 Descripción**: Genera una solicitud formal de financiamiento para un vehículo del catálogo dirigida a una entidad bancaria.
* **💻 Uso en Frontend**: Botón "Solicitar Financiamiento" en la ficha del auto.
* **📥 Request Body**:
```json
{
  "vehicleId": 1,
  "financialEntityId": 5,
  "requestedLoanAmount": 20000.00,
  "downPaymentAmount": 5000.00,
  "tenureMonths": 36
}
```
* **📤 Response (HTTP 201 Created)**:
```json
{
  "id": 301,
  "applicationCode": "APP-2026-98741",
  "status": "PENDING_EVALUATION",
  "createdAt": "2026-10-04T16:00:00Z"
}
```

---

### 2.4 Listar Mis Solicitudes de Crédito
* **Método**: `GET` | **Ruta**: `/api/v1/financing/credit-applications/me` | **Acceso**: `ROLE_USER`
* **💡 Descripción**: Muestra el historial y estado en tiempo real (En Evaluación, Aprobada, Rechazada) de las solicitudes del cliente.
* **💻 Uso en Frontend**: Pantalla "Mis Solicitudes".

---

### 2.5 Consultar Scoring Crediticio Personal
* **Método**: `GET` | **Ruta**: `/api/v1/scoring/me` | **Acceso**: `ROLE_USER`
* **💡 Descripción**: Devuelve la calificación crediticia calculada por el motor de scoring (0 a 1000 puntos y categoría RIESGO).
* **💻 Uso en Frontend**: Dashboard del Cliente con velocímetro/gauge de Score Crediticio.

---

### 2.6 Calcular Depreciación Vehicular Proyectada
* **Método**: `POST` | **Ruta**: `/api/v1/projections/depreciations` | **Acceso**: `ROLE_USER`
* **💡 Descripción**: Proyecta la pérdida de valor comercial de un vehículo a 1, 3 y 5 años basada en kilometraje y categoría.
* **💻 Uso en Frontend**: Gráfico de Depreciación en la vista del vehículo.

---

### 2.7 Consulta al Asesor Financiero IA Gemini (Chatbot Dual Path)
* **Método**: `POST` | **Ruta**: `/api/v1/consultations/gemini/chat` | **Acceso**: `ROLE_USER`
* **💡 Descripción**: Envía preguntas financieras/vehiculares al agente de Inteligencia Artificial Gemini.
* **💻 Uso en Frontend**: Widget de Chat Flotante "Asesor IA".
* **📥 Request Body**:
```json
{
  "prompt": "¿Me conviene financiar un auto de $25,000 con mi sueldo de S/ 4,500?",
  "vehiclePrice": 25000.00
}
```
* **📤 Response (HTTP 200 OK)**:
```json
{
  "advice": "Basado en tu nivel de ingresos y el costo del vehículo...",
  "recommendedDownPayment": 7500.00,
  "maxRecommendedMonthlyInstallment": 1350.00
}
```

---

### 2.8 Solicitar Vinculación RUC para Rol Concesionario o Banco
* **Método**: `POST` | **Ruta**: `/api/v1/users/{userId}/dealer-role-requests` | **Acceso**: `ROLE_USER` (Mismo usuario)
* **💡 Descripción**: Solicita la elevación de cuenta a `ROLE_DEALER` o `ROLE_FINANCIAL_INSTITUTION` enviando un RUC 20 válido.
* **📥 Request Body**:
```json
{
  "ruc": "20601234567",
  "companyName": "AUTOLAND PERU S.A.C."
}
```

---

## 🚗 3. Endpoints de Concesionarios (`ROLE_DEALER`)

Requieren autenticación con rol `ROLE_DEALER`.

---

### 3.1 Registrar Vehículo en Inventario
* **Método**: `POST` | **Ruta**: `/api/v1/catalog/vehicles` | **Acceso**: `ROLE_DEALER`, `ROLE_ADMIN`
* **💡 Descripción**: Publica un nuevo vehículo en el catálogo del concesionario.
* **💻 Uso en Frontend**: Formulario de Publicación de Vehículos en Portal Concesionario.
* **📥 Request Body**:
```json
{
  "brand": "Toyota",
  "model": "RAV4 Hybrid",
  "year": 2026,
  "price": 34990.00,
  "currency": "USD",
  "transmission": "AUTOMATIC",
  "fuelType": "HYBRID",
  "imageUrl": "https://res.cloudinary.com/demo/image/upload/v1/rav4.jpg"
}
```

---

### 3.2 Actualizar Vehículo de Inventario
* **Método**: `PUT` | **Ruta**: `/api/v1/catalog/vehicles/{vehicleId}` | **Acceso**: `ROLE_DEALER`, `ROLE_ADMIN`

---

### 3.3 Eliminar / Dar de Baja Vehículo
* **Método**: `DELETE` | **Ruta**: `/api/v1/catalog/vehicles/{vehicleId}` | **Acceso**: `ROLE_DEALER`, `ROLE_ADMIN`

---

### 3.4 Crear Asesor de Ventas
* **Método**: `POST` | **Ruta**: `/api/v1/dealers/me/sales-agents` | **Acceso**: `ROLE_DEALER`
* **💡 Descripción**: Registra un nuevo agente de ventas asociado a la tienda del concesionario.
* **📥 Request Body**:
```json
{
  "fullName": "Carlos Mendoza",
  "email": "carlos.mendoza@autoland.pe",
  "phone": "+51987654321"
}
```

---

### 3.5 Listar Asesores de Ventas del Concesionario
* **Método**: `GET` | **Ruta**: `/api/v1/dealers/me/sales-agents` | **Acceso**: `ROLE_DEALER`

---

### 3.6 Crear Sesión de Checkout Stripe (Suscripción B2B)
* **Método**: `POST` | **Ruta**: `/api/v1/billing/subscriptions/checkout-session` | **Acceso**: `ROLE_DEALER`
* **💡 Descripción**: Inicia el proceso de pago con Stripe para adquirir un plan de suscripción comercial (BASIC, PRO, ENTERPRISE).
* **💻 Uso en Frontend**: Al elegir un Plan, invocar este endpoint y redirigir la ventana a `checkoutUrl`.
* **📥 Request Body**:
```json
{
  "planId": "plan_pro_annual"
}
```
* **📤 Response (HTTP 200 OK)**:
```json
{
  "sessionId": "cs_test_a1b2c3d4...",
  "checkoutUrl": "https://checkout.stripe.com/c/pay/cs_test_a1b2c3d4..."
}
```

---

### 3.7 Descargar Factura PDF de Suscripción
* **Método**: `GET` | **Ruta**: `/api/v1/billing/invoices/{invoiceId}/pdf` | **Acceso**: `ROLE_DEALER`
* **💡 Descripción**: Retorna el archivo PDF oficial de la factura de pago.
* **💻 Uso en Frontend**: Botón "Descargar Factura PDF" (Abre en nueva pestaña o descarga binaria).

---

### 3.8 Dashboard Métricas ROI del Concesionario
* **Método**: `GET` | **Ruta**: `/api/v1/billing/dealer-metrics/me` | **Acceso**: `ROLE_DEALER`
* **💡 Descripción**: Retorna estadísticas de prospectos recibidos, créditos aprobados y retorno de inversión.

---

## 🏦 4. Endpoints de Entidades Financieras (`ROLE_FINANCIAL_INSTITUTION`)

Requieren autenticación con rol `ROLE_FINANCIAL_INSTITUTION`.

---

### 4.1 Registrar Entidad Financiera / Banco
* **Método**: `POST` | **Ruta**: `/api/v1/partners/financial-entities` | **Acceso**: `ROLE_FINANCIAL_INSTITUTION`, `ROLE_ADMIN`
* **📥 Request Body**:
```json
{
  "name": "Banco de Crédito BCP",
  "code": "BCP_PERU",
  "ruc": "20100047218",
  "baseRate": 11.5
}
```

---

### 4.2 Listar Solicitudes Bancarias Recibidas
* **Método**: `GET` | **Ruta**: `/api/v1/financing/credit-applications/financial-entity/me` | **Acceso**: `ROLE_FINANCIAL_INSTITUTION`
* **💡 Descripción**: Bandeja de solicitudes de crédito ingresadas por los clientes hacia el banco.

---

### 4.3 Aprobar / Rechazar Solicitud de Crédito
* **Método**: `PUT` | **Ruta**: `/api/v1/financing/credit-applications/{applicationId}/status` | **Acceso**: `ROLE_FINANCIAL_INSTITUTION`
* **📥 Request Body**:
```json
{
  "status": "APPROVED",
  "approvedAmount": 20000.00,
  "assignedEffectiveRate": 11.99,
  "remarks": "Crédito aprobado sujeto a verificación domiciliaria."
}
```

---

## 👔 5. Endpoints de Asesores de Ventas (`ROLE_SALES_AGENT`)

Requieren autenticación con rol `ROLE_SALES_AGENT`.

---

### 5.1 Listar Mis Prospectos Asignados (CRM)
* **Método**: `GET` | **Ruta**: `/api/v1/crm/leads/me` | **Acceso**: `ROLE_SALES_AGENT`
* **💡 Descripción**: Listado de clientes interesados asignados al agente comercial.

---

### 5.2 Agendar Prueba de Manejo (Test Drive)
* **Método**: `POST` | **Ruta**: `/api/v1/crm/leads/{leadId}/test-drives` | **Acceso**: `ROLE_SALES_AGENT`, `ROLE_DEALER`
* **📥 Request Body**:
```json
{
  "vehicleId": 1,
  "scheduledAt": "2026-10-10T10:00:00Z",
  "notes": "Cliente prefiere probar el vehículo en autopista."
}
```

---

## ⚙️ 6. Endpoints de Administración (`ROLE_ADMIN`)

Requieren autenticación con rol `ROLE_ADMIN`.

---

### 6.1 Listar Todos los Usuarios del Sistema
* **Método**: `GET` | **Ruta**: `/api/v1/users?page=0&size=20` | **Acceso**: `ROLE_ADMIN`

---

### 6.2 Modificar Roles de Usuario
* **Método**: `PUT` | **Ruta**: `/api/v1/users/{userId}/roles` | **Acceso**: `ROLE_ADMIN`
* **📥 Request Body**:
```json
{
  "roles": ["ROLE_USER", "ROLE_DEALER"]
}
```

---

### 6.3 Aprobar Solicitud B2B RUC
* **Método**: `POST` | **Ruta**: `/api/v1/partners/corporate-verifications/{requestId}/approve` | **Acceso**: `ROLE_ADMIN`

---

### 6.4 Dashboard Consolidado de Analíticas
* **Método**: `GET` | **Ruta**: `/api/v1/analytics/overview` | **Acceso**: `ROLE_ADMIN`
* **💡 Descripción**: Retorna el total de usuarios, concesionarios, créditos otorgados y volumen operado en soles/dólares.
