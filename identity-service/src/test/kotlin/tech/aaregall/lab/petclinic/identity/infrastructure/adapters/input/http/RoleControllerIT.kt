package tech.aaregall.lab.petclinic.identity.infrastructure.adapters.input.http

import io.micronaut.http.HttpStatus
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.micronaut.test.extensions.testresources.annotation.TestResourcesProperties
import io.restassured.http.ContentType
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import tech.aaregall.lab.petclinic.identity.application.ports.input.CreateRoleCommand
import tech.aaregall.lab.petclinic.identity.application.ports.input.CreateRoleInputPort
import tech.aaregall.lab.petclinic.test.spec.keycloak.KeycloakPropsProvider
import tech.aaregall.lab.petclinic.test.spec.keycloak.KeycloakPropsProvider.Companion.getAuthorizationBearer
import java.time.Instant.now

@MicronautTest(transactional = false)
@TestResourcesProperties(providers = [KeycloakPropsProvider::class])
internal class RoleControllerIT(private val embeddedServer: EmbeddedServer) {

    @Nested
    inner class CreateRole {

        @Test
        fun `Should return Unauthorized when no Authorization header`() {
            Given {
                contentType(ContentType.JSON)
                body("""
                    {
                        "name": "New role"
                    }
                """.trimIndent())
            } When {
                port(embeddedServer.port)
                post("/api/roles")
            } Then {
                statusCode(HttpStatus.UNAUTHORIZED.code)
                body("message", equalTo("Unauthorized"))
            }
        }

        @Test
        fun `Should return 400 Bad Request when name is blank`() {
            Given {
                contentType(ContentType.JSON)
                body("""
                    {
                        "name": ""
                    }
                """.trimIndent())
                header(getAuthorizationBearer())
            } When {
                port(embeddedServer.port)
                post("/api/roles")
            } Then {
                statusCode(HttpStatus.BAD_REQUEST.code)
                body(
                    "message", equalTo("Bad Request"),
                    "_embedded.errors.size()", equalTo(1),
                    "_embedded.errors[0].message", allOf(
                        containsString("name"), containsString("must not be blank")
                    )
                )
            }
        }

        @Test
        fun `Should return 400 Bad Request when a Role with the same name already exists no matter case`(createRoleInputPort: CreateRoleInputPort) {
            val role = createRoleInputPort.createRole(CreateRoleCommand("Admin"))

            Given {
                contentType(ContentType.JSON)
                body("""
                    {
                        "name": "${role.name.uppercase()}"
                    }
                """.trimIndent())
                header(getAuthorizationBearer())
            } When {
                port(embeddedServer.port)
                post("/api/roles")
            } Then {
                statusCode(HttpStatus.BAD_REQUEST.code)
            }
        }

        @Test
        fun `Should return 201 Created when the Role name is not blank and no other role with the same name exists`() {
            val roleName = "NEW_ROLE_${now().toEpochMilli()}"

            Given {
                contentType(ContentType.JSON)
                body("""
                    {
                        "name": "$roleName"
                    }
                """.trimIndent())
                header(getAuthorizationBearer())
            } When {
                port(embeddedServer.port)
                post("/api/roles")
            } Then {
                statusCode(HttpStatus.CREATED.code)
                body(
                    "id", notNullValue(),
                    "name", equalTo(roleName),
                )
            }
        }

    }

}