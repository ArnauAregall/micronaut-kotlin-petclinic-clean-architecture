package tech.aaregall.lab.petclinic.pet.infrastructure.adapters.output.http

import io.lettuce.core.api.StatefulRedisConnection
import io.micronaut.context.annotation.Value
import io.micronaut.http.HttpMethod.GET
import io.micronaut.http.HttpStatus
import io.micronaut.http.HttpStatus.NOT_FOUND
import io.micronaut.http.HttpStatus.OK
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.micronaut.test.extensions.testresources.annotation.TestResourcesProperties
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.mockserver.client.MockServerClient
import org.mockserver.model.Header
import org.mockserver.model.HttpRequest.request
import org.mockserver.model.HttpResponse.response
import org.mockserver.model.JsonBody.json
import org.mockserver.verify.VerificationTimes.once
import tech.aaregall.lab.petclinic.pet.application.ports.output.LoadPetOwnerCommand
import tech.aaregall.lab.petclinic.pet.application.ports.output.LoadPetOwnerCommandException
import tech.aaregall.lab.petclinic.testresources.mockserver.MockServerPropsProvider
import java.util.UUID

@MicronautTest
@TestResourcesProperties(providers = [MockServerPropsProvider::class])
internal class PetOwnerHttpAdapterIT(
    private val petOwnerHttpAdapter: PetOwnerHttpAdapter,
    private val redisConnection: StatefulRedisConnection<String, String>,
    private val mockServerClient: MockServerClient) {

    @Value("\${app.ports.output.pet-owner.required-identity-role-name}")
    lateinit var requiredIdentityRoleName: String

    @AfterEach
    fun tearDown() {
        mockServerClient.reset()
    }

    fun getCachedPetOwner(identityId: UUID): String? = redisConnection.sync().get("pet-owner:$identityId")

    @Nested
    inner class LoadPetOwner {


        @Test
        fun `Should return and cache a PetOwner when Identity Service response is 200 OK and Identity has the required role`() {
            val identityId = UUID.randomUUID()

            mockServerClient
                .`when`(request().withMethod(GET.name).withPath("/api/identities/$identityId"))
                .respond(
                    response()
                        .withStatusCode(OK.code)
                        .withHeaders(Header("Content-Type", "application/json"))
                        .withBody(json(
                            """
                            {
                              "id": "$identityId", "first_name": "John", "last_name": "Doe", "roles": ["$requiredIdentityRoleName"]
                            }
                        """.trimIndent()
                        ))
                )

            val petOwner = petOwnerHttpAdapter.loadPetOwner(LoadPetOwnerCommand(identityId))

            assertThat(petOwner.block()!!)
                .isNotNull
                .extracting("identityId", "firstName", "lastName")
                .containsExactly(identityId, "John", "Doe")

            assertThat(getCachedPetOwner(identityId)).isNotNull

            mockServerClient
                .verify(request().withMethod(GET.name).withPath("/api/identities/$identityId"), once())
        }

        @Test
        fun `Should return null when Identity Service response is 200 OK and Identity does not have the required role`() {
            val identityId = UUID.randomUUID()

            mockServerClient
                .`when`(request().withMethod(GET.name).withPath("/api/identities/$identityId"))
                .respond(
                    response()
                        .withStatusCode(OK.code)
                        .withHeaders(Header("Content-Type", "application/json"))
                        .withBody(json(
                            """
                            {
                              "id": "$identityId", "first_name": "Bob", "last_name": "Builder", "roles": ["FOO", "BAR"]
                            }
                        """.trimIndent()
                        ))
                )

            val petOwner = petOwnerHttpAdapter.loadPetOwner(LoadPetOwnerCommand(identityId))

            assertThat(petOwner.block()).isNull()

            mockServerClient
                .verify(request().withMethod(GET.name).withPath("/api/identities/$identityId"), once())
        }

        @Test
        fun `Should return null when Identity Service response is 404 Not Found`() {
            val identityId = UUID.randomUUID()

            mockServerClient
                .`when`(request().withMethod(GET.name).withPath("/api/identities/$identityId"))
                .respond(
                    response().withStatusCode(NOT_FOUND.code)
                )

            val petOwner = petOwnerHttpAdapter.loadPetOwner(LoadPetOwnerCommand(identityId))

            assertThat(petOwner.block()).isNull()

            mockServerClient
                .verify(request().withMethod(GET.name).withPath("/api/identities/$identityId"), once())
        }


        @ParameterizedTest
        @EnumSource(value = HttpStatus::class, mode = EnumSource.Mode.EXCLUDE, names = ["OK", "NOT_FOUND"])
        fun `Should throw a controlled exception when Identity Service response is not 200 or 404`(httpStatus: HttpStatus) {
            val identityId = UUID.randomUUID()

            mockServerClient
                .reset()
                .`when`(request().withMethod(GET.name).withPath("/api/identities/$identityId"))
                .respond(
                    response().withStatusCode(httpStatus.code)
                )

            assertThatCode { petOwnerHttpAdapter.loadPetOwner(LoadPetOwnerCommand(identityId)).block()!! }
                .isInstanceOf(LoadPetOwnerCommandException::class.java)
                .hasMessageContaining("Failed loading PetOwner")
                .hasMessageContaining("HTTP call to Identity Service returned not expected response status")

            mockServerClient
                .verify(request().withMethod(GET.name).withPath("/api/identities/$identityId"), once())
        }

    }

    @Nested
    inner class DeletePetOwner {

        @Test
        fun `Should invalidate PetOwner cache`() {
            val identityId = UUID.randomUUID()

            mockServerClient
                .`when`(request())
                .respond(
                    response()
                    .withStatusCode(OK.code)
                    .withBody(json(
                        """
                            {
                              "id": "$identityId", "roles": ["$requiredIdentityRoleName"]
                            }
                        """.trimIndent()
                    )
                )
            )

            val petOwner = petOwnerHttpAdapter.loadPetOwner(LoadPetOwnerCommand(identityId)).block()!!

            assertThat(getCachedPetOwner(identityId)).isNotNull

            petOwnerHttpAdapter.deletePetOwner(petOwner)

            assertThat(getCachedPetOwner(identityId)).isNull()
        }

    }

}