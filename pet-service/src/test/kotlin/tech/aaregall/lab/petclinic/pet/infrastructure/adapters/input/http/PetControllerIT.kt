package tech.aaregall.lab.petclinic.pet.infrastructure.adapters.input.http

import io.micronaut.http.HttpStatus
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.restassured.http.ContentType
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.CoreMatchers.nullValue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@MicronautTest(transactional = false)
internal class PetControllerIT(private val embeddedServer: EmbeddedServer) {

    @Nested
    inner class CreatePet {

        @Test
        fun `Should create a Pet without PetOwner`() {
            Given {
                contentType(ContentType.JSON)
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

    }

}