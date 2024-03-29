package tech.aaregall.lab.petclinic.vet.infrastructure.adapters.input.http

import io.micronaut.data.jdbc.runtime.JdbcOperations
import io.micronaut.http.HttpStatus
import io.micronaut.http.HttpStatus.BAD_REQUEST
import io.micronaut.http.HttpStatus.CONFLICT
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
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import tech.aaregall.lab.petclinic.testresources.keycloak.KeycloakFixture.Companion.getAuthorizationBearer
import tech.aaregall.lab.petclinic.testresources.keycloak.KeycloakPropsProvider
import tech.aaregall.lab.petclinic.vet.application.ports.input.CreateSpecialityCommand
import tech.aaregall.lab.petclinic.vet.application.ports.input.CreateSpecialityInputPort

@MicronautTest(transactional = false)
@TestResourcesProperties(providers = [KeycloakPropsProvider::class])
internal class SpecialityControllerIT(private val embeddedServer: EmbeddedServer, private val jdbc: JdbcOperations) {

    @BeforeEach
    fun setUp() {
        jdbc.execute { conn -> conn.prepareStatement("TRUNCATE TABLE speciality CASCADE ").execute() }
    }

    @Nested
    inner class SearchSpecialities {

        @Test
        fun `Should return Unauthorized when no Authorization header`() {
            Given {
                contentType(JSON)
                queryParams(mapOf("page" to "0", "size" to "20"))
            } When {
                port(embeddedServer.port)
                get("/api/specialities")
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
                get("/api/specialities")
            } Then {
                statusCode(OK.code)
                body(
                    "content.size()", equalTo(0),
                    "totalSize", equalTo(0)
                )
            }
        }

        @Test
        fun `Should return 200 OK with paginated Specialities content when there are Specialities`(createSpecialityInputPort: CreateSpecialityInputPort) {
            val specialities = (0..40).map {
                createSpecialityInputPort.createSpeciality(CreateSpecialityCommand(
                    name = "Speciality $it", description = "Description for Speciality $it"
                ))
            }.toList()

            fun assertPage(page: Int, size: Int, expectedRange: IntRange) {
                fun sliceSortedByName(range: IntRange) = specialities.sortedBy { it.name }.slice(range)

                Given {
                    contentType(JSON)
                    queryParams(mapOf("page" to page, "size" to size))
                    header(getAuthorizationBearer())
                } When {
                    port(embeddedServer.port)
                    get("/api/specialities")
                } Then {
                    statusCode(OK.code)
                    val expectedSlice = sliceSortedByName(expectedRange)
                    body(
                        "content.size()", equalTo(expectedSlice.size),
                        "content.id", containsInAnyOrder(
                            *expectedSlice.map { it.id.toString() }.toTypedArray()
                        ),
                        "content.name", containsInAnyOrder(
                            *expectedSlice.map { it.name }.toTypedArray())
                        ,
                        "content.description", containsInAnyOrder(
                            *expectedSlice.map { it.description }.toTypedArray()
                        ),
                        "totalSize", equalTo(specialities.size)
                    )
                }
            }

            assertPage(1, 20, 0..19)
            assertPage(2, 20, 20..39)
        }

    }

    @Nested
    inner class CreateSpeciality {

        @Test
        fun `Should return Unauthorized when no Authorization header`() {
            Given {
                contentType(JSON)
                body("""
                    {
                        "name": "Foo"
                    }
                """.trimIndent())
            } When {
                port(embeddedServer.port)
                post("/api/specialities")
            } Then {
                statusCode(UNAUTHORIZED.code)
                body("message", equalTo("Unauthorized"))
            }
        }

        @Test
        fun `Should not allow blank name`() {
            Given {
                contentType(JSON)
                body("""
                    {
                        "name": ""
                    }
                """.trimIndent())
                header(getAuthorizationBearer())
            } When {
                port(embeddedServer.port)
                post("/api/specialities")
            } Then {
                statusCode(BAD_REQUEST.code)
                body(
                    "message", equalTo("Bad Request"),
                    "_embedded.errors.size()", equalTo(1),
                    "_embedded.errors[0].message", allOf(
                        containsString("name"),
                        containsString("must not be blank")
                    )
                )
            }
        }

        @Test
        fun `Should not allow blank description`() {
            Given {
                contentType(JSON)
                body("""
                    {
                        "name": "Foo",
                        "description": ""
                    }
                """.trimIndent())
                header(getAuthorizationBearer())
            } When {
                port(embeddedServer.port)
                post("/api/specialities")
            } Then {
                statusCode(BAD_REQUEST.code)
                body(
                    "message", equalTo("Bad Request"),
                    "_embedded.errors.size()", equalTo(1),
                    "_embedded.errors[0].message", allOf(
                        containsString("description"),
                        containsString("size must be between 1 and 1000")
                    )
                )
            }
        }

        @Test
        fun `Should return conflict when a Speciality with a similar name already exists`(createSpecialityInputPort: CreateSpecialityInputPort) {
            val speciality = createSpecialityInputPort.createSpeciality(CreateSpecialityCommand(name = "Surgery"))

            Given {
                contentType(JSON)
                body("""
                    {
                        "name": "${speciality.name}"
                    }
                """.trimIndent())
                header(getAuthorizationBearer())
            } When {
                port(embeddedServer.port)
                post("/api/specialities")
            } Then {
                statusCode(CONFLICT.code)
                body(
                    "message", equalTo("Conflict"),
                    "_embedded.errors.size()", equalTo(1),
                    "_embedded.errors[0].message", allOf(
                        containsString("Speciality with name"),
                        containsString(speciality.name),
                        containsString("already exists")
                    )
                )
            }
        }

        @Test
        fun `Should return 200 OK when request body is valid and no Speciality with a similar name exists`() {
            Given {
                contentType(JSON)
                body("""
                    {
                        "name": "Cardiology",
                        "description": "Cardiology is a branch of medicine that deals with disorders of the heart"
                    }
                """.trimIndent())
                header(getAuthorizationBearer())
            } When {
                port(embeddedServer.port)
                post("/api/specialities")
            } Then {
                statusCode(HttpStatus.CREATED.code)
                body(
                    "id", notNullValue(),
                    "name", equalTo("Cardiology"),
                    "description", equalTo("Cardiology is a branch of medicine that deals with disorders of the heart")
                )
            }
        }

    }

}