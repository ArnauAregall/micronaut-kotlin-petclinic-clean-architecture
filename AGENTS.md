# AGENTS.md — Engineering Agent Guide

Micronaut Kotlin PetClinic is a multi-module Gradle monorepo implementing the veterinary clinic domain as three
independently deployable microservices: **identity-service**, **pet-service**, and **vet-service**. Each service follows
ports-and-adapters (hexagonal) clean architecture, owns its own PostgreSQL schema, and communicates asynchronously via
Apache Kafka. OAuth2/JWT security is delegated to Keycloak.

---

## Tech Stack

| Layer                        | Technology                                                                                                                              |
|------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------|
| Language                     | Kotlin 1.9, JVM 21                                                                                                                      |
| Framework                    | Micronaut 4.3.4 (Netty runtime)                                                                                                         |
| Build                        | Gradle wrapper (`./gradlew`), Kotlin DSL (`build.gradle.kts`)                                                                           |
| identity-service persistence | Hibernate JPA (blocking), Flyway migrations, PostgreSQL                                                                                 |
| pet-service persistence      | Micronaut Data R2DBC (reactive, Project Reactor), `@R2dbcRepository`, Flyway migrations, PostgreSQL, Redis (Lettuce)                    |
| vet-service persistence      | Raw JDBC via `JdbcOperations` (blocking), manual `ResultSet` mapping, Flyway migrations, PostgreSQL                                     |
| Messaging                    | Apache Kafka — `identity` topic (identity-service produces; pet-service and vet-service consume)                                        |
| Security                     | Keycloak (OAuth2/OIDC IdP), Micronaut Security JWT (`idtoken` authentication)                                                           |
| Unit testing                 | JUnit 5, MockK (`@MockK`, `@InjectMockKs`, `@ExtendWith(MockKExtension::class)`)                                                        |
| Integration testing          | `@MicronautTest`, Micronaut Test Resources (Testcontainers for PostgreSQL, Kafka, Keycloak, MockServer), REST Assured Kotlin extensions |
| Native images                | GraalVM (`./gradlew :<service>:dockerBuildNative`)                                                                                      |

**⚠️ Micronaut & Kotlin patterns — get these right before writing any code:**

| Use                                                                    | Not                                            | Note                                                                     |
|------------------------------------------------------------------------|------------------------------------------------|--------------------------------------------------------------------------|
| `@UseCase` (from `:common`)                                            | `@Singleton` on use case implementations       | `@UseCase` is a meta-annotation for `@Singleton`                         |
| `@MicronautTest(transactional = false)`                                | `@SpringBootTest` or `@Transactional` on tests | Micronaut manages transactions differently; keep `transactional = false` |
| `@TestResourcesProperties(providers = [KeycloakPropsProvider::class])` | Manually injecting Keycloak config             | Keycloak is managed by Testcontainers via the Test Resources extension   |
| `@CleanDatabase` (from `:test-resources`)                              | Truncating tables manually                     | Runs `flyway.clean()` + `flyway.migrate()` before each `@Test`           |
| `MockK` (`io.mockk`)                                                   | Mockito / `@MockBean` / `@MockitoBean`         | The project uses MockK exclusively for mocking                           |
| Kotlin `data class` for domain models                                  | Java POJOs or `@Entity` in the domain layer    | Domain models are pure Kotlin, no framework annotations                  |
| `internal` visibility on use cases and adapters                        | `public`                                       | Expose only what the port interface requires                             |

---

## Key Commands

**Git operations are strictly off-limits for agents.** Do not run any `git` command. Do not stage, commit, push, or
branch.

### All services (from repo root)

```bash
./gradlew build                                   # compile + test all modules
./gradlew test                                    # run all tests across all modules
./gradlew :identity-service:test                  # run identity-service tests only
./gradlew :pet-service:test                       # run pet-service tests only
./gradlew :vet-service:test                       # run vet-service tests only
./gradlew :common:test                            # run common module tests
```

> **During iteration, never run `./gradlew build` as your feedback loop** — it is slow. Run only the affected service
> tests: `./gradlew :<service>:test --tests "<TestClassName>"`.

### Running a single test class or method

```bash
./gradlew :identity-service:test --tests "tech.aaregall.lab.petclinic.identity.infrastructure.adapters.input.http.IdentityControllerIT"
./gradlew :identity-service:test --tests "tech.aaregall.lab.petclinic.identity.infrastructure.adapters.input.http.IdentityControllerIT.CreateIdentity*"
./gradlew :pet-service:test --tests "tech.aaregall.lab.petclinic.pet.application.usecase.CreatePetUseCaseTest"
```

### Docker Compose (local dev bootstrap)

```bash
docker compose up -d        # start Keycloak, PostgreSQL, Kafka, Redis, MockServer
docker compose down         # stop and remove containers
```

> Services auto-configure their ports via Micronaut Test Resources when running tests. Docker Compose is only needed
> when running services as full applications outside the test harness.

### GraalVM native images

```bash
./gradlew :identity-service:dockerBuildNative
./gradlew :pet-service:dockerBuildNative
./gradlew :vet-service:dockerBuildNative
```

### Interpreting failures

| Symptom                                                 | Cause                                                           | Fix                                                                                |
|---------------------------------------------------------|-----------------------------------------------------------------|------------------------------------------------------------------------------------|
| `No bean of type [...] found`                           | Missing `@UseCase`, `@Singleton`, or output port implementation | Check that the class is annotated and resides in the correct package               |
| `FlywayException: Validate failed`                      | An existing migration script was edited                         | Never edit committed migrations — create a new versioned script                    |
| `Could not find a valid Docker environment`             | Docker daemon not running                                       | `docker ps` to check; Testcontainers requires Docker                               |
| `BeanDefinitionException` on `@MockK` / `@InjectMockKs` | Wrong MockK extension                                           | Add `@ExtendWith(MockKExtension::class)` to the unit test class                    |
| `Unauthorized` in IT when calling protected endpoints   | Missing `Authorization` header                                  | Use `KeycloakFixture.getAuthorizationBearer()` to inject a valid token             |
| Test data leaks between tests                           | `@CleanDatabase` missing                                        | Annotate the IT class with `@CleanDatabase`                                        |
| `MockServer expectation not satisfied`                  | MockServer stub not configured                                  | Add stub via `mockServerClient.when(...).respond(...)` before calling the endpoint |

---

## Project Structure

```
identity-service/       ← Manages identities, contact details, roles; publishes identity Kafka events
pet-service/            ← Manages pets and pet owner projections; consumes identity events; uses Redis cache
vet-service/            ← Manages vets and specialities; consumes identity events; HTTP-validates identities
common/                 ← Shared annotations (@UseCase), reactive utilities, common HTTP error handler
test-resources/         ← Shared Testcontainers wiring: Keycloak, MockServer, @CleanDatabase
docker-compose.yml      ← Local dev: Keycloak, PostgreSQL, Kafka, Redis, MockServer
```

### Service internal structure (all services follow this layout)

```
<service>/src/main/kotlin/tech/aaregall/lab/petclinic/<service>/
  domain/
    model/              ← Domain models and value objects (pure Kotlin, no framework annotations)
    event/              ← Domain events (data classes published via output ports)
  application/
    ports/
      input/            ← Input port interfaces (fun interface) + Command data classes
      output/           ← Output port interfaces (persistence, event publishing, HTTP, cache)
    usecase/            ← Use case implementations: @UseCase internal class; implement input ports; inject output ports
  infrastructure/
    adapters/
      input/
        http/           ← Micronaut @Controller classes, request/response DTOs, mappers, error handlers
        kafka/          ← @KafkaListener consumer adapters (pet-service, vet-service only)
      output/
        persistence/    ← JPA/@MappedEntity entities, repositories, persistence adapters, mappers
        kafka/          ← @KafkaClient producer adapters (identity-service only)
        http/           ← Micronaut HTTP client adapters for outbound calls (vet-service: identity validation)
        cache/          ← Redis cache adapters (pet-service only)
    config/
      security/         ← Custom JWT claim validators

<service>/src/main/resources/
  application.yml       ← Service configuration (name, security, datasource, Flyway, Kafka)
  db/migrations/        ← Flyway SQL migration scripts
```

> **identity-service exception:** its use cases live in `application/ports/usecase/` (legacy location). All new services
> and new use cases in pet-service and vet-service use `application/usecase/`.

**Non-obvious constraints:**

- Domain models in `domain/model/` must not import any Micronaut, Jakarta, or infrastructure classes.
- Input port interfaces in `application/ports/input/` must not import infrastructure classes.
- Output port interfaces in `application/ports/output/` are the only dependency that use cases may take on
  infrastructure — never inject a repository or HTTP client directly into a use case.
- Use case implementations are `internal` classes annotated with `@UseCase`. They implement an input port interface and
  receive output ports via constructor injection.
- HTTP controllers inject input port interfaces, never use case classes directly.
- `application.yml` is the only configuration format — never create `.properties` files.

---

## Persistence Technologies per Service

| Service          | Persistence                                      | Repository / access pattern                                                | Entity annotation                           |
|------------------|--------------------------------------------------|----------------------------------------------------------------------------|---------------------------------------------|
| identity-service | Hibernate JPA (blocking)                         | `JpaRepository<E, ID>` (Micronaut Data JPA)                                | `@Entity` (JPA)                             |
| pet-service      | Micronaut Data R2DBC (reactive, Project Reactor) | `ReactorPageableRepository<E, ID>` with `@R2dbcRepository`                 | `@MappedEntity`                             |
| vet-service      | Raw JDBC (blocking)                              | `JdbcOperations` injected directly into adapters — no repository interface | No entity class; manual `ResultSet` mapping |

> **pet-service returns reactive types.** Persistence adapters return `Mono<T>` / `Flux<T>` (Project Reactor). Use case
> implementations also return reactive types — wrap in `UnitReactive` / `CollectionReactive` from `:common` when the
> port
> interface dictates it.

---

## Testing

### Unit tests — use case tests

All use case unit tests use MockK and JUnit 5. No Spring or Micronaut context is started.

```kotlin
@ExtendWith(MockKExtension::class)
internal class CreateIdentityUseCaseTest {

    @MockK
    lateinit var identityOutputPort: IdentityOutputPort

    @MockK
    lateinit var identityEventPublisher: IdentityEventPublisher

    @InjectMockKs
    lateinit var useCase: CreateIdentityUseCase

    @Test
    fun `It should create an Identity and publish an event`() {
        val expected = Identity(id = IdentityId.create(), firstName = "Foo", lastName = "Bar")

        every { identityOutputPort.createIdentity(any()) } returns expected
        every { identityEventPublisher.publishIdentityCreatedEvent(any()) } answers { callOriginal() }

        val result = useCase.createIdentity(CreateIdentityCommand("Foo", "Bar"))

        assertThat(result).isEqualTo(expected)
        verify { identityEventPublisher.publishIdentityCreatedEvent(IdentityCreatedEvent(result)) }
    }
}
```

### Integration tests — controller / adapter tests

All integration tests use `@MicronautTest(transactional = false)` and resolve Testcontainers infrastructure via the
`:test-resources` module.

**Mandatory annotations:**

| Annotation                                                               | Purpose                                              | Required when                        |
|--------------------------------------------------------------------------|------------------------------------------------------|--------------------------------------|
| `@MicronautTest(transactional = false)`                                  | Starts the full Micronaut context                    | Always                               |
| `@CleanDatabase`                                                         | Wipes and re-migrates the schema before each `@Test` | Any test that writes to the database |
| `@TestResourcesProperties(providers = [KeycloakPropsProvider::class])`   | Injects Keycloak container properties                | Any test calling a secured endpoint  |
| `@TestResourcesProperties(providers = [MockServerPropsProvider::class])` | Injects MockServer container properties              | Any test stubbing outbound HTTP      |

**Minimal controller integration test:**

```kotlin
@MicronautTest(transactional = false)
@CleanDatabase
@TestResourcesProperties(providers = [KeycloakPropsProvider::class])
internal class IdentityControllerIT(private val embeddedServer: EmbeddedServer) {

    @Test
    fun `Should return Unauthorized when no Authorization header`() {
        Given {
            contentType(ContentType.JSON)
            body("""{"first_name": "John", "last_name": "Doe"}""")
        } When {
            port(embeddedServer.port)
            post("/api/identities")
        } Then {
            statusCode(HttpStatus.UNAUTHORIZED.code)
        }
    }

    @Test
    fun `Should create an Identity`() {
        Given {
            contentType(ContentType.JSON)
            body("""{"first_name": "John", "last_name": "Doe"}""")
            header(KeycloakFixture.getAuthorizationBearer())
        } When {
            port(embeddedServer.port)
            post("/api/identities")
        } Then {
            statusCode(HttpStatus.CREATED.code)
            body("id", notNullValue(), "first_name", equalTo("John"))
        }
    }
}
```

**Stubbing outbound HTTP with MockServer** (pet-service and vet-service pattern):

```kotlin
@MicronautTest(transactional = false)
@TestResourcesProperties(providers = [MockServerPropsProvider::class, KeycloakPropsProvider::class])
internal class PetControllerIT(
    private val embeddedServer: EmbeddedServer,
    private val mockServerClient: MockServerClient
) {

    @BeforeEach
    fun setUp() {
        mockServerClient.reset()
    }

    private fun stubIdentityResponse(identityId: UUID, status: HttpStatus) {
        mockServerClient
            .`when`(request().withMethod("GET").withPath("/api/identities/$identityId"))
            .respond(
                response().withStatusCode(status.code)
                    .withHeader("Content-Type", "application/json")
            )
    }
}
```

**Rules:**

- Never use `@MockK` or any mock library inside `@MicronautTest` integration tests. Use real beans + MockServer stubs
  for outbound HTTP, and direct input port calls to seed data.
- Call `mockServerClient.reset()` in `@BeforeEach` when a test class has multiple test methods that stub the same path.
- Always inject test data through the service's own input ports (e.g. `createPetInputPort.createPet(...)`) rather than
  writing SQL directly.
- Cover: 401 Unauthorized (no token), happy path (201/200), sad paths (404 not found, 409 conflict, 400 bad request).

---

## Database Migrations (Flyway)

Migration scripts live in `<service>/src/main/resources/db/migrations/`.

**Naming:** `V{sequential-integer}__{intent}.sql`

Before writing a new script:

1. Find the next available integer: `ls <service>/src/main/resources/db/migrations/`
2. Name with the next sequential integer and a lowercase-underscore description of **intent**, not mechanics.

```
V1__create-identity.sql           ← correct: describes what schema represents
V6__add-email-index-to-contact-details.sql   ← correct: intent-based naming
V6__alter_table_add_column.sql    ← wrong: describes mechanics, not intent
```

- **Never edit a committed migration.** Always create a new versioned script. Editing a committed script causes
  `FlywayException: Validate failed` at startup and in CI.

---

## Implementing a New Feature

### Layer order — follow this sequence

1. **Domain model** (`domain/model/`) — add data classes and value objects. No Micronaut annotations. No persistence
   types. Business invariants enforced via `init { require(...) }`.

2. **Domain events** (`domain/event/`) — add event data classes if the feature must publish Kafka events (
   identity-service only).

3. **Input port** (`application/ports/input/`) — define a `fun interface` for the use case entry point and a
   `data class` for the command. No framework imports.

4. **Output port** (`application/ports/output/`) — define an interface for every external resource the use case needs (
   persistence, event publisher, HTTP client, cache). No implementation classes here.

5. **Use case implementation** (`application/usecase/`) — annotate with `@UseCase`, make `internal`, implement the input
   port, inject output ports via constructor. No direct repository or HTTP client dependencies.

6. **Persistence adapter** (`infrastructure/adapters/output/persistence/`) — implement the output port(s). For
   identity-service: add a `@Entity` JPA entity, a `JpaRepository`, a mapper, and a `@Singleton` adapter. For
   pet-service: add a `@MappedEntity` entity, a `@R2dbcRepository`, a mapper, and a `@Singleton` adapter returning
   reactive types. For vet-service: inject `JdbcOperations` directly into a `@Singleton` adapter and write raw SQL with
   manual `ResultSet` mapping — no entity class or repository interface needed.

7. **HTTP controller** (`infrastructure/adapters/input/http/`) — add a `@Controller` class. Request DTOs go in
   `dto/request/`, response DTOs in `dto/response/`. Add a mapper in `mapper/`. Controllers inject input port interfaces
   only.

8. **Error handling** — extend `HttpExceptionHandler` in `infrastructure/adapters/input/http/error/` for any new domain
   exception that should return a specific HTTP status.

9. **Migration** — if the feature changes the schema, add a new Flyway script (see naming rules above).

10. **Tests** — write a unit test for the use case (`@ExtendWith(MockKExtension::class)`) and an integration test for
    the controller / adapter (`@MicronautTest`). Cover happy path and key error paths.

---

## Decision Gates

Answer each gate before writing code. These are not optional.

### Gate 1 — Which layer does a new class belong in?

```
Is this a new HTTP endpoint handler?
  YES → infrastructure/adapters/input/http/ inside the service
  NO  →
    Is this a Kafka consumer?
      YES → infrastructure/adapters/input/kafka/ inside the service
      NO  →
        Talks to the database, Kafka broker (as producer), HTTP client, or Redis?
          YES → infrastructure/adapters/output/<technology>/ and implement an output port
          NO  →
            Contains only data fields (domain model, value object, event, command)?
              YES → domain/model/ or domain/event/ or application/ports/input/
              NO  →
                Orchestrates output ports to fulfil a use case?
                  YES → application/usecase/ (annotate @UseCase, implement input port)
                  NO  → common/ if truly cross-cutting across services
```

### Gate 2 — Where does a new interface belong?

```
Is the interface called by a controller or Kafka consumer to trigger a business operation?
  YES → application/ports/input/ (input port)
  NO  →
    Is the interface implemented by an infrastructure adapter?
      YES → application/ports/output/ (output port)
```

### Gate 3 — How should I test this code?

```
Does the code path touch a database, Kafka, external HTTP, or the Micronaut context?
  YES → integration test (@MicronautTest; inject real beans; add @CleanDatabase if it writes to DB)
  NO  → unit test (@ExtendWith(MockKExtension::class); use @MockK and @InjectMockKs)
```

### Gate 4 — Should a test stub outbound HTTP?

```
Does the service under test call identity-service or any external HTTP API?
  YES → add @TestResourcesProperties(providers = [MockServerPropsProvider::class])
        and configure stubs via the injected MockServerClient
  NO  → no MockServer setup needed
```

### Gate 5 — Should I add a new Flyway migration?

```
Does the feature add or modify a table, column, index, or constraint?
  YES → create a new versioned V{N}__intent.sql in <service>/src/main/resources/db/migrations/
  NO  → no migration needed
```

### Gate 6 — Does a new use case need to publish a Kafka event?

```
Does the operation change identity data (create/update/delete)?
  YES → publish via IdentityEventPublisher output port (identity-service only)
  NO  → no Kafka publishing needed; pet-service and vet-service never produce events
```

### Gate 7 — Should a new persistence method be reactive?

```
Is the code in pet-service?
  YES → return Mono<T> or Flux<T> via ReactorPageableRepository / @R2dbcRepository
  NO  →
    Is the code in identity-service?
      YES → use JpaRepository (blocking; Hibernate JPA)
      NO  → vet-service uses JdbcOperations with raw SQL and manual ResultSet mapping (blocking)
```

---

## Keeping This File Up to Date

When a merge request adds a new indexed component, update the relevant section:

| New component                                   | Where to update in this file                                           |
|-------------------------------------------------|------------------------------------------------------------------------|
| New service module                              | Add entry to Project Structure                                         |
| New Kafka topic or consumer                     | Update Tech Stack messaging row and Gate 6                             |
| New outbound HTTP adapter                       | Add to service internal structure and Gate 4                           |
| New Testcontainers provider in `test-resources` | Add to Integration Testing table                                       |
| New Flyway migration                            | No file update needed — migration scripts are self-documenting         |
| New `@UseCase` in a service                     | No file update needed unless the pattern deviates from the layer order |

**Never modify the rules, patterns, or architectural guidance in this file** — those are stable conventions requiring
team agreement. Only add rows to the tables listed above.
