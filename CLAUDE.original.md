# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

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
docker compose -f docker-compose-local.yml up -d

# Free ports used by running services
bash ports.sh   # kills PIDs on 9000, 8080, 3000
```

Start services individually (each in its own terminal):

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

- **rococo-grpc-common** — pure library (no Spring Boot); contains all `.proto` files and generated Java stubs. All gRPC-enabled services
  depend on it. Must build before other modules.
- **rococo-auth** — OAuth2 Authorization Server (Spring Authorization Server). Handles login/registration UI (Thymeleaf), issues JWTs,
  publishes `user-registered` Kafka events after successful registration.
- **rococo-gateway** — OAuth2 Resource Server. No database. Routes REST calls from the frontend to backend services via gRPC. Assembles
  composite responses (e.g., enriches painting with full artist/museum objects via parallel `CompletableFuture` calls).
- **rococo-userdata** — stores user profiles (first/last name, avatar). Created lazily via Kafka consumer; updated via gRPC.
- **rococo-geo** — country reference data (~250 countries in Russian). Results cached with `ConcurrentMapCacheManager` — country data never
  changes at runtime, no eviction needed.
- **rococo-artist / rococo-museum / rococo-painting** — domain CRUD services. Each owns its own database. No cross-service FK constraints (
  referential integrity enforced at application level).

### Key Cross-Service Patterns

**Data enrichment in gateway** — backend gRPC services return only IDs for related entities. The gateway resolves them:

- `MuseumResponse` from `rococo-museum` contains `country_id`; gateway calls geo service to fill `geo.country.name`.
- `PaintingResponse` from `rococo-painting` contains `artist_id` and optional `museum_id`; gateway fetches both in parallel.

**Image handling** — stored as `BYTEA` in PostgreSQL. Transmitted as raw base64 strings over gRPC. The gateway passes these through directly
to the frontend.

**gRPC error mapping** — `GrpcExceptionHandler` in gateway maps `StatusRuntimeException` codes to HTTP: `NOT_FOUND→404`,
`ALREADY_EXISTS→409`, `INVALID_ARGUMENT→400`, `INTERNAL→500`.

### Proto Files

Located in `rococo-grpc-common/src/main/proto/`. One file per service:
`rococo-geo.proto`, `rococo-userdata.proto`, `rococo-artist.proto`, `rococo-museum.proto`, `rococo-painting.proto`.

Generated Java classes land in `io.efremov.rococo.grpc` package.

### Database & Migrations

Each service runs Flyway against its own database. Schema naming follows `rococo-<service>` (with a dash, not underscore). Migration files
are at:

```
rococo-<service>/src/main/resources/db/migration/rococo-<service>/V1__schema_init.sql
```

PostgreSQL credentials (local): `postgres` / `secret`, port `5432`.

### Kafka

KRaft mode (no Zookeeper), `confluentinc/cp-kafka:7.8.0`, port `9092`.

- **Producer**: `rococo-auth` → topic `user-registered` (message = username string, key = username)
- **Consumer**: `rococo-userdata`, group `rococo-userdata`; creates user profile if not already exists (idempotent)

### Code Conventions

- Base package: `io.efremov.rococo` across all modules
- Entities: Lombok `@Getter`/`@Setter`, UUID PKs (`uuid_generate_v4()`), HibernateProxy-safe `equals`/`hashCode`
- Nullability: JSpecify annotations (`@NonNull`, `@Nullable`)
- gRPC service implementations extend `*Grpc.*ImplBase` and are annotated `@GrpcService`
- gRPC clients use `@GrpcClient("<name>")` field injection; client names match keys in `grpc.client.*` YAML config
- REST endpoints: public GET, authenticated POST/PATCH (enforced in `SecurityConfig`)
- Tests: `@ExtendWith(MockitoExtension.class)`, AssertJ assertions, suffix `Test`
