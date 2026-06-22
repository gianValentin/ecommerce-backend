# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Comandos esenciales

### Compilar y ejecutar

```bash
# Ejecutar con perfil H2 (por defecto, sin base de datos externa)
./mvnw spring-boot:run -Ph2

# Ejecutar con perfil PostgreSQL
./mvnw spring-boot:run -Ppostgresql

# Compilar sin tests
./mvnw clean package -DskipTests -Ph2

# Compilar con Docker (usa H2 internamente)
docker build -t ecommerce-backend .
docker-compose up
```

### Tests

```bash
# Ejecutar todos los tests
./mvnw test

# Ejecutar un test específico
./mvnw test -Dtest=UserServiceTest

# Ejecutar un método de test específico
./mvnw test -Dtest=UserServiceTest#saveUserCaseSuccess
```

## Perfiles de base de datos

El proyecto tiene dos perfiles Maven que controlan la base de datos:

| Perfil | Base de datos | Cuándo usarlo |
|--------|--------------|---------------|
| `h2` (default) | H2 en memoria (modo PostgreSQL) | Desarrollo local / Docker |
| `postgresql` | PostgreSQL real | Producción |

El perfil activo se inyecta en `application.properties` via `@activatedProperties@` y activa el archivo `application-{profile}.properties` correspondiente.

Con H2 activo, la consola web está disponible en `http://localhost:8080/my-console`.

Variables de entorno para PostgreSQL: `PROD_DB_HOST`, `PROD_DB_PORT`, `PROD_DB_NAME`, `PROD_DB_USERNAME`, `PROD_DB_PASSWORD`.

## Arquitectura del proyecto

### Paquete principal: `com.app.core`

La aplicación sigue una arquitectura en capas estándar de Spring Boot:

```
controller/       → REST controllers (CartController, CategoryController, OrderController, ProductController, UserController)
service/          → Interfaces de servicio
service/impl/     → Implementaciones Default* (DefaultCartService, DefaultProductService, etc.)
repository/       → Spring Data JPA repositories
entity/model/     → Entidades JPA
entity/dto/       → DTOs organizados por dominio (cart/, category/, product/, user/, etc.)
exception/        → ControllerAdvice + excepciones custom (CJNotFoundException, CECartException)
config/           → Configuración de Jackson, ModelMapper, OpenAPI, validación
utils/            → Utilidades (ObjectMapperUtils, Constant, CustomCodeException)
```

### Seguridad (`com.app.core.security`)

La seguridad está separada en su propio subpaquete:

- **`SecurityUser`** extiende `UserModel` e implementa `UserDetails` de Spring Security.
- **`SecurityConfig`** tiene dos `SecurityFilterChain` beans separados, uno por perfil (`@Profile("h2")` y `@Profile("postgresql")`), porque el perfil H2 requiere `MvcRequestMatcher` para que el H2 console sea accesible.
- Los JWT se gestionan con `JwtAuthenticationFilter` + `DefaultJwtService`. Los tokens se persisten en la tabla `Token` y se revocan en cada nuevo login.
- **Roles**: `ANONYMOUS`, `USER`, `ADMIN`, `MANAGER`. Los endpoints de escritura de productos, categorías y usuarios requieren `ADMIN`.
- El endpoint `/api/v1/cart/**` es público (sin autenticación).
- CORS se configura vía la propiedad `security.allowedOrigins` (env var `PROD_ALLOWED_ORIGINS`, default `*`).

### Modelo de dominio

- **`UserModel`** (`_user` table, UUID id) — usuario de negocio con `Role`.
- **`SecurityUser`** extiende `UserModel` — añade la lista de `Token` para Spring Security.
- **`ProductModel`** — tiene `CategoryModel` (ManyToOne), `PriceModel` (OneToOne), y `Set<ImageModel>` (OneToMany).
- **`CartModel`** (UUID id, tipo `CType.CART`) — pertenece a un `UserModel` y contiene `Set<EntryModel>`.
- Los carritos anónimos usan usuarios con `Role.ANONYMOUS`. Al autenticarse, el carrito anónimo se fusiona automáticamente con el carrito del usuario en sesión (`DefaultCartService.mergeCart`).

### Convenciones de código

- Los DTOs de request usan prefijo `Save*`, `Update*`, `Request*`; los de respuesta usan `Response*`.
- Las excepciones de negocio heredan de `RuntimeException` y llevan un código de `CustomCodeException`.
- `ObjectMapperUtils` provee mapeo estático de entidades a DTOs via ModelMapper con estrategia `STRICT`.
- La validación de request bodies usa Bean Validation (`@Valid`). Los errores se centralizan en `ControllerAdvice`.
- Para listas en el body del POST (ej. productos), se usa el wrapper `ValidList<T>` para que `@Valid` funcione sobre cada elemento.

### API y documentación

- Base URL: `http://localhost:8080/api/v1/`
- Swagger UI disponible en: `http://localhost:8080/swagger-ui/index.html`
- Los controllers están anotados con `@Tag` y `@Operation` de SpringDoc OpenAPI.