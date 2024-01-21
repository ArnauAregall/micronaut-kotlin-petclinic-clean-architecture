# micronaut-kotlin-petclinic-clean-architecture

[![Build Gradle project](https://github.com/ArnauAregall/micronaut-kotlin-petclinic-clean-architecture/actions/workflows/build-gradle-project.yml/badge.svg)](https://github.com/ArnauAregall/micronaut-kotlin-petclinic-clean-architecture/actions/workflows/build-gradle-project.yml)

Sample repository to explore microservices architecture patterns using Micronaut and Kotlin while following **clean architecture/ports and adapters**.

Tech stack:

- Kotlin
- Gradle
- Micronaut
- Postgres
- Apache Kafka
- Keycloak
- Docker
- Testcontainers

The goal is to mimic the well known project [Spring PetClinic](https://spring-petclinic.github.io/) but using Micronaut and following a distributed architecture design.

----

## Running the application

### Infrastructure dependencies

```shell
docker-compose up
```

### identity-service

```shell
./gradlew :identity-service:nativeCompile

export MICRONAUT_ENVIRONMENTS=dev OAUTH2_CLIENT_SECRET=xxx; ./identity-service/build/native/nativeCompile/identity-service
```

### pet-service

```shell
./gradlew :pet-service:nativeCompile

export MICRONAUT_ENVIRONMENTS=dev OAUTH2_CLIENT_SECRET=xxxx; ./pet-service/build/native/nativeCompile/pet-service
```

----

### Keycloak notes

OpenID client settings:

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
