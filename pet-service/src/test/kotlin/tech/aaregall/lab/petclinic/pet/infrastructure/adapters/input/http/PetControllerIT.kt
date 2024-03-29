package tech.aaregall.lab.petclinic.pet.infrastructure.adapters.input.http

import io.micronaut.context.annotation.Value
import io.micronaut.data.r2dbc.operations.R2dbcOperations
import io.micronaut.http.HttpMethod.GET
import io.micronaut.http.HttpStatus
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.micronaut.test.extensions.testresources.annotation.TestResourcesProperties
import io.restassured.http.ContentType.JSON
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.everyItem
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.not
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.mockserver.model.Header
import org.mockserver.model.HttpRequest.request
import org.mockserver.model.HttpResponse.response
import org.mockserver.model.JsonBody.json
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import tech.aaregall.lab.petclinic.pet.application.ports.input.CreatePetCommand
import tech.aaregall.lab.petclinic.pet.application.ports.input.CreatePetInputPort
import tech.aaregall.lab.petclinic.pet.domain.model.PetType
import tech.aaregall.lab.petclinic.test.spec.keycloak.KeycloakPropsProvider
import tech.aaregall.lab.petclinic.test.spec.keycloak.KeycloakPropsProvider.Companion.getAuthorizationBearer
import tech.aaregall.lab.petclinic.test.spec.mockserver.MockServerPropsProvider
import tech.aaregall.lab.petclinic.test.spec.mockserver.MockServerPropsProvider.Companion.getMockServerClient
import java.time.LocalDate
import java.util.UUID
import java.util.UUID.randomUUID

@MicronautTest(transactional = false)
@TestResourcesProperties(providers = [MockServerPropsProvider::class, KeycloakPropsProvider::class])
internal class PetControllerIT(private val embeddedServer: EmbeddedServer) {

    @Value("\${app.ports.output.pet-owner.required-identity-role-name}")
    lateinit var requiredIdentityRoleName: String

    private fun mockGetIdentityResponse(identityId: UUID, httpStatus: HttpStatus) {
        getMockServerClient()
            .`when`(request().withMethod(GET.name).withPath("/api/identities/$identityId"))
            .respond {
                val response = response()
                    .withStatusCode(httpStatus.code)
                    .withHeaders(Header("Content-Type", "application/json"))

                return@respond when (httpStatus) {
                    HttpStatus.OK ->  response.withBody(json("""
                        {
                          "id": "$identityId", "first_name": "John", "last_name": "Doe", "roles": ["$requiredIdentityRoleName"]
                        }
                        """.trimIndent()
                    ))
                    else -> response
                }
            }
    }

    @Nested
    inner class SearchPets(private val createPetInputPort: CreatePetInputPort, private val r2dbc: R2dbcOperations) {

        // TODO Find a cleaner solution to not depend on r2dbc, maybe something more elegant like @CleanDatabase
        @BeforeEach
        fun beforeEach() {
            Mono.from(r2dbc.withTransaction { status ->
                status.connection.createStatement("truncate table pet").execute()
            }).block()
        }

        @Test
        fun `Should return Unauthorized when no Authorization header`() {
            Given {
                contentType(JSON)
                queryParams(mapOf("page" to "0", "size" to "20"))
            } When {
                port(embeddedServer.port)
                get("/api/pets")
            } Then {
                statusCode(HttpStatus.UNAUTHORIZED.code)
                body("message", equalTo("Unauthorized"))
            }
        }

        @Test
        fun `Should return 200 OK with empty content array when there are no Pets`() {
            Given {
                contentType(JSON)
                queryParams(mapOf("page" to "0", "size" to "20"))
                header(getAuthorizationBearer())
            } When {
                port(embeddedServer.port)
                get("/api/pets")
            } Then {
                statusCode(HttpStatus.OK.code)
                body("content.size()", equalTo(0))
                body("totalSize", equalTo(0))
            }
        }

        @Test
        fun `Should return 200 OK with not empty content array containing Pets with PetOwners details when there are Pets`() {
            val ownerIdentityId = randomUUID()
            mockGetIdentityResponse(ownerIdentityId, HttpStatus.OK)

            val pets = Flux.fromIterable(
                listOf(
                    CreatePetCommand(
                        type = PetType.DOG,
                        name = "Snoopy",
                        birthDate = LocalDate.now(),
                        ownerIdentityId = ownerIdentityId
                    ),
                    CreatePetCommand(
                        type = PetType.CAT,
                        name = "Garfield",
                        birthDate = LocalDate.now().minusYears(10),
                        ownerIdentityId = ownerIdentityId
                    )
                )
            )
                .flatMap { createPetInputPort.createPet(it).toMono() }
                .collectList()
                .block()!!

            Given {
                contentType(JSON)
                queryParams(mapOf("page" to "0", "size" to "20"))
                header(getAuthorizationBearer())
            } When {
                port(embeddedServer.port)
                get("/api/pets")
            } Then {
                statusCode(HttpStatus.OK.code)
                // using '*' to spread array into varargs to match containsInAnyOrder
                body(
                    "content.size()", allOf(
                        not(0), equalTo(pets.size)
                    ),
                    "content.id", containsInAnyOrder(
                        *pets.map { it.id.toString() }.toTypedArray()
                    ),
                    "content.name", containsInAnyOrder(
                        *pets.map { it.name }.toTypedArray()
                    ),
                    "content.birth_date", containsInAnyOrder(
                        *pets.map { it.birthDate.toString() }.toTypedArray()
                    ),
                    "content.owner.id", everyItem(`is`(ownerIdentityId.toString())),
                    "content.owner.first_name", everyItem(`is`("John")),
                    "content.owner.last_name", everyItem(`is`("Doe")),
                    "totalSize", allOf(
                        not(0), equalTo(pets.size)
                    )
                )
            }
        }

    }

    @Nested
    inner class LoadPet(private val createPetInputPort: CreatePetInputPort) {

        @Test
        fun `Should return Unauthorized when no Authorization header`() {
            Given {
                pathParam("id", randomUUID())
            } When {
                port(embeddedServer.port)
                get("/api/pets/{id}")
            } Then {
                statusCode(HttpStatus.UNAUTHORIZED.code)
                body("message", equalTo("Unauthorized"))
            }
        }

        @Test
        fun `Should return 400 Bad Request when ID is not a UUID`() {
            Given {
                pathParam("id", "something")
                header(getAuthorizationBearer())
            } When {
                port(embeddedServer.port)
                get("/api/pets/{id}")
            } Then {
                statusCode(HttpStatus.BAD_REQUEST.code)
                body(
                    "message", equalTo("Bad Request"),
                    "_embedded.errors.size()", equalTo(1),
                    "_embedded.errors[0].message", containsString("Invalid UUID string")
                )
            }
        }

        @Test
        fun `Should return 404 Not Found when Pet does not exist`() {
            Given {
                pathParam("id", randomUUID())
                header(getAuthorizationBearer())
            } When {
                port(embeddedServer.port)
                get("/api/pets/{id}")
            } Then {
                statusCode(HttpStatus.NOT_FOUND.code)
                body("message", equalTo("Not Found"))
            }
        }

        @Test
        fun `Should return 200 OK with null Owner details when Pet exists and does not have a PetOwner`() {
            val pet = createPetInputPort.createPet(
                CreatePetCommand(
                    type = PetType.CAT,
                    name = "Silvester",
                    birthDate = LocalDate.of(2024, 1, 31),
                    ownerIdentityId = null
                )
            ).block()!!

            Given {
                pathParam("id", pet.id.toString())
                header(getAuthorizationBearer())
            } When {
                port(embeddedServer.port)
                get("/api/pets/{id}")
            } Then {
                statusCode(HttpStatus.OK.code)
                body(
                    "id", equalTo(pet.id.toString()),
                    "type", equalTo("CAT"),
                    "name", equalTo("Silvester"),
                    "birth_date", equalTo("2024-01-31"),
                    "owner", nullValue()
                )
            }
        }

        @Test
        fun `Should return 200 OK with filled Owner details when Pet exists and has a PetOwner`() {
            val ownerIdentityId = randomUUID()
            mockGetIdentityResponse(ownerIdentityId, HttpStatus.OK)

            val pet = createPetInputPort.createPet(
                CreatePetCommand(
                    type = PetType.BIRD,
                    name = "Tweety",
                    birthDate = LocalDate.of(2024, 1, 31),
                    ownerIdentityId = ownerIdentityId
                )
            ).block()!!

            Given {
                pathParam("id", pet.id.toString())
                header(getAuthorizationBearer())
            } When {
                port(embeddedServer.port)
                get("/api/pets/{id}")
            } Then {
                statusCode(HttpStatus.OK.code)
                body(
                    "id", equalTo(pet.id.toString()),
                    "type", equalTo("BIRD"),
                    "name", equalTo("Tweety"),
                    "birth_date", equalTo("2024-01-31"),
                    "owner", notNullValue(),
                    "owner.id", equalTo(ownerIdentityId.toString()),
                    "owner.first_name", equalTo("John"),
                    "owner.last_name", equalTo("Doe")
                )
            }
        }
    }

    @Nested
    inner class AdoptPet(private val createPetInputPort: CreatePetInputPort) {

        @Test
        fun `Should return Unauthorized when no Authorization token`() {
            Given {
                pathParam("id", randomUUID())
                contentType(JSON)
                body("""
                    {"owner_identity_id": "${randomUUID()}"}
                """.trimIndent())
            } When {
                port(embeddedServer.port)
                patch("/api/pets/{id}/adopt")
            } Then {
                statusCode(HttpStatus.UNAUTHORIZED.code)
                body("message", equalTo("Unauthorized"))
            }
        }

        @Test
        fun `Should return 400 Bad Request when ID is not a UUID`() {
            Given {
                pathParam("id", "something")
                header(getAuthorizationBearer())
                contentType(JSON)
                body("""
                    {"owner_identity_id": "${randomUUID()}"}
                """.trimIndent())
            } When {
                port(embeddedServer.port)
                patch("/api/pets/{id}/adopt")
            } Then {
                statusCode(HttpStatus.BAD_REQUEST.code)
                body(
                    "message", equalTo("Bad Request"),
                    "_embedded.errors.size()", equalTo(1),
                    "_embedded.errors[0].message", containsString("Invalid UUID string")
                )
            }
        }

        @Test
        fun `Should return 400 Bad Request when body owner_identity_id is null`() {
            Given {
                pathParam("id", randomUUID().toString())
                header(getAuthorizationBearer())
                contentType(JSON)
                body("""
                    {"owner_identity_id": null}
                """.trimIndent())
            } When {
                port(embeddedServer.port)
                patch("/api/pets/{id}/adopt")
            } Then {
                statusCode(HttpStatus.BAD_REQUEST.code)
                body(
                    "message", equalTo("Bad Request"),
                    "_embedded.errors.size()", equalTo(1),
                    "_embedded.errors[0].message", containsString("Pet adopter Identity ID is required")
                )
            }
        }

        @Test
        fun `Should return 404 Not Found when Pet does not exist`() {
            Given {
                pathParam("id", randomUUID().toString())
                header(getAuthorizationBearer())
                contentType(JSON)
                body("""
                    {"owner_identity_id": "${randomUUID()}"}
                """.trimIndent())
            } When {
                port(embeddedServer.port)
                patch("/api/pets/{id}/adopt")
            } Then {
                statusCode(HttpStatus.NOT_FOUND.code)
            }
        }

        @Test
        fun `Should return 400 Bad Request when PetOwner does not exist`() {
            val pet = createPetInputPort.createPet(
                CreatePetCommand(
                    type = PetType.DOG,
                    name = "Snoopy",
                    birthDate = LocalDate.now(),
                    ownerIdentityId = null
                )
            ).block()!!

            val ownerIdentityId = randomUUID()
            mockGetIdentityResponse(ownerIdentityId, HttpStatus.NOT_FOUND)

            Given {
                pathParam("id", pet.id.toString())
                header(getAuthorizationBearer())
                contentType(JSON)
                body("""
                    {"owner_identity_id": "$ownerIdentityId"}
                """.trimIndent())
            } When {
                port(embeddedServer.port)
                patch("/api/pets/{id}/adopt")
            } Then {
                statusCode(HttpStatus.BAD_REQUEST.code)
                body(
                    "message", equalTo("Bad Request"),
                    "_embedded.errors.size()", equalTo(1),
                    "_embedded.errors[0].message", allOf(
                        containsString("Failed to adopt Pet"),
                        containsString("Could not load the adopter PetOwner with ID $ownerIdentityId")
                    )
                )
            }
        }

        @Test
        fun `Should return 200 OK with new PetOwner details when both Pet and PetOwner exist`() {
            val pet = createPetInputPort.createPet(
                CreatePetCommand(
                    type = PetType.DOG,
                    name = "Poppy",
                    birthDate = LocalDate.of(2024, 2, 10),
                    ownerIdentityId = null
                )
            ).block()!!

            val ownerIdentityId = randomUUID()
            mockGetIdentityResponse(ownerIdentityId, HttpStatus.OK)

            Given {
                pathParam("id", pet.id.toString())
                header(getAuthorizationBearer())
                contentType(JSON)
                body("""
                    {"owner_identity_id": "$ownerIdentityId"}
                """.trimIndent())
            } When {
                port(embeddedServer.port)
                patch("/api/pets/{id}/adopt")
            } Then {
                statusCode(HttpStatus.OK.code)
                body(
                    "id", equalTo(pet.id.toString()),
                    "type", equalTo("DOG"),
                    "name", equalTo("Poppy"),
                    "birth_date", equalTo("2024-02-10"),
                    "owner", notNullValue(),
                    "owner.id", equalTo(ownerIdentityId.toString()),
                    "owner.first_name", equalTo("John"),
                    "owner.last_name", equalTo("Doe")
                )
            }
        }

    }

    @Nested
    inner class DeletePet(private val createPetInputPort: CreatePetInputPort) {

        @Test
        fun `Should return Unauthorized when no Authorization header`() {
            Given {
                pathParam("id", randomUUID())
            } When {
                port(embeddedServer.port)
                delete("/api/pets/{id}")
            } Then {
                statusCode(HttpStatus.UNAUTHORIZED.code)
                body("message", equalTo("Unauthorized"))
            }
        }

        @Test
        fun `Should return 400 Bad Request when ID is not a UUID`() {
            Given {
                pathParam("id", "something")
                header(getAuthorizationBearer())
            } When {
                port(embeddedServer.port)
                delete("/api/pets/{id}")
            } Then {
                statusCode(HttpStatus.BAD_REQUEST.code)
                body(
                    "message", equalTo("Bad Request"),
                    "_embedded.errors.size()", equalTo(1),
                    "_embedded.errors[0].message", containsString("Invalid UUID string")
                )
            }
        }

        @Test
        fun `Should return 400 Bad Request when Pet does not exist`() {
            val id = randomUUID()
            Given {
                pathParam("id", id)
                header(getAuthorizationBearer())
            } When {
                port(embeddedServer.port)
                delete("/api/pets/{id}")
            } Then {
                statusCode(HttpStatus.BAD_REQUEST.code)
                body(
                    "message", equalTo("Bad Request"),
                    "_embedded.errors.size()", equalTo(1),
                    "_embedded.errors[0].message", containsString("Failed deleting Pet with ID $id: Pet was not found")
                )
            }
        }

        @Test
        fun `Should return 204 No Content when Pet exits`() {
            val pet = createPetInputPort.createPet(
                CreatePetCommand(
                    type = PetType.DOG,
                    name = "Snoopy",
                    birthDate = LocalDate.now(),
                    ownerIdentityId = null
                )
            ).block()!!

            Given {
                pathParam("id", pet.id.toString())
                header(getAuthorizationBearer())
            } When {
                port(embeddedServer.port)
                delete("/api/pets/{id}")
            } Then {
                statusCode(HttpStatus.NO_CONTENT.code)
            }
        }

    }

    @Nested
    inner class CreatePet {

        @Test
        fun `Should return Unauthorized when no Authorization header`() {
            Given {
                contentType(JSON)
                body("""
                    {
                        "type": "DOG",
                        "name": "Bimo",
                        "birth_date": "2020-03-21"
                    }
                """.trimIndent())
            } When {
                port(embeddedServer.port)
                post("/api/pets")
            } Then {
                statusCode(HttpStatus.UNAUTHORIZED.code)
                body("message", equalTo("Unauthorized"))
            }
        }

        @Test
        fun `Should not allow blank or null fields`() {
            Given {
                header(getAuthorizationBearer())
                contentType(JSON)
                body("""
                    {
                        "type": "",
                        "name": "",
                        "birth_date": null
                    }
                """.trimIndent())
            } When {
                port(embeddedServer.port)
                post("/api/pets")
            } Then {
                statusCode(HttpStatus.BAD_REQUEST.code)
                body(
                    "message", equalTo("Bad Request"),
                    "_embedded.errors.size()", equalTo(3),
                    "_embedded.errors[0].message", allOf(
                        containsString("birthDate"),
                        containsString("must not be null")
                    ),
                    "_embedded.errors[1].message", allOf(
                        containsString("name"),
                        containsString("must not be blank")
                    ),
                    "_embedded.errors[2].message", allOf(
                        containsString("type"),
                        containsString("must not be blank")
                    )
                )
            }
        }

        @Test
        fun `Should not allow birth dates in the future`() {
            Given {
                header(getAuthorizationBearer())
                contentType(JSON)
                body("""
                    {
                        "type": "CAT",
                        "name": "Future",
                        "birth_date": "${LocalDate.now().plusYears(1)}"
                    }
                """.trimIndent())
            } When {
                port(embeddedServer.port)
                post("/api/pets")
            } Then {
                statusCode(HttpStatus.BAD_REQUEST.code)
                body(
                    "message", equalTo("Bad Request"),
                    "_embedded.errors.size()", equalTo(1),
                    "_embedded.errors[0].message", allOf(
                        containsString("birthDate"),
                        containsString("must be a date in the past or in the present")
                    ),
                )
            }
        }

        @Test
        fun `Should create a Pet without PetOwner`() {
            Given {
                header(getAuthorizationBearer())
                contentType(JSON)
                body("""
                    {
                        "type": "DOG",
                        "name": "Bimo",
                        "birth_date": "2020-03-21"
                    }
                """.trimIndent())
            } When {
                port(embeddedServer.port)
                post("/api/pets")
            } Then {
                statusCode(HttpStatus.CREATED.code)
                body(
                    "id", notNullValue(),
                    "type", equalTo("DOG"),
                    "name", equalTo("Bimo"),
                    "birth_date", equalTo("2020-03-21"),
                    "owner", nullValue()
                )
            }
        }

        @Test
        fun `Should create a Pet with a valid PetOwner`() {
            val ownerIdentityId = randomUUID()

            mockGetIdentityResponse(ownerIdentityId, HttpStatus.OK)

            Given {
                header(getAuthorizationBearer())
                contentType(JSON)
                body("""
                    {
                        "type": "DOG",
                        "name": "Java",
                        "birth_date": "2019-06-20",
                        "owner_identity_id": "$ownerIdentityId"
                    }
                """.trimIndent())
            } When {
                port(embeddedServer.port)
                post("/api/pets")
            } Then {
                statusCode(HttpStatus.CREATED.code)
                body(
                    "id", notNullValue(),
                    "type", equalTo("DOG"),
                    "name", equalTo("Java"),
                    "birth_date", equalTo("2019-06-20"),
                    "owner", notNullValue(),
                    "owner.id", equalTo(ownerIdentityId.toString()),
                    "owner.first_name", equalTo("John"),
                    "owner.last_name", equalTo("Doe")
                )
            }
        }

        @Test
        fun `Should return 400 Bad Request when creating a Pet with a non existing PetOwner`() {
            val ownerIdentityId = randomUUID()

            mockGetIdentityResponse(ownerIdentityId, HttpStatus.NOT_FOUND)

            Given {
                header(getAuthorizationBearer())
                contentType(JSON)
                body("""
                    {
                        "type": "DOG",
                        "name": "Scooby Doo",
                        "birth_date": "1980-01-01",
                        "owner_identity_id": "$ownerIdentityId"
                    }
                """.trimIndent())
            } When {
                port(embeddedServer.port)
                post("/api/pets")
            } Then {
                statusCode(HttpStatus.BAD_REQUEST.code)
                body(
                    "_embedded.errors.size()", equalTo(1),
                    "_embedded.errors[0].message", allOf(
                        containsString("Failed to create Pet"),
                        containsString("Could not load the PetOwner with ID $ownerIdentityId")
                    )
                )
            }
        }

        @ParameterizedTest
        @EnumSource(HttpStatus::class, mode = EnumSource.Mode.EXCLUDE, names = ["OK", "NOT_FOUND"])
        fun `Should return 503 Internal Server Error when fetching the PetOwner returns an unexpected error`(httpStatus: HttpStatus) {
            val ownerIdentityId = randomUUID()

            mockGetIdentityResponse(ownerIdentityId, httpStatus)

            Given {
                header(getAuthorizationBearer())
                contentType(JSON)
                body("""
                    {
                        "type": "CAT",
                        "name": "Marrameu",
                        "birth_date": "2020-01-01",
                        "owner_identity_id": "$ownerIdentityId"
                    }
                """.trimIndent())
            } When {
                port(embeddedServer.port)
                post("/api/pets")
            } Then {
                statusCode(HttpStatus.INTERNAL_SERVER_ERROR.code)
                body(
                    "_embedded.errors.size()", equalTo(1),
                    "_embedded.errors[0].message", allOf(
                        containsString("Failed loading PetOwner"),
                        containsString("HTTP call to Identity Service returned not expected response status")
                    )
                )
            }

        }

    }

}