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
import tech.aaregall.lab.petclinic.identity.application.ports.input.AssignRoleToIdentityInputPort
import tech.aaregall.lab.petclinic.identity.application.ports.input.CreateIdentityCommand
import tech.aaregall.lab.petclinic.identity.application.ports.input.CreateIdentityInputPort
import tech.aaregall.lab.petclinic.identity.application.ports.input.CreateRoleCommand
import tech.aaregall.lab.petclinic.identity.application.ports.input.CreateRoleInputPort
import tech.aaregall.lab.petclinic.identity.application.ports.input.UpdateIdentityContactDetailsCommand
import tech.aaregall.lab.petclinic.identity.application.ports.input.UpdateIdentityContactDetailsInputPort
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
        private val createIdentityInputPort: CreateIdentityInputPort,
        private val updateIdentityContactDetailsInputPort: UpdateIdentityContactDetailsInputPort) {

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
            val identity = createIdentityInputPort.createIdentity(CreateIdentityCommand(firstName = "Foo", lastName = "Bar"))
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
            val identity = createIdentityInputPort.createIdentity(CreateIdentityCommand(firstName = "Foo", lastName = "Bar"))
            val contactDetails = updateIdentityContactDetailsInputPort.updateIdentityContactDetails(
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
            val identity = createIdentityInputPort.createIdentity(CreateIdentityCommand(firstName = "Foo", lastName = "Bar"))
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
            createIdentityInputPort: CreateIdentityInputPort,
            createRoleInputPort: CreateRoleInputPort,
            assignRoleToIdentityInputPort: AssignRoleToIdentityInputPort
        ) {

            val identity = createIdentityInputPort.createIdentity(CreateIdentityCommand(firstName = "Foo", lastName = "Bar"))

            val role1 = createRoleInputPort.createRole(CreateRoleCommand("Warrior"))
            val role2 = createRoleInputPort.createRole(CreateRoleCommand("Knight"))
            val role3 = createRoleInputPort.createRole(CreateRoleCommand("Paladin"))

            setOf(role1, role2, role3).forEach {
                assignRoleToIdentityInputPort.assignRoleToIdentity(
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
    inner class UpdateIdentityContactDetails(private val createIdentityInputPort: CreateIdentityInputPort) {

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
            val identityId = createIdentityInputPort.createIdentity(CreateIdentityCommand("Foo", "Bar")).id

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
            val identityId = createIdentityInputPort.createIdentity(CreateIdentityCommand("Foo", "Bar")).id

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
            val identityId = createIdentityInputPort.createIdentity(CreateIdentityCommand("John", "Doe")).id

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
    inner class AssignRoleToIdentity(private val createIdentityInputPort: CreateIdentityInputPort) {

        @Test
        fun `Should return Unauthorized when no Authorization header`() {
            Given {
                pathParam("id", UUID.randomUUID())
                contentType(ContentType.JSON)
                body("""
                    {
                        "role_id": "${UUID.randomUUID()}"
                    }
                """.trimIndent())
            } When {
                port(embeddedServer.port)
                put("/api/identities/{id}/role")
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
                        "role_id": "${UUID.randomUUID()}"
                    }
                """.trimIndent())
                header(getAuthorizationBearer())
            } When {
                port(embeddedServer.port)
                put("/api/identities/{id}/role")
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
                        "role_id": "${UUID.randomUUID()}"
                    }
                """.trimIndent())
                header(getAuthorizationBearer())
            } When {
                port(embeddedServer.port)
                put("/api/identities/{id}/role")
            } Then {
                statusCode(HttpStatus.BAD_REQUEST.code)
            }
        }

        @Test
        fun `Should return Bad Request when Role does not exist`() {
            val identity = createIdentityInputPort.createIdentity(
                CreateIdentityCommand(firstName = "Albert", lastName = "Almond")
            )

            Given {
                pathParam("id", identity.id.toString())
                contentType(ContentType.JSON)
                body("""
                    {
                        "role_id": "${UUID.randomUUID()}"
                    }
                """.trimIndent())
                header(getAuthorizationBearer())
            } When {
                port(embeddedServer.port)
                put("/api/identities/{id}/role")
            } Then {
                statusCode(HttpStatus.BAD_REQUEST.code)
            }
        }

        @Test
        fun `Should return 409 Conflict when Identity already has the Role assigned`(
            createRoleInputPort: CreateRoleInputPort,
            assignRoleToIdentityInputPort: AssignRoleToIdentityInputPort
        ) {
            val identity = createIdentityInputPort.createIdentity(
                CreateIdentityCommand(firstName = "Bill", lastName = "Banana")
            )

            val role = createRoleInputPort.createRole(CreateRoleCommand(name = "Role for ${identity.id}"))

            assignRoleToIdentityInputPort.assignRoleToIdentity(
                AssignRoleToIdentityCommand(identityId = identity.id, roleId = role.id)
            )

            Given {
                pathParam("id", identity.id.toString())
                contentType(ContentType.JSON)
                body("""
                    {
                        "role_id": "${role.id}"
                    }
                """.trimIndent())
                header(getAuthorizationBearer())
            } When {
                port(embeddedServer.port)
                put("/api/identities/{id}/role")
            } Then {
                statusCode(HttpStatus.CONFLICT.code)
            }
        }

        @Test
        fun `Should return 200 OK when Identity does not have the Role assigned`(createRoleInputPort: CreateRoleInputPort) {
            val identity = createIdentityInputPort.createIdentity(
                CreateIdentityCommand(firstName = "Catherine", lastName = "Carrot")
            )

            val role = createRoleInputPort.createRole(CreateRoleCommand(name = "Role for ${identity.id}"))

            Given {
                pathParam("id", identity.id.toString())
                contentType(ContentType.JSON)
                body("""
                    {
                        "role_id": "${role.id}"
                    }
                """.trimIndent())
                header(getAuthorizationBearer())
            } When {
                port(embeddedServer.port)
                put("/api/identities/{id}/role")
            } Then {
                statusCode(HttpStatus.OK.code)
            }
        }

    }

    @Nested
    inner class RevokeRoleFromIdentity(private val createIdentityInputPort: CreateIdentityInputPort) {

        @Test
        fun `Should return Unauthorized when no Authorization header`() {
            Given {
                pathParam("id", UUID.randomUUID())
                pathParam("roleId", UUID.randomUUID())
            } When {
                port(embeddedServer.port)
                delete("/api/identities/{id}/role/{roleId}")
            } Then {
                statusCode(HttpStatus.UNAUTHORIZED.code)
                body("message", equalTo("Unauthorized"))
            }
        }

        @Test
        fun `Should return Bad Request when Identity ID is not a UUID`() {
            Given {
                pathParam("id", "something")
                pathParam("roleId", UUID.randomUUID())
                header(getAuthorizationBearer())
            } When {
                port(embeddedServer.port)
                delete("/api/identities/{id}/role/{roleId}")
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
        fun `Should return Bad Request when Role ID is not a UUID`() {
            Given {
                pathParam("id", UUID.randomUUID())
                pathParam("roleId", "something")
                header(getAuthorizationBearer())
            } When {
                port(embeddedServer.port)
                delete("/api/identities/{id}/role/{roleId}")
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
                pathParam("roleId", UUID.randomUUID())
                contentType(ContentType.JSON)
                header(getAuthorizationBearer())
            } When {
                port(embeddedServer.port)
                delete("/api/identities/{id}/role/{roleId}")
            } Then {
                statusCode(HttpStatus.BAD_REQUEST.code)
            }
        }

        @Test
        fun `Should return Bad Request when Role does not exist`() {
            val identity = createIdentityInputPort.createIdentity(
                CreateIdentityCommand(firstName = "Amelia", lastName = "Apricot")
            )

            Given {
                pathParam("id", identity.id.toString())
                pathParam("roleId", UUID.randomUUID())
                contentType(ContentType.JSON)
                header(getAuthorizationBearer())
            } When {
                port(embeddedServer.port)
                delete("/api/identities/{id}/role/{roleId}")
            } Then {
                statusCode(HttpStatus.BAD_REQUEST.code)
            }
        }

        @Test
        fun `Should return 409 Conflict when Identity does not have the Role assigned`(createRoleInputPort: CreateRoleInputPort) {
            val identity = createIdentityInputPort.createIdentity(
                CreateIdentityCommand(firstName = "Eric", lastName = "Egg")
            )

            val role = createRoleInputPort.createRole(CreateRoleCommand(name = "A new Role ${identity.id}"))

            Given {
                pathParam("id", identity.id.toString())
                pathParam("roleId", role.id.toString())
                header(getAuthorizationBearer())
            } When {
                port(embeddedServer.port)
                delete("/api/identities/{id}/role/{roleId}")
            } Then {
                statusCode(HttpStatus.CONFLICT.code)
            }
        }

        @Test
        fun `Should return 204 No Content when Identity has the Role assigned`(
            createRoleInputPort: CreateRoleInputPort,
            assignRoleToIdentityInputPort: AssignRoleToIdentityInputPort) {

            val identity = createIdentityInputPort.createIdentity(
                CreateIdentityCommand(firstName = "Daisy", lastName = "Doughnut")
            )

            val role = createRoleInputPort.createRole(CreateRoleCommand(name = "Role for ${identity.id}"))

            assignRoleToIdentityInputPort.assignRoleToIdentity(
                AssignRoleToIdentityCommand(identityId = identity.id, roleId = role.id)
            )

            Given {
                pathParam("id", identity.id.toString())
                pathParam("roleId", role.id.toString())
                header(getAuthorizationBearer())
            } When {
                port(embeddedServer.port)
                delete("/api/identities/{id}/role/{roleId}")
            } Then {
                statusCode(HttpStatus.NO_CONTENT.code)
            }
        }

    }

    @Nested
    inner class DeleteIdentity(private val createIdentityInputPort: CreateIdentityInputPort) {

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
            val identity = createIdentityInputPort.createIdentity(
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