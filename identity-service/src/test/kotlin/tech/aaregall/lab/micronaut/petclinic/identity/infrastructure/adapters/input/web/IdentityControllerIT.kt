package tech.aaregall.lab.micronaut.petclinic.identity.infrastructure.adapters.input.web

import io.micronaut.http.HttpStatus
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.restassured.http.ContentType
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import jakarta.inject.Inject
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@MicronautTest
class IdentityControllerIT {

    @Inject
    lateinit var embeddedServer: EmbeddedServer

    @Nested
    inner class CreateIdentity {

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
            } When {
                port(embeddedServer.port)
                post("/api/identities")
            } Then {
                statusCode(HttpStatus.CREATED.code)
                body("id", notNullValue())
                body("first_name", equalTo("John"))
                body("last_name", equalTo("Doe"))
            }
        }
    }

}