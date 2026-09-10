# Hoja de Ruta (Roadmap) y Estado del Proyecto

Este documento registra el progreso del desarrollo de la plataforma **SmartFinance Drive Platform**, indicando los componentes implementados y los pendientes según la arquitectura DDD y Limpia establecida.

---

## Estado de Bounded Contexts y Funcionalidades

### 1. Cimientos y Configuración Inicial `[COMPLETADO]`
- [x] Estructura inicial del repositorio y configuración de Gitflow (`main` y `develop`).
- [x] Integración de códigos de referencia en control de versiones.
- [x] Configuración de dependencias de Maven y exclusión temporal del autoconfigurador de base de datos para compilación limpia (`SmartfinanceDrivePlatformApplicationTests` `@Disabled`).

---

### 2. Bounded Context: `shared` `[COMPLETADO]`
- [x] **Agregado Base:** `AbstractDomainAggregateRoot` (Soporte para publicación de eventos de dominio).
- [x] **Value Objects Financieros:**
  - `Money` (Manejo inmutable de montos monetarios precisos con divisa y redondeo HALF_UP).
  - `Percent` (Manejo inmutable de tasas con escala de 6 decimales).
- [x] **Excepciones de Negocio:** `DomainValidationException` para validación de invariantes de dominio.
- [x] **Persistencia Compartida:** `AuditableAbstractPersistenceEntity` para auditoría JPA automática (`id`, `created_at`, `updated_at`).
- [x] **Pruebas Unitarias:** Cobertura de tests unitarios para `Money` y `Percent`.

---

### 3. Bounded Context: `catalog` `[COMPLETADO]`
- [x] **Dominio:**
  - Agregado raíz `Vehicle` y Value Objects (`VehicleId`, `UserId`, `FinancialEntityId`).
  - Puerto del Repositorio `VehicleRepository`.
  - Comandos y Consultas (`CreateVehicleCommand`, `UpdateVehicleCommand`, `DeleteVehicleCommand`, `GetVehicleByIdQuery`, `GetVehiclesByUserIdQuery`).
- [x] **Aplicación:**
  - Interfaces e Implementaciones de servicios CQRS (`VehicleCommandServiceImpl`, `VehicleQueryServiceImpl`).
- [x] **Infraestructura:**
  - Entidad JPA `VehiclePersistenceEntity` y Repositorio Spring Data `SpringDataVehicleRepository`.
  - Mapeadores y Adaptador de Persistencia (`VehiclePersistenceAssembler`, `VehicleRepositoryAdapter`).
- [x] **Interfaces (REST API):**
  - DTO Resources (`CreateVehicleResource`, `UpdateVehicleResource`, `VehicleResource`).
  - Controlador REST `VehiclesController` (`/api/v1/vehicles`) con soporte CRUD.
- [x] **Pruebas Unitarias:**
  - Tests de negocio en `VehicleTest`.
  - Tests mockeados del adaptador de persistencia en `VehicleRepositoryAdapterTest`.
  - Tests mockeados del controlador en `VehiclesControllerTest`.

---

### 4. Bounded Context: `profiles` (Perfiles de Usuarios) `[COMPLETADO]`
- [x] **Dominio:** Agregado `Profile`, Value Objects (`ProfileId`, `UserId`), comandos y consultas.
- [x] **Aplicación:** Servicios CQRS de lectura y escritura (`ProfileCommandServiceImpl`, `ProfileQueryServiceImpl`).
- [x] **Infraestructura:** Entidad JPA `ProfilePersistenceEntity` y adaptadores de repositorio.
- [x] **Interfaces REST:** Controller `ProfilesController` (`/api/v1/profiles`), DTOs y transformadores.
- [x] **Pruebas Unitarias:** Cobertura de tests para agregado, adaptador y controlador.

---

### 5. Bounded Context: `partners` (Concesionarios y Entidades Financieras) `[COMPLETADO]`
- [x] **Dominio:** Agregado `FinancialEntity`, entidad `RateBenchmark`, Value Objects (`FinancialEntityId`, `RateBenchmarkId`), comandos y consultas.
- [x] **Aplicación:** Servicios CQRS de lectura y escritura (`FinancialEntityCommandServiceImpl`, `FinancialEntityQueryServiceImpl`).
- [x] **Infraestructura:** Entidades JPA `FinancialEntityPersistenceEntity` y `RateBenchmarkPersistenceEntity`, adaptadores y Spring Data.
- [x] **Interfaces REST:** Controller `FinancialEntitiesController` (`/api/v1/financial-entities`), DTOs y transformadores.
- [x] **Pruebas Unitarias:** Cobertura de tests para agregado, adaptador y controlador.

---

### 6. Bounded Context: `financing` (Motor Financiero SBS) `[PENDIENTE]`
- [ ] Solicitud de crédito y simulación.
- [ ] **Motor Financiero:** Implementación de fórmulas matemáticas SBS:
  - Método francés vencido ordinario con amortizaciones exactas por días calendario.
  - Periodos de gracia total y parcial.
  - Cuotas dobles (Julio y Diciembre) y Cuota Balón (devengando intereses).
  - Cálculo de la TCEA mediante Solver TIR (Bisección).

---

### 7. Bounded Context: `scoring` `[PENDIENTE]`
- [ ] Reglas de evaluación de riesgos del perfil solicitante frente a la simulación financiera.

---

### 8. Bounded Context: `projections` `[PENDIENTE]`
- [ ] Reportes consolidados y estadísticas financieras para bancos y concesionarios.

---

### 9. Bounded Context: `iam` (Seguridad y Autenticación) `[PENDIENTE]`
- [ ] **Dominio:**
  - Agregado raíz `User` y entidad `Role`.
  - Repositorios de dominio `UserRepository` y `RoleRepository`.
- [ ] **Aplicación:**
  - Casos de uso de registro (Sign Up), inicio de sesión (Sign In) y recuperación de contraseña.
- [ ] **Infraestructura (Seguridad):**
  - Implementación de hashing de contraseñas con BCrypt.
  - Implementación de JWT Token Services (Generación y validación de **Access Token** y **Refresh Token**).
  - Configuración del pipeline de Spring Security (Filtro JWT, CORS, autorización de rutas).
- [ ] **Interfaces REST:**
  - `AuthenticationController` y DTOs asociados.
