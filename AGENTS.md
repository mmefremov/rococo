# AGENTS.md

Guidance for Codex working in this repo.

## Build & Run Commands

```bash
# Build all modules
./gradlew build

# Build a specific module
./gradlew :rococo-artist:build

# Run tests for all modules
./gradlew test

# Run tests for a specific module
./gradlew :rococo-artist:test

# Run a single test class
./gradlew :rococo-artist:test --tests "io.efremov.rococo.service.ArtistServiceTest"

# Run a single test method
./gradlew :rococo-artist:test --tests "io.efremov.rococo.service.ArtistServiceTest.findById_existingId_returnsArtist"

# Generate gRPC stubs from proto files (required before first build)
./gradlew :rococo-grpc-common:generateProto

# Start infrastructure (PostgreSQL + Kafka)
docker compose --profile env up -d

# Free ports used by running services
bash free-ports.sh   # kills PIDs on 9000, 8092, 8094, 8096, 8098, 8100, 8080, 3000
```

Start services individually (each in own terminal):

```bash
./gradlew :rococo-auth:bootRun
./gradlew :rococo-gateway:bootRun
./gradlew :rococo-geo:bootRun
./gradlew :rococo-artist:bootRun
./gradlew :rococo-museum:bootRun
./gradlew :rococo-painting:bootRun
./gradlew :rococo-userdata:bootRun
```

## Architecture Overview

### Service Map

```
rococo-client (SvelteKit, port 3000)
       │ REST
rococo-gateway (port 8080)  ← validates JWT from rococo-auth (port 9000)
       │
       ├─ gRPC 8092 → rococo-userdata  ← Kafka topic "user-registered" ← rococo-auth
       ├─ gRPC 8094 → rococo-geo       (country data, in-memory cached)
       ├─ gRPC 8096 → rococo-artist
       ├─ gRPC 8098 → rococo-museum
       └─ gRPC 8100 → rococo-painting
```

### Module Responsibilities

- **rococo-grpc-common** — pure lib (no Spring Boot); all `.proto` files + generated Java stubs. All gRPC services depend on it. Build
  first.
- **rococo-auth** — OAuth2 Authorization Server. Handles login/registration UI (Thymeleaf), issues JWTs, publishes `user-registered` Kafka
  events.
- **rococo-gateway** — OAuth2 Resource Server. No DB. Routes REST → backend via gRPC. Assembles composite responses (e.g., enriches painting
  with artist/museum via parallel `CompletableFuture` calls).
- **rococo-userdata** — stores user profiles (first/last name, avatar). Created lazily via Kafka; updated via gRPC.
- **rococo-geo** — country ref data (~250 countries, Russian). Cached via `ConcurrentMapCacheManager` — never changes at runtime.
- **rococo-artist / rococo-museum / rococo-painting** — domain CRUD services. Each owns own DB. No cross-service FK constraints (integrity
  enforced at app level).

### Key Cross-Service Patterns

**Data enrichment in gateway** — domain gRPC services return only own-DB data; gateway resolves references:

- `MuseumResponse` from `rococo-museum` is flat: `{id, title, description, photo, city, country_id}`. Gateway calls geo in a single batch to
  fill country name.
- `PaintingResponse` from `rococo-painting` is flat: `{id, title, description, content, artist_id, optional museum_id}`. Gateway fetches
  artists and museums **in parallel** via `CompletableFuture.supplyAsync(..., enrichmentExecutor)`.
- No domain service makes cross-service gRPC calls. All enrichment and reference-existence validation lives in gateway.

**Existence checks in gateway** — before create/update mutations gateway calls `GetXByIds` batch RPCs. If any requested ID is absent from
the response, gateway throws `Status.INVALID_ARGUMENT` → HTTP 400. There is no separate `existsByIds` RPC; absence is derived by comparing
requested vs returned ID sets.

**Async enrichment executor** — `rococo-gateway` has `AsyncConfig` bean `enrichmentExecutor` (`ThreadPoolTaskExecutor`, core=8, max=32,
prefix `"enrich-"`). Used exclusively for parallel batch enrichment in `GrpcPaintingClient`.

**Image handling** — stored as `BYTEA` in PostgreSQL. Transmitted as raw base64 over gRPC. Gateway passes through to frontend.

**gRPC error mapping** — `GrpcExceptionHandler` in gateway maps `StatusRuntimeException` → HTTP: `NOT_FOUND→404`, `ALREADY_EXISTS→409`,
`INVALID_ARGUMENT→400`, `INTERNAL→500`.

### Proto Files

Located in `rococo-grpc-common/src/main/proto/`. One file per service:
`rococo-geo.proto`, `rococo-userdata.proto`, `rococo-artist.proto`, `rococo-museum.proto`, `rococo-painting.proto`.

Generated Java classes land in `io.efremov.rococo.grpc` package.

**Batch RPCs** (added as part of enrichment refactor):

| Service           | RPC                 | Request                              | Response            |
|-------------------|---------------------|--------------------------------------|---------------------|
| `GeoService`      | `GetCountriesByIds` | `CountriesByIdRequest` (repeated id) | `CountriesResponse` |
| `MuseumService`   | `GetMuseumsByIds`   | `MuseumsByIdRequest` (repeated id)   | `MuseumsResponse`   |
| `PaintingService` | `GetPaintingsByIds` | `PaintingsByIdRequest` (repeated id) | `PaintingsResponse` |

`GetCountryById` (single geo RPC) was removed — use batch instead.

### Database & Migrations

Each service runs Flyway against own DB. Schema naming: `rococo-<service>` (dash, not underscore). Migration files:

```
rococo-<service>/src/main/resources/db/migration/rococo-<service>/V1__schema_init.sql
```

PostgreSQL credentials (local): `postgres` / `secret`, port `6432`.

### Kafka

KRaft mode (no Zookeeper), `confluentinc/cp-kafka:7.8.0`, port `9092`.

- **Producer**: `rococo-auth` → topic `user-registered` (message = username string, key = username)
- **Consumer**: `rococo-userdata`, group `rococo-userdata`; creates user profile if not exists (idempotent)

### Build Structure

Gradle multi-module. Versions centralized in root `build.gradle` `subprojects { ext { ... } }`.

**Group A** (all 7 Spring Boot services): Lombok, `spring-boot-starter-test`, `useJUnitPlatform`, `printVersion` — declared in root.

**Group B** (5 gRPC domain services: geo, artist, museum, painting, userdata): `rococo-grpc-common`, `spring-boot-starter-data-jpa`, Flyway,
PostgreSQL driver, gRPC trio, `grpc-server-spring-boot-starter` — declared in root.

Each module's `build.gradle` contains only unique deps (e.g., `spring-boot-starter-cache` for geo, `spring-kafka` for userdata).

**Key versions** (root ext):

- `springBootVersion` = `3.5.8`
- `grpcVersion` = `1.75.0`
- `lombokVersion` = `1.18.42`
- `flywayVersion` = `11.20.0`

**Windows port issue**: Docker/Hyper-V reserves ranges 7995–8194, which covers all gRPC service ports (8092–8100). Fix:
`net stop winnat && net start winnat` in PowerShell as Administrator.

### Code Conventions

- Base package: `io.efremov.rococo` across all modules
- Entities: Lombok `@Getter`/`@Setter`, UUID PKs (`uuid_generate_v4()`), HibernateProxy-safe `equals`/`hashCode`
- Nullability: JSpecify annotations (`@NonNull`, `@Nullable`)
- gRPC impls extend `*Grpc.*ImplBase`, annotated `@GrpcService`
- gRPC clients: `@GrpcClient("<name>")` field injection; names match `grpc.client.*` YAML keys
- REST: public GET, authenticated POST/PATCH (enforced in `SecurityConfig`)
- Tests: `@ExtendWith(MockitoExtension.class)`, AssertJ assertions, suffix `Test`

## gRPC Integration Tests (rococo-tests)

### Infrastructure

- `BaseGrpcTest` — provides static blocking stubs: `ARTIST_BLOCKING_STUB`, `MUSEUM_BLOCKING_STUB`,
  `PAINTING_BLOCKING_STUB`, `GEO_BLOCKING_STUB`, `USERDATA_BLOCKING_STUB`
- Tests in `rococo-tests/src/test/java/io/efremov/rococo/test/api/grpc/`, extend `BaseGrpcTest`
- `ValidationGrpcSteps` — central assertion utility (mirrors `ValidationSteps` for REST)
    - `checkResponse(Request, Response)` — compares request fields ↔ response fields
    - `checkEntity(Request, Response)` — fetches entity from DB by **request** data, compares ↔ response
    - `checkEntity(Request, String id)` — fetches entity from DB, compares ↔ request fields (after Create/Update)
    - `checkPageResponse(PageRequest, PageResponse)` — validates pagination metadata + content list against DB
    - `checkStatusRuntimeException(callable, Status.Code, description)` — asserts gRPC error code + message

### TestDataHelper gRPC helpers

- `getCreateArtistRequest()`, `getCreateMuseumRequest()`, `getCreatePaintingRequest()` — proto Create builders
- `getUpdateArtistRequest(ArtistResponse)`, `getUpdateMuseumRequest(MuseumResponse)`, `getUpdatePaintingRequest(PaintingResponse)` — proto
  Update builders from existing response
- `getAnyCountry()` — `CountryEntity` from DB

### gRPC test conventions

- Class annotations: `@Tag(MUTATION_API_TAG|INFO_API_TAG) @Epic("API") @Feature("rococo-<service>") @Story("<action>")`
- No auth on gRPC tests — auth is enforced at gateway (REST) level only
- For Update positive tests: create entity via stub → build UpdateRequest via `TestDataHelper.getUpdate*Request(response)` → modify field →
  call update stub
- For duplicate/not-found negative tests in Update: create a fresh entity first, then attempt the conflicting/invalid update

### gRPC error messages (from service implementations)

Domain services only validate own-field constraints. Cross-service reference errors originate in **gateway**.

| Scenario                                  | Source            | Code               | Message                                     |
|-------------------------------------------|-------------------|--------------------|---------------------------------------------|
| Artist not found (by id)                  | `rococo-artist`   | `NOT_FOUND`        | `"Artist not found: <uuid>"`                |
| Museum not found (by id)                  | `rococo-museum`   | `NOT_FOUND`        | `"Museum not found: <uuid>"`                |
| Painting not found (by id)                | `rococo-painting` | `NOT_FOUND`        | `"Painting not found: <uuid>"`              |
| Artist name duplicate                     | `rococo-artist`   | `ALREADY_EXISTS`   | `"Artist name already taken: <name>"`       |
| Museum title duplicate                    | `rococo-museum`   | `ALREADY_EXISTS`   | `"Museum title already taken: <title>"`     |
| Painting title duplicate                  | `rococo-painting` | `ALREADY_EXISTS`   | `"Painting already exists: <title>"`        |
| Field blank                               | domain service    | `INVALID_ARGUMENT` | `"<field>: must not be blank"`              |
| Field length out of range                 | domain service    | `INVALID_ARGUMENT` | `"<field>: must be from N to M characters"` |
| Photo/content exceeds 1 MB                | domain service    | `INVALID_ARGUMENT` | `"<field>: must not exceed 1 MB"`           |
| Country not found (create/update museum)  | gateway           | `INVALID_ARGUMENT` | `"Country not found: <uuid>"`               |
| Artist not found (create/update painting) | gateway           | `INVALID_ARGUMENT` | `"Artist not found: <uuid>"`                |
| Museum not found (create/update painting) | gateway           | `INVALID_ARGUMENT` | `"Museum not found: <uuid>"`                |

### Compile check (no test run)

```bash
./gradlew :rococo-tests:compileTestJava
```

## Integration Tests (rococo-tests)

### Infrastructure

- `AuthApiClient.register(username, password)` — full OAuth2 PKCE flow, stores token in `ApiLoginExtension` thread-local
- `BearerTokenInterceptor` reads token from `ApiLoginExtension.getToken()` automatically — do NOT pass token to `GatewayApiClient`
  constructor
- `GatewayApiClient()` — no-arg constructor; use `*Raw()` methods (returning `retrofit2.Response<T>`) for HTTP status assertions
- `TestDataHelper` — static helpers: `createRandomArtist`, `createRandomMuseum`, `createRandomPainting`, `fetchRandomCountry`
- `@ApiLogin` on test method — auto-registers random user before test, injects `GatewayApiClient` as method param

### Test conventions

- One class per API method, in `rococo-tests/src/test/java/io/efremov/rococo/test/rest/`
- Class annotations: `@Tag("api") @Epic("API") @Feature("rococo-<module>") @Story("<action>")`
- Method annotations: `@DisplayName` only
- Method naming: `positive/negativeVerbNounWithCaseTest` e.g. `negativeCreateArtistWithoutAuthTest`
- Every AssertJ assertion must have `.as("field name")`
