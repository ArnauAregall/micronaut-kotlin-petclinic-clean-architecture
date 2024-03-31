package tech.aaregall.lab.petclinic.vet.infrastructure.adapters.input.http

import io.micronaut.context.annotation.Value
import io.micronaut.data.jdbc.runtime.JdbcOperations
import io.micronaut.http.HttpMethod.GET
import io.micronaut.http.HttpStatus.BAD_REQUEST
import io.micronaut.http.HttpStatus.CREATED
import io.micronaut.http.HttpStatus.OK
import io.micronaut.http.HttpStatus.UNAUTHORIZED
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.micronaut.test.extensions.testresources.annotation.TestResourcesProperties
import io.restassured.http.ContentType.JSON
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockserver.client.MockServerClient
import org.mockserver.model.HttpRequest.request
import org.mockserver.model.HttpResponse.response
import org.mockserver.model.JsonBody.json
import tech.aaregall.lab.petclinic.testresources.keycloak.KeycloakFixture.Companion.getAuthorizationBearer
import tech.aaregall.lab.petclinic.testresources.keycloak.KeycloakPropsProvider
import tech.aaregall.lab.petclinic.testresources.mockserver.MockServerPropsProvider
import tech.aaregall.lab.petclinic.vet.application.ports.input.CreateSpecialityCommand
import tech.aaregall.lab.petclinic.vet.application.ports.input.CreateSpecialityInputPort
import tech.aaregall.lab.petclinic.vet.application.ports.input.CreateVetCommand
import tech.aaregall.lab.petclinic.vet.application.ports.input.CreateVetInputPort
import tech.aaregall.lab.petclinic.vet.domain.model.Vet
import java.time.Instant
import java.time.Instant.now
import java.util.Collections.nCopies
import java.util.UUID
import java.util.UUID.nameUUIDFromBytes
import java.util.UUID.randomUUID

@MicronautTest(transactional = false)
@TestResourcesProperties(providers = [KeycloakPropsProvider::class, MockServerPropsProvider::class])
internal class VetControllerIT(
    private val embeddedServer: EmbeddedServer,
    private val jdbc: JdbcOperations,
    private val mockServerClient: MockServerClient) {

    @Value("\${app.ports.output.vet-id-validation.required-identity-role-name}")
    lateinit var requiredIdentityRoleName: String

    @BeforeEach
    fun setUp() {
        jdbc.execute { conn -> conn.prepareStatement("""
            TRUNCATE TABLE vet CASCADE;
            TRUNCATE TABLE speciality CASCADE;
        """.trimIndent()).execute() }
    }

    fun givenValidIdentityId(identityId: UUID = randomUUID()) =
        identityId.also {
            mockServerClient
                .`when`(request().withMethod(GET.name).withPath("/api/identities/$it"))
                .respond(
                    response().withStatusCode(OK.code).withBody(
                        json("""
                            {"id": "$it", "roles": ["$requiredIdentityRoleName"]}
                            """.trimIndent()
                        )
                    )
                )
        }

    @Nested
    inner class SearchVets {

        @Test
        fun `Should return Unauthorized when no Authorization header`() {
            Given {
                contentType(JSON)
                queryParams(mapOf("page" to "0", "size" to "20"))
            } When {
                port(embeddedServer.port)
                get("/api/vets")
            } Then {
                statusCode(UNAUTHORIZED.code)
                body("message", equalTo("Unauthorized"))
            }
        }

        @Test
        fun `Should return 200 OK with empty content array when there are no Specialities`() {
            Given {
                contentType(JSON)
                queryParams(mapOf("page" to "0", "size" to "20"))
                header(getAuthorizationBearer())
            } When {
                port(embeddedServer.port)
                get("/api/vets")
            } Then {
                statusCode(OK.code)
                body(
                    "content.size()", equalTo(0),
                    "totalSize", equalTo(0)
                )
            }
        }

        @Test
        fun `Should return 200 OK with paginated Vets content when there are Vets`(
            createVetInputPort: CreateVetInputPort,
            createSpecialityInputPort: CreateSpecialityInputPort) {

            val specialities = (1..3).map {
                createSpecialityInputPort.createSpeciality(
                    CreateSpecialityCommand(
                        name = "Speciality $it", description = "Description for Speciality $it"
                    )
                )
            }.toList()

            val vetsWithTimestamps: Map<Instant, Vet> = (0..40).map { index ->
                CreateVetCommand(
                    identityId = nameUUIDFromBytes(index.toString().toByteArray()),
                    specialitiesIds = specialities.map { speciality -> speciality.id })
            }.map {
                givenValidIdentityId(it.identityId)
                it
            }.associate {
                now().plusMillis(100) to createVetInputPort.createVet(it)
            }

            fun assertPage(page: Int, size: Int, expectedRange: IntRange) {
                fun sortedSlice(range: IntRange) = vetsWithTimestamps.entries.sortedBy { it.key }.map { it.value }.slice(range)

                Given {
                    contentType(JSON)
                    queryParams(mapOf("page" to page, "size" to size))
                    header(getAuthorizationBearer())
                } When {
                    port(embeddedServer.port)
                    get("/api/vets")
                } Then {
                    statusCode(OK.code)
                    val expectedSlice = sortedSlice(expectedRange)
                    body(
                        "content.size()", equalTo(expectedSlice.size),
                        "content.id", containsInAnyOrder(
                            *expectedSlice.map { it.id.toString() }.toTypedArray()
                        ),
                        "content.specialities.id.flatten()", containsInAnyOrder(
                            *specialities.map { it.id.toString() }.flatMap { nCopies(expectedSlice.size, it) }.toTypedArray()
                        ),
                        "totalSize", equalTo(vetsWithTimestamps.size)
                    )
                }
            }

            assertPage(1, 20, 0..19)
            assertPage(2, 20, 20..39)
        }

    }

    @Nested
    inner class CreateVet {

        @Test
        fun `Should return Unauthorized when no Authorization header`() {
            Given {
                contentType(JSON)
                body("""
                    {
                        "identity_id": "${randomUUID()}",
                        "specialities_ids": ["${randomUUID()}", "${randomUUID()}"]
                    }
                """.trimIndent())
            } When {
                port(embeddedServer.port)
                post("/api/vets")
            } Then {
                statusCode(UNAUTHORIZED.code)
                body("message", equalTo("Unauthorized"))
            }
        }

        @Test
        fun `Should not allow null Identity ID`() {
            Given {
                contentType(JSON)
                body("""
                    {
                        "identity_id": null,
                        "specialities_ids": ["${randomUUID()}", "${randomUUID()}"]
                    }
                """.trimIndent())
                header(getAuthorizationBearer())
            } When {
                port(embeddedServer.port)
                post("/api/vets")
            } Then {
                statusCode(BAD_REQUEST.code)
                body(
                    "message", equalTo("Bad Request"),
                    "_embedded.errors.size()", equalTo(1),
                    "_embedded.errors[0].message", containsString("Vet Identity ID is required")
                )
            }
        }

        @Test
        fun `Should not allow blank Identity ID`() {
            Given {
                contentType(JSON)
                body("""
                    {
                        "identity_id": "",
                        "specialities_ids": ["${randomUUID()}", "${randomUUID()}"]
                    }
                """.trimIndent())
                header(getAuthorizationBearer())
            } When {
                port(embeddedServer.port)
                post("/api/vets")
            } Then {
                statusCode(BAD_REQUEST.code)
                body(
                    "message", equalTo("Bad Request"),
                    "_embedded.errors.size()", equalTo(1),
                    "_embedded.errors[0].message", containsString("Invalid UUID string")
                )
            }
        }

        @Test
        fun `Should not allow empty Specialities IDs`() {
            Given {
                contentType(JSON)
                body("""
                    {
                        "identity_id": "${randomUUID()}",
                        "specialities_ids": []
                    }
                """.trimIndent())
                header(getAuthorizationBearer())
            } When {
                port(embeddedServer.port)
                post("/api/vets")
            } Then {
                statusCode(BAD_REQUEST.code)
                body(
                    "message", equalTo("Bad Request"),
                    "_embedded.errors.size()", equalTo(1),
                    "_embedded.errors[0].message", containsString("Specialities IDs cannot be empty")
                )
            }
        }

        @Test
        fun `Should return not accept an Identity ID that is from an existing Vet`(
            createVetInputPort: CreateVetInputPort,
            createSpecialityInputPort: CreateSpecialityInputPort) {

            val identityId = givenValidIdentityId()

            createSpecialityInputPort.createSpeciality(CreateSpecialityCommand(name = "Dermatology"))
                .also { speciality ->
                    createVetInputPort.createVet(
                        CreateVetCommand(identityId = identityId, specialitiesIds = setOf(speciality.id))
                    )
                }

            Given {
                contentType(JSON)
                body("""
                    {
                        "identity_id": "$identityId",
                        "specialities_ids": ["${randomUUID()}", "${randomUUID()}"]
                    }
                """.trimIndent())
                header(getAuthorizationBearer())
            } When {
                port(embeddedServer.port)
                post("/api/vets")
            } Then {
                statusCode(BAD_REQUEST.code)
                body(
                    "message", equalTo("Bad Request"),
                    "_embedded.errors.size()", equalTo(1),
                    "_embedded.errors[0].message", allOf(
                        containsString("Failed to create Vet"),
                        containsString("Vet with Identity ID"),
                        containsString(identityId.toString()),
                        containsString("already exists")
                    )
                )
            }
        }

        @Test
        fun `Should return not accept an Identity ID that is not a valid Vet ID`() {
            val identityId = randomUUID()

            mockServerClient
                .`when`(request().withMethod(GET.name).withPath("/api/identities/$identityId"))
                .respond(
                    response().withStatusCode(OK.code).withBody(
                        json("""
                            {"id": "$identityId", "roles": ["NOT_A_VET"]}
                            """.trimIndent()
                        )
                    )
                )

            Given {
                contentType(JSON)
                body("""
                    {
                        "identity_id": "$identityId",
                        "specialities_ids": ["${randomUUID()}", "${randomUUID()}"]
                    }
                """.trimIndent())
                header(getAuthorizationBearer())
            } When {
                port(embeddedServer.port)
                post("/api/vets")
            } Then {
                statusCode(BAD_REQUEST.code)
                body(
                    "message", equalTo("Bad Request"),
                    "_embedded.errors.size()", equalTo(1),
                    "_embedded.errors[0].message", allOf(
                        containsString("Failed to create Vet"),
                        containsString("Vet ID"),
                        containsString(identityId.toString()),
                        containsString("is not valid")
                    )
                )
            }
        }

        @Test
        fun `Should return not accept a non existing Speciality ID`() {
            val specialityId = randomUUID()

            Given {
                contentType(JSON)
                body("""
                    {
                        "identity_id": "${givenValidIdentityId()}",
                        "specialities_ids": ["$specialityId"]
                    }
                """.trimIndent())
                header(getAuthorizationBearer())
            } When {
                port(embeddedServer.port)
                post("/api/vets")
            } Then {
                statusCode(BAD_REQUEST.code)
                body(
                    "message", equalTo("Bad Request"),
                    "_embedded.errors.size()", equalTo(1),
                    "_embedded.errors[0].message", allOf(
                        containsString("Failed to create Vet"),
                        containsString("Speciality"),
                        containsString(specialityId.toString()),
                        containsString("does not exist")
                    )
                )
            }
        }

        @Test
        fun `Should return 201 Created with the expected Vet response when Identity and Specialities IDs are valid`(createSpecialityInputPort: CreateSpecialityInputPort) {
            val speciality1 = createSpecialityInputPort.createSpeciality(CreateSpecialityCommand(name = "Dermatology"))
            val speciality2 = createSpecialityInputPort.createSpeciality(CreateSpecialityCommand(name = "Cardiology"))

            val identityId = givenValidIdentityId( )

            Given {
                contentType(JSON)
                body("""
                    {
                        "identity_id": "$identityId",
                        "specialities_ids": ["${speciality1.id}", "${speciality2.id}"]
                    }
                """.trimIndent())
                header(getAuthorizationBearer())
            } When {
                port(embeddedServer.port)
                post("/api/vets")
            } Then {
                statusCode(CREATED.code)
                body(
                    "id", equalTo(identityId.toString()),
                    "specialities.size()", equalTo(2),
                    "specialities.id", containsInAnyOrder(speciality1.id.toString(), speciality2.id.toString())
                )
            }
        }

    }

}