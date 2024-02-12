package tech.aaregall.lab.petclinic.identity.infrastructure.adapters.input.http

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
import tech.aaregall.lab.petclinic.identity.application.ports.input.AssignRoleToIdentityCommand
import tech.aaregall.lab.petclinic.identity.application.ports.input.AssignRoleToIdentityUseCase
import tech.aaregall.lab.petclinic.identity.application.ports.input.CreateIdentityCommand
import tech.aaregall.lab.petclinic.identity.application.ports.input.CreateIdentityUseCase
import tech.aaregall.lab.petclinic.identity.application.ports.input.CreateRoleCommand
import tech.aaregall.lab.petclinic.identity.application.ports.input.CreateRoleUseCase
import tech.aaregall.lab.petclinic.identity.application.ports.input.UpdateIdentityContactDetailsCommand
import tech.aaregall.lab.petclinic.identity.application.ports.input.UpdateIdentityContactDetailsUseCase
import tech.aaregall.lab.petclinic.test.spec.keycloak.KeycloakSpec
import tech.aaregall.lab.petclinic.test.spec.keycloak.KeycloakSpec.Companion.getAuthorizationBearer
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
    inner class LoadIdentity(
        private val createIdentityUseCase: CreateIdentityUseCase,
        private val updateIdentityContactDetailsUseCase: UpdateIdentityContactDetailsUseCase) {

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
        fun `Should return OK with null ContactDetails when Identity exists and does not have ContactDetails`() {
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

        @Test
        fun `Should return OK with ContactDetails when Identity exists and has ContactDetails`() {
            val identity = createIdentityUseCase.createIdentity(CreateIdentityCommand(firstName = "Foo", lastName = "Bar"))
            val contactDetails = updateIdentityContactDetailsUseCase.updateIdentityContactDetails(
                UpdateIdentityContactDetailsCommand(identityId = identity.id, email = "foo.bar@test.com", phoneNumber = "123 456 789")
            )

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
                    "contact_details", notNullValue(),
                    "contact_details.email", equalTo(contactDetails.email),
                    "contact_details.phone_number", equalTo(contactDetails.phoneNumber)
                )
            }
        }

        @Test
        fun `Should return OK with null Roles when Identity exists and does not have Roles`() {
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
                    "roles", nullValue()
                )
            }
        }

        @Test
        fun `Should return OK with Roles sorted alphabetically when Identity exists and has Roles assigned`(
            createIdentityUseCase: CreateIdentityUseCase,
            createRoleUseCase: CreateRoleUseCase,
            assignRoleToIdentityUseCase: AssignRoleToIdentityUseCase
        ) {

            val identity = createIdentityUseCase.createIdentity(CreateIdentityCommand(firstName = "Foo", lastName = "Bar"))

            val role1 = createRoleUseCase.createRole(CreateRoleCommand("Warrior"))
            val role2 = createRoleUseCase.createRole(CreateRoleCommand("Knight"))
            val role3 = createRoleUseCase.createRole(CreateRoleCommand("Paladin"))

            setOf(role1, role2, role3).forEach {
                assignRoleToIdentityUseCase.assignRoleToIdentity(
                    AssignRoleToIdentityCommand(identityId = identity.id, roleId = it.id))
            }

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
                    "roles.size()", equalTo(3),
                    "roles[0]", equalTo("Knight"),
                    "roles[1]", equalTo("Paladin"),
                    "roles[2]", equalTo("Warrior")
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

    @Nested
    inner class DeleteIdentity(private val createIdentityUseCase: CreateIdentityUseCase) {

        @Test
        fun `Should return Unauthorized when no Authorization header`() {
            Given {
                pathParam("id", UUID.randomUUID())
            } When {
                port(embeddedServer.port)
                delete("/api/identities/{id}")
            } Then {
                statusCode(HttpStatus.UNAUTHORIZED.code)
                body("message", equalTo("Unauthorized"))
            }
        }

        @Test
        fun `Should return Bad Request when Identity does not exist`() {
            Given {
                pathParam("id", UUID.randomUUID())
                header(getAuthorizationBearer())
            } When {
                port(embeddedServer.port)
                delete("/api/identities/{id}")
            } Then {
                statusCode(HttpStatus.BAD_REQUEST.code)
            }
        }

        @Test
        fun `Should return No Content when Identity exists`() {
            val identity = createIdentityUseCase.createIdentity(
                CreateIdentityCommand(firstName = "John", lastName = "Doe")
            )

            Given {
                pathParam("id", identity.id.toString())
                header(getAuthorizationBearer())
            } When {
                port(embeddedServer.port)
                delete("/api/identities/{id}")
            } Then {
                statusCode(HttpStatus.NO_CONTENT.code)
            }
        }

    }

}