package tech.aaregall.lab.micronaut.petclinic.identity.infrastructure.adapters.input.web

import io.micronaut.http.HttpStatus
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.micronaut.test.extensions.testresources.annotation.TestResourcesProperties
import io.restassured.http.ContentType
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric
import org.apache.commons.lang3.RandomStringUtils.randomNumeric
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.notNullValue
import org.hamcrest.Matchers.nullValue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import tech.aaregall.lab.micronaut.petclinic.identity.application.ports.input.CreateIdentityCommand
import tech.aaregall.lab.micronaut.petclinic.identity.application.ports.input.CreateIdentityUseCase
import tech.aaregall.lab.micronaut.petclinic.identity.spec.KeycloakSpec
import tech.aaregall.lab.micronaut.petclinic.identity.spec.KeycloakSpec.Companion.getAuthorizationBearer
import java.util.UUID

@MicronautTest(transactional = false)
@TestResourcesProperties(providers = [KeycloakSpec::class])
internal class IdentityControllerIT(private val embeddedServer: EmbeddedServer) {

    @Nested
    inner class CreateIdentity {

        @Test
        fun `Should return Unauthorized when no Authorization header`() {
            Given {
                contentType(ContentType.JSON)
                body("""
                    {
                        "first_name": "John",
                        "last_name": "Doe"
                    }
                """.trimIndent())
            } When {
                port(embeddedServer.port)
                post("/api/identities")
            } Then {
                statusCode(HttpStatus.UNAUTHORIZED.code)
                body("message", equalTo("Unauthorized"))
            }
        }

        @Test
        fun `Should create and Identity`() {
            Given {
                contentType(ContentType.JSON)
                body("""
                    {
                        "first_name": "John",
                        "last_name": "Doe"
                    }
                """.trimIndent())
                header(getAuthorizationBearer())
            } When {
                port(embeddedServer.port)
                post("/api/identities")
            } Then {
                statusCode(HttpStatus.CREATED.code)
                body(
                    "id", notNullValue(),
                    "first_name", equalTo("John"),
                    "last_name", equalTo("Doe"),
                    "contact_details", nullValue()
                )
            }
        }

        @Test
        fun `Should not allow blank fields`() {
            Given {
                contentType(ContentType.JSON)
                body("""
                    {
                        "first_name": "",
                        "last_name": ""
                    }
                """.trimIndent())
                header(getAuthorizationBearer())
            } When {
                port(embeddedServer.port)
                post("/api/identities")
            } Then {
                statusCode(HttpStatus.BAD_REQUEST.code)
                body(
                    "message", equalTo("Bad Request"),
                    "_embedded.errors.size()", equalTo(2),
                    "_embedded.errors[0].message", allOf(
                        containsString("firstName"), containsString("must not be blank")),
                    "_embedded.errors[1].message", allOf(
                        containsString("lastName"), containsString("must not be blank"))
                )
            }
        }

    }

    @Nested
    inner class LoadIdentity {

        @Test
        fun `Should return Unauthorized when no Authorization header`() {
            Given {
                pathParam("id", UUID.randomUUID())
            } When {
                port(embeddedServer.port)
                get("/api/identities/{id}")
            } Then {
                statusCode(HttpStatus.UNAUTHORIZED.code)
                body("message", equalTo("Unauthorized"))
            }
        }

        @Test
        fun `Should return Bad Request when ID is not a UUID`() {
            Given {
                pathParam("id", "something")
                header(getAuthorizationBearer())
            } When {
                port(embeddedServer.port)
                get("/api/identities/{id}")
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
        fun `Should return Not Found when Identity does not exist`() {
            Given {
                pathParam("id", UUID.randomUUID())
                header(getAuthorizationBearer())
            } When {
                port(embeddedServer.port)
                get("/api/identities/{id}")
            } Then {
                statusCode(HttpStatus.NOT_FOUND.code)
            }
        }

        @Test
        fun `Should return OK when Identity exists`(createIdentityUseCase: CreateIdentityUseCase) {
            val identity = createIdentityUseCase.createIdentity(CreateIdentityCommand(firstName = "Foo", lastName = "Bar"))
            Given {
                pathParam("id", identity.id.toString())
                header(getAuthorizationBearer())
            } When {
                port(embeddedServer.port)
                get("/api/identities/{id}")
            } Then {
                statusCode(HttpStatus.OK.code)
                body(
                    "id", equalTo(identity.id.toString()) ,
                    "first_name", equalTo(identity.firstName),
                    "last_name", equalTo(identity.lastName),
                    "contact_details", nullValue()
                )
            }
        }

    }

    @Nested
    inner class UpdateIdentityContactDetails(private val createIdentityUseCase: CreateIdentityUseCase) {

        @Test
        fun `Should return Unauthorized when no Authorization header`() {
            Given {
                pathParam("id", UUID.randomUUID())
                contentType(ContentType.JSON)
                body("""
                    {
                        "email": "foo.bar@test.com",
                        "phone_number": "+34 123 456 789"
                    }
                """.trimIndent())
            } When {
                port(embeddedServer.port)
                patch("/api/identities/{id}/contact-details")
            } Then {
                statusCode(HttpStatus.UNAUTHORIZED.code)
                body("message", equalTo("Unauthorized"))
            }
        }

        @Test
        fun `Should return Bad Request when ID is not a UUID`() {
            Given {
                pathParam("id", "something")
                contentType(ContentType.JSON)
                body("""
                    {
                        "email": "foo.bar@test.com",
                        "phone_number": "+34 123 456 789"
                    }
                """.trimIndent())
                header(getAuthorizationBearer())
            } When {
                port(embeddedServer.port)
                patch("/api/identities/{id}/contact-details")
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
        fun `Should return Bad Request when Identity does not exist`() {
            Given {
                pathParam("id", UUID.randomUUID())
                contentType(ContentType.JSON)
                body("""
                    {
                        "email": "foo.bar@test.com",
                        "phone_number": "+34 123 456 789"
                    }
                """.trimIndent())
                header(getAuthorizationBearer())
            } When {
                port(embeddedServer.port)
                patch("/api/identities/{id}/contact-details")
            } Then {
                statusCode(HttpStatus.BAD_REQUEST.code)
            }
        }

        @Test
        fun `Should not allow blank fields`() {
            val identityId = createIdentityUseCase.createIdentity(CreateIdentityCommand("Foo", "Bar")).id

            Given {
                pathParam("id", identityId.toString())
                contentType(ContentType.JSON)
                body("""
                    {
                        "email": "",
                        "phone_number": ""
                    }
                """.trimIndent())
                header(getAuthorizationBearer())
            } When {
                port(embeddedServer.port)
                patch("/api/identities/{id}/contact-details")
            } Then {
                statusCode(HttpStatus.BAD_REQUEST.code)
                body(
                    "message", equalTo("Bad Request"),
                    "_embedded.errors.size()", equalTo(2),
                    "_embedded.errors[0].message", allOf(
                        containsString("email"), containsString("must not be blank")),
                    "_embedded.errors[1].message", allOf(
                        containsString("phoneNumber"), containsString("must not be blank"))
                )
            }
        }

        @Test
        fun `Should not allow over-sized length fields`() {
            val identityId = createIdentityUseCase.createIdentity(CreateIdentityCommand("Foo", "Bar")).id

            Given {
                pathParam("id", identityId.toString())
                contentType(ContentType.JSON)
                body("""
                    {
                        "email": "${randomAlphanumeric(101)}",
                        "phone_number": "${randomNumeric(21)}"
                    }
                """.trimIndent())
                header(getAuthorizationBearer())
            } When {
                port(embeddedServer.port)
                patch("/api/identities/{id}/contact-details")
            } Then {
                statusCode(HttpStatus.BAD_REQUEST.code)
                body(
                    "message", equalTo("Bad Request"),
                    "_embedded.errors.size()", equalTo(2),
                    "_embedded.errors[0].message", allOf(
                        containsString("email"), containsString("too long"), containsString("max 100 characters")),
                    "_embedded.errors[1].message", allOf(
                        containsString("phoneNumber"), containsString("too long"), containsString("max 20 characters"))
                )
            }
        }

        @Test
        fun `Should return No Content when Contact Details are successfully updated`() {
            val identityId = createIdentityUseCase.createIdentity(CreateIdentityCommand("John", "Doe")).id

            Given {
                pathParam("id", identityId.toString())
                contentType(ContentType.JSON)
                body("""
                    {
                        "email": "john.doe@company.test",
                        "phone_number": "+34 111 222 333"
                    }
                """.trimIndent())
                header(getAuthorizationBearer())
            } When {
                port(embeddedServer.port)
                patch("/api/identities/{id}/contact-details")
            } Then {
                statusCode(HttpStatus.NO_CONTENT.code)
            }
        }

    }

}