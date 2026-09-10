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

### 6. Bounded Context: `financing` (Motor Financiero SBS) `[COMPLETADO]`
- [x] **Dominio:** Agregado raíz `Simulation`, entidad `PaymentPeriod`, Value Objects (`SimulationId`, `PaymentPeriodId`, `GracePeriodType`, `VehicleInsuranceType`), comandos y consultas.
- [x] **Motor Financiero (`FinancingPlanBuilder`):** Implementación de fórmulas matemáticas SBS:
  - Método francés vencido ordinario con amortizaciones por base diaria exacta ($360$ días).
  - Periodos de gracia total (capitaliza intereses) y parcial (intereses y seguros).
  - Seguros de desgravamen y vehicular (`ENDOSADO`, `MENSUAL`, `FINANCIADO`).
  - Cuota Balón ("Compra Inteligente") descontada a valor presente.
  - Cálculo exacto de indicadores **TIR** (Newton-Raphson), **TCEA** y **VAN**.
- [x] **Aplicación:** Servicios CQRS de lectura y escritura (`SimulationCommandServiceImpl`, `SimulationQueryServiceImpl`).
- [x] **Infraestructura:** Entidades JPA `SimulationPersistenceEntity` y `PaymentPeriodPersistenceEntity`, assemblers y adaptadores de repositorio.
- [x] **Interfaces REST:** Controller `SimulationsController` (`/api/v1/simulations`), DTOs y transformadores.
- [x] **Pruebas Unitarias:** Cobertura de tests para motor financiero, agregado, adaptador y controlador REST.

---

### 7. Bounded Context: `scoring` (Credit Scoring & Risk Assessment) `[COMPLETADO]`
- [x] **Dominio:** Agregado raíz `CreditScore`, Value Objects (`ScoreId`, `RiskTier`, `ScoringStatus`), comandos y consultas.
- [x] **Motor de Scoring (`CreditScoringEngine`):** Reglas de evaluación de riesgo basadas en el Ratio de Cobertura de Deuda (DTI - Debt-to-Income):
  - Tier A ($\text{DTI} \le 30\%$): Estado `APPROVED`, descuento TEA $-1.5\%$.
  - Tier B ($30\% < \text{DTI} \le 45\%$): Estado `CONDITIONALLY_APPROVED`, TEA estándar.
  - Tier C ($\text{DTI} > 45\%$): Estado `REJECTED`, recargo por riesgo $+2.5\%$.
- [x] **Aplicación:** Servicios CQRS de lectura y escritura (`CreditScoreCommandServiceImpl`, `CreditScoreQueryServiceImpl`).
- [x] **Infraestructura:** Entidad JPA `CreditScorePersistenceEntity`, assemblers y `CreditScoreRepositoryAdapter`.
- [x] **Interfaces REST:** Controller `CreditScoresController` (`/api/v1/credit-scores`), DTOs y transformadores.
- [x] **Pruebas Unitarias:** Cobertura de tests para motor de scoring, agregado, adaptador JPA y controlador REST.

---

### 8. Bounded Context: `projections` (Depreciation & Valuation Projections) `[COMPLETADO]`
- [x] **Dominio:** Agregado raíz `DepreciationProjection`, Value Objects (`ProjectionId`, `MotorizationType`, `RecommendedAction`), comandos y consultas.
- [x] **Motor de Depreciación (`DepreciationCalculator`):** Curva de estimación de valor comercial a 2, 3 y 5 años según motorización:
  - `COMBUSTION`: Depreciación Año 1 = $18\%$, Años posteriores = $10\%/\text{año}$.
  - `ECOLOGICO`: Depreciación Año 1 = $12\%$, Años posteriores = $7\%/\text{año}$.
  - Matriz de asesoramiento inteligente al vencimiento vs Cuota Balón (`TRADE_IN`, `KEEP_AND_PAY`, `RETURN_VEHICLE`).
- [x] **Aplicación:** Servicios CQRS de lectura y escritura (`DepreciationProjectionCommandServiceImpl`, `DepreciationProjectionQueryServiceImpl`).
- [x] **Infraestructura:** Entidad JPA `DepreciationProjectionPersistenceEntity`, assemblers y `DepreciationProjectionRepositoryAdapter`.
- [x] **Interfaces REST:** Controller `DepreciationProjectionsController` (`/api/v1/depreciation-projections`), DTOs y transformadores.
- [x] **Pruebas Unitarias:** Cobertura de tests para calculador de depreciación, agregado, adaptador JPA y controlador REST.

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
