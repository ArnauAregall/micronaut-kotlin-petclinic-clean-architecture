package tech.aaregall.lab.petclinic.pet.infrastructure.adapters.input.http

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
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.mockserver.model.Header
import org.mockserver.model.HttpRequest.request
import org.mockserver.model.HttpResponse.response
import org.mockserver.model.JsonBody.json
import tech.aaregall.lab.petclinic.pet.spec.MockServerSpec
import tech.aaregall.lab.petclinic.pet.spec.MockServerSpec.Companion.getMockServerClient
import java.util.UUID

@MicronautTest(transactional = false)
@TestResourcesProperties(providers = [MockServerSpec::class])
internal class PetControllerIT(private val embeddedServer: EmbeddedServer) {

    private fun mockGetIdentityResponse(identityId: UUID, httpStatus: HttpStatus) {
        getMockServerClient()
            .`when`(request().withMethod(GET.name).withPath("/api/identities/$identityId"))
            .respond {
                val response = response()
                    .withStatusCode(httpStatus.code)
                    .withHeaders(Header("Content-Type", "application/json"))

                return@respond when (httpStatus) {
                    HttpStatus.OK ->  response.withBody(json("{\"id\": \"$identityId\" }"))
                    else -> response
                }
            }
    }


    @Nested
    inner class CreatePet {

        @Test
        fun `Should create a Pet without PetOwner`() {
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
            val ownerIdentityId = UUID.randomUUID()

            mockGetIdentityResponse(ownerIdentityId, HttpStatus.OK)

            Given {
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
                    "owner.id", equalTo(ownerIdentityId.toString())
                )
            }
        }

        @Test
        fun `Should return 404 Not Found when creating a Pet with a non existing PetOwner`() {
            val ownerIdentityId = UUID.randomUUID()

            mockGetIdentityResponse(ownerIdentityId, HttpStatus.NOT_FOUND)

            Given {
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
                statusCode(HttpStatus.NOT_FOUND.code)
            }
        }

        @ParameterizedTest
        @EnumSource(HttpStatus::class, mode = EnumSource.Mode.EXCLUDE, names = ["OK", "NOT_FOUND"])
        fun `Should return 503 Internal Server Error when fetching the PetOwner returns an unexpected error`(httpStatus: HttpStatus) {
            val ownerIdentityId = UUID.randomUUID()

            mockGetIdentityResponse(ownerIdentityId, httpStatus)

            Given {
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
                log().all()
                body(
                    "_embedded.errors.size()", equalTo(1),
                    "_embedded.errors[0].message",
                    allOf(containsString("Failed loading PetOwner"), containsString("HTTP call to Identity Service returned not expected response status"))
                )
            }

        }

    }

}