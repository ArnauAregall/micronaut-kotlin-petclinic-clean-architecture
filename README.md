# micronaut-kotlin-petclinic-clean-architecture

[![Build Gradle project](https://github.com/ArnauAregall/micronaut-kotlin-petclinic-clean-architecture/actions/workflows/build-gradle-project.yml/badge.svg)](https://github.com/ArnauAregall/micronaut-kotlin-petclinic-clean-architecture/actions/workflows/build-gradle-project.yml)

Sample repository to explore microservices architecture patterns using Micronaut and Kotlin while following **clean architecture/ports and adapters**.

Tech stack:

- Kotlin
- Gradle
- GraalVM
- Micronaut
- Project Reactor
- Postgres
- Flyway
- Hibernate / R2DBC
- Apache Kafka
- Redis
- Keycloak
- Docker
- Testcontainers

The goal is to mimic the well known project [Spring PetClinic](https://spring-petclinic.github.io/) but using Micronaut and following a distributed architecture design.

----
## Requirements

The application requires **JDK 21** on a GraalVM distribution.

Is recommended to use [SDKMAN!](https://sdkman.io/) to install the JDK. 

````shell
$ curl -s "https://get.sdkman.io" | bash
$ sdk install java 21.0.2-graalce
$ sdk use java 21.0.2-graalce
````
----

## Running the application

**Add the following entry to your `/etc/hosts`**:

```
# Required for Keycloak issuer matching 
127.0.0.1 keycloak.local
```

Also see `.env` file for needed environment variables.

### Containerized with `docker-compose`

**Build Micronaut applications Docker images**:

```
./gradlew dockerBuildNative
```

**Start all the applications and infrastructure containers**:

```
docker compose --profile petclinic up
```

### Local development mode

**1. Start infrastructure dependencies containers**

```shell
docker compose up
```

**2. identity-service**

```shell
./gradlew :identity-service:nativeCompile # or from your IDE with the env vars below

export MICRONAUT_ENVIRONMENTS=dev OAUTH2_CLIENT_SECRET=${IDENTITY_SERVICE_OAUTH2_CLIENT_SECRET}; ./identity-service/build/native/nativeCompile/identity-service
```

**3. pet-service**

```shell
./gradlew :pet-service:nativeCompile # or from your IDE with the env vars below

export MICRONAUT_ENVIRONMENTS=dev OAUTH2_CLIENT_SECRET=${PET_SERVICE_OAUTH2_CLIENT_SECRET}; ./pet-service/build/native/nativeCompile/pet-service
```

**4. vet-service**

```shell
./gradlew :vet-service:nativeCompile # or from your IDE with the env vars below

export MICRONAUT_ENVIRONMENTS=dev OAUTH2_CLIENT_SECRET=${VET_SERVICE_OAUTH2_CLIENT_SECRET}; ./vet-service/build/native/nativeCompile/vet-service
```

----

### Keycloak notes

**How to import Keycloak OpenID realm**

- Open your browser and head to [Master Realm Admin Console](http://keycloak.local:8082/admin/master/console/) page.
- Login with Keycloak Administrator credentials.
- Create a new realm named `petclinic` by importing the file `./keycloak/realm/petclinic-realm.json`.

**OpenID client settings**:

- Valid Redirect URIs:

    ````
    http://localhost:${server.port}/oauth/callback/keycloak 
    ````

**Client Scope**

Create a new client scope for both `identity-service` and `pet-service` OAuth clients.

The client scope should contain an **Audience mapper** that adds both services as audience in the `aud` claim of the JWT token.

Fixes:

```
13:54:48.623 [default-nioEventLoopGroup-1-3] DEBUG i.m.s.t.jwt.validator.JwtValidator - Validating signed JWT
13:54:48.645 [default-nioEventLoopGroup-1-3] DEBUG i.m.s.t.j.s.jwks.JwksSignatureUtils - JWT Key ID: gOb1j1sIHJKVTacOrsKLH-LaQEkK_rGnSGnWnVXFZ18
13:54:48.645 [default-nioEventLoopGroup-1-3] DEBUG i.m.s.t.j.s.jwks.JwksSignatureUtils - JWK Set Key IDs: gOb1j1sIHJKVTacOrsKLH-LaQEkK_rGnSGnWnVXFZ18,2avwDqmRvQW-nJcEZQiIjoIY5wsNyarebI6kziBY2oU
13:54:48.646 [default-nioEventLoopGroup-1-3] DEBUG i.m.s.t.j.s.jwks.JwksSignatureUtils - Found 1 matching JWKs
13:54:48.651 [default-nioEventLoopGroup-1-3] DEBUG i.m.s.o.c.IdTokenClaimsValidator - audiences 'identity-service' does not contain client id 'pet-service'
```
