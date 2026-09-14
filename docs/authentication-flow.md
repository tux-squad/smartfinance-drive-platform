# Guía de Flujo de Autenticación, Gestión de Usuarios y Elevación de Roles

Este documento describe de manera detallada la arquitectura de autenticación, la gestión de usuarios, el control de acceso basado en roles (RBAC) y los mecanismos de elevación automática de privilegios en **SmartFinance Drive Platform**.

---

## 1. Arquitectura General de Seguridad (IAM)

La plataforma utiliza una arquitectura de seguridad basada en **JSON Web Tokens (JWT)** y **Spring Security**, complementada con verificación externa en tiempo real a través de la **SUNAT** para validar la identidad de concesionarios y entidades bancarias.

### Principios Fundamentales
1. **Asignación Mínima de Privilegios**: Todo usuario nuevo registrado a través del endpoint público recibe de forma estricta y exclusiva el rol `ROLE_USER`.
2. **Sin Elevación Pública Directa**: No es posible registrarse directamente como `ROLE_ADMIN`, `ROLE_DEALER` o `ROLE_FINANCIAL_INSTITUTION` en el formulario inicial de registro.
3. **Elevación Automatizada basada en SUNAT**: Los usuarios pueden solicitar la elevación de su cuenta a Concesionario (`ROLE_DEALER`) o Entidad Financiera (`ROLE_FINANCIAL_INSTITUTION`). El sistema valida inmediatamente con la SUNAT si el RUC ingresado existe, está en estado **ACTIVO**, condición **HABIDO** y posee la actividad económica (CIIU) correspondiente.
4. **Control Jerárquico de Administrador**: Los administradores (`ROLE_ADMIN`) poseen la facultad de modificar manualmente el rol de cualquier usuario registrado.

---

## 2. Definición de Roles en la Plataforma

| Rol | Código en Sistema | Descripción y Alcance | Método de Obtención |
| :--- | :--- | :--- | :--- |
| **Cliente / Usuario Base** | `ROLE_USER` | Usuario regular. Puede explorar el catálogo de vehículos, realizar simulaciones de crédito, consultar su scoring crediticio y ver proyecciones de depreciación. | Registro público inicial (`POST /api/v1/auth/registrations`). |
| **Concesionario / Dealer** | `ROLE_DEALER` | Representante de una sucursal o automotriz. Puede publicar vehículos en el catálogo, actualizar precios, stock y gestionar su inventario. | Registro como `ROLE_USER` + Solicitud de Rol Dealer con RUC Automotriz (CIIU `451...`). |
| **Banca / Entidad Financiera** | `ROLE_FINANCIAL_INSTITUTION` | Entidad bancaria o de crédito. Puede gestionar tasas de interés, configurar productos financieros y evaluar solicitudes de financiamiento. | Registro como `ROLE_USER` + Solicitud de Rol Financiero con RUC Financiero (CIIU `64...` o `66...`). |
| **Analista Financiero** | `ROLE_FINANCIAL_ANALYST` | Perfil de auditoría y análisis de riesgo dentro de la plataforma. | Asignación manual por un `ROLE_ADMIN`. |
| **Administrador** | `ROLE_ADMIN` | Control total del sistema, gestión global de usuarios, vehículos, entidades y configuraciones. | Asignación manual por la base de datos o por otro `ROLE_ADMIN`. |

---

## 3. Flujo Paso a Paso de Autenticación y Elevación

### Paso 1: Registro Inicial de Usuario
Cualquier persona puede registrarse en la plataforma proporcionando sus credenciales básicas (correo electrónico y contraseña).

* **Endpoint**: `POST /api/v1/auth/registrations`
* **Lógica Interna**:
  * Se encripta la contraseña utilizando `BCryptPasswordEncoder`.
  * Se asigna automáticamente la lista `[ROLE_USER]`.
  * Se persiste la cuenta de usuario en el Bounded Context de IAM.

#### Ejemplo de Solicitud (Request Body)
```json
{
  "username": "juan.perez@concesionario.com",
  "password": "Password123!"
}
```

#### Ejemplo de Respuesta (HTTP 201 Created)
```json
{
  "id": 105,
  "username": "juan.perez@concesionario.com",
  "roles": [
    "ROLE_USER"
  ]
}
```

---

### Paso 2: Autenticación e Inicio de Sesión
El usuario ingresa sus credenciales para obtener las fichas de acceso firmadas.

* **Endpoint**: `POST /api/v1/auth/sessions`
* **Lógica Interna**:
  * Valida las credenciales contra la base de datos.
  * Genera un `token` de acceso JWT de corta duración firmado con clave secreta HMAC-SHA.
  * Genera un `refreshToken` (UUID) para renovaciones futuras.

#### Ejemplo de Solicitud (Request Body)
```json
{
  "username": "juan.perez@concesionario.com",
  "password": "Password123!"
}
```

#### Ejemplo de Respuesta (HTTP 200 OK)
```json
{
  "id": 105,
  "username": "juan.perez@concesionario.com",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "a1b2c3d4-5678-90ab-cdef-1234567890ab"
}
```

El cliente debe almacenar el `token` e incluirlo en todas las solicitudes posteriores dentro del encabezado HTTP:
```http
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

---

### Paso 3: Renovación de Token Expire (Refresh Token)
Cuando el `token` JWT expira, el cliente puede solicitar un nuevo token sin requerir que el usuario vuelva a ingresar su contraseña.

* **Endpoint**: `POST /api/v1/auth/tokens`

#### Ejemplo de Solicitud (Request Body)
```json
{
  "refreshToken": "a1b2c3d4-5678-90ab-cdef-1234567890ab"
}
```

#### Ejemplo de Respuesta (HTTP 200 OK)
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.new_token...",
  "refreshToken": "a1b2c3d4-5678-90ab-cdef-1234567890ab"
}
```

---

### Paso 4: Elevación de Rol mediante Validación Automatizada SUNAT

Si el usuario registrado requiere operar como **Concesionario (Dealer)** o **Entidad Financiera (Banca)**, no necesita solicitar una aprobación manual a un administrador. Puede solicitar la elevación de su cuenta de forma automática mediante la integración con la SUNAT.

#### Opción A: Elevación a Concesionario (`ROLE_DEALER`)
* **Endpoint**: `POST /api/v1/users/{userId}/dealer-role-requests`
* **Acceso**: El propio usuario (`userId`) o un `ROLE_ADMIN`.
* **Proceso de Validación**:
  1. El sistema realiza una llamada al servicio externo de consulta de RUC de la SUNAT.
  2. Verifica que el RUC exista en la base de datos oficial.
  3. Comprueba que el estado sea **ACTIVO** y la condición del contribuyente sea **HABIDO**.
  4. Valida el código de actividad económica **CIIU**: debe comenzar con `451` (Venta de vehículos automotores).
  5. Si todas las validaciones son exitosas, el sistema agrega automáticamente el rol `ROLE_DEALER` a la lista de roles del usuario y guarda los cambios.

##### Ejemplo de Solicitud (Request Body)
```json
{
  "ruc": "20123456789"
}
```

##### Ejemplo de Respuesta (HTTP 200 OK)
```json
{
  "id": 105,
  "username": "juan.perez@concesionario.com",
  "roles": [
    "ROLE_USER",
    "ROLE_DEALER"
  ]
}
```

---

#### Opción B: Elevación a Entidad Financiera (`ROLE_FINANCIAL_INSTITUTION`)
* **Endpoint**: `POST /api/v1/users/{userId}/financial-institution-role-requests`
* **Acceso**: El propio usuario (`userId`) o un `ROLE_ADMIN`.
* **Proceso de Validación**:
  1. El servicio consulta los datos del RUC ingresado en la SUNAT.
  2. Confirma que el estado sea **ACTIVO** y la condición sea **HABIDO**.
  3. Valida el código de actividad económica **CIIU**: debe comenzar con `64` (Intermediación financiera) o `66` (Actividades auxiliares a la intermediación financiera).
  4. Si cumple las condiciones, añade de forma automática el rol `ROLE_FINANCIAL_INSTITUTION` a la cuenta del usuario.

##### Ejemplo de Solicitud (Request Body)
```json
{
  "ruc": "20987654321"
}
```

##### Ejemplo de Respuesta (HTTP 200 OK)
```json
{
  "id": 108,
  "username": "contacto@banco-ejemplo.pe",
  "roles": [
    "ROLE_USER",
    "ROLE_FINANCIAL_INSTITUTION"
  ]
}
```

---

### Paso 5: Modificación Manual de Roles por Administrador
En casos especiales donde se requiera asignar perfiles como `ROLE_FINANCIAL_ANALYST` o `ROLE_ADMIN`, o remover roles a un usuario, un administrador puede actualizar directamente el rol asignado.

* **Endpoint**: `PUT /api/v1/users/{userId}/roles`
* **Acceso**: Exclusivo para `ROLE_ADMIN`.

#### Ejemplo de Solicitud (Request Body)
```json
{
  "role": "ROLE_FINANCIAL_ANALYST"
}
```

#### Ejemplo de Respuesta (HTTP 200 OK)
```json
{
  "id": 105,
  "username": "juan.perez@concesionario.com",
  "roles": [
    "ROLE_USER",
    "ROLE_FINANCIAL_ANALYST"
  ]
}
```

---

## 4. Diagrama de Secuencia del Flujo Completo

```mermaid
sequenceDiagram
    autonumber
    actor U as Usuario / Cliente
    participant API as REST Controller (IAM)
    participant SVC as UserCommandService
    participant SUNAT as Servidor SUNAT (Partners)
    participant DB as Base de Datos PostgreSQL

    Note over U, DB: 1. Registro Público Inicial
    U->>API: POST /api/v1/auth/registrations (email, password)
    API->>SVC: SignUpCommand (roles = [ROLE_USER])
    SVC->>DB: Persistir nuevo Usuario con ROLE_USER
    DB-->>API: Usuario creado (id = 105)
    API-->>U: HTTP 201 (id: 105, roles: ["ROLE_USER"])

    Note over U, DB: 2. Inicio de Sesión y Token JWT
    U->>API: POST /api/v1/auth/sessions (email, password)
    API->>SVC: AuthenticateUserCommand
    SVC-->>API: Credenciales Válidas + JWT Token + Refresh Token
    API-->>U: HTTP 200 (token: "eyJhbG...", refreshToken: "a1b2...")

    Note over U, DB: 3. Solicitud de Elevación a Concesionario (Dealer)
    U->>API: POST /api/v1/users/105/dealer-role-requests (ruc: "20123456789")
    API->>SVC: RequestDealerRoleCommand(105, "20123456789")
    SVC->>SUNAT: verifyRuc("20123456789")
    SUNAT-->>SVC: SunatRucInfo (Estado: ACTIVO, Condición: HABIDO, CIIU: 45100)
    
    alt RUC Válido, Activo, Habido y CIIU Automotriz
        SVC->>DB: user.addRole(ROLE_DEALER) & save()
        DB-->>API: Usuario Actualizado
        API-->>U: HTTP 200 (roles: ["ROLE_USER", "ROLE_DEALER"])
    else RUC Inválido, Inactivo o CIIU no corresponde
        SVC-->>API: DomainValidationException("RUC no cumple condiciones")
        API-->>U: HTTP 400 Bad Request
    end
```

---

## 5. Matriz de Permisos por Endpoints

| Bounded Context | Operación / Endpoint | ROLE_USER | ROLE_DEALER | ROLE_FINANCIAL_INSTITUTION | ROLE_ADMIN |
| :--- | :--- | :---: | :---: | :---: | :---: |
| **Catalog** | Consultar Vehículos (`GET /api/v1/vehicles`) | SI | SI | SI | SI |
| **Catalog** | Crear / Modificar Vehículos (`POST/PUT /api/v1/vehicles`) | NO | SI | NO | SI |
| **Financing** | Realizar Simulación (`POST /api/v1/financing/simulations`) | SI (Propietario) | NO | NO | SI |
| **Financing** | Listar Simulaciones por Entidad | NO | NO | SI | SI |
| **Partners** | Gestionar Entidades Financieras (`POST/PUT /api/v1/financial-entities`) | NO | NO | SI | SI |
| **Partners** | Consulta de RUC SUNAT (`GET /api/v1/sunat/ruc/{ruc}`) | SI | SI | SI | SI |
| **Billing** | Suscribirse a Plan Premium (`POST /api/v1/billing/subscriptions`) | SI | SI | SI | SI |
| **IAM** | Modificar Roles de Usuario (`PUT /api/v1/users/{id}/roles`) | NO | NO | NO | SI |
