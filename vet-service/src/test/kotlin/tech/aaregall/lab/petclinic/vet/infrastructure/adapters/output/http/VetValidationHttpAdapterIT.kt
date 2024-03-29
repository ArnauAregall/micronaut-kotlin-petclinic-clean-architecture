package tech.aaregall.lab.petclinic.vet.infrastructure.adapters.output.http

import io.micronaut.context.annotation.Value
import io.micronaut.http.HttpMethod.GET
import io.micronaut.http.HttpStatus
import io.micronaut.http.HttpStatus.OK
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.micronaut.test.extensions.testresources.annotation.TestResourcesProperties
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.mockserver.model.HttpRequest.request
import org.mockserver.model.HttpResponse.response
import org.mockserver.model.JsonBody.json
import org.mockserver.verify.VerificationTimes.once
import tech.aaregall.lab.petclinic.test.spec.mockserver.MockServerPropsProvider
import tech.aaregall.lab.petclinic.test.spec.mockserver.MockServerPropsProvider.Companion.getMockServerClient
import tech.aaregall.lab.petclinic.vet.domain.model.VetId

@MicronautTest
@TestResourcesProperties(providers = [MockServerPropsProvider::class])
internal class VetValidationHttpAdapterIT(private val vetValidationHttpAdapter: VetValidationHttpAdapter) {

    @Value("\${app.ports.output.vet-id-validation.required-identity-role-name}")
    lateinit var requiredIdentityRoleName: String

    @Nested
    inner class IsValidVetId {

        @ParameterizedTest
        @EnumSource(value = HttpStatus::class, mode = EnumSource.Mode.EXCLUDE, names = ["OK"])
        fun `Should return false when Identity service response status is not 200 OK`(nonOkHttpStatus: HttpStatus) {
            val vetId = VetId.create()

            val getIdentityRequest = request().withMethod(GET.name).withPath("/api/identities/$vetId")

            getMockServerClient()
                .`when`(getIdentityRequest)
                .respond(response().withStatusCode(nonOkHttpStatus.code))

            val result = vetValidationHttpAdapter.isValidVetId(vetId)

            assertThat(result).isFalse()

            getMockServerClient().verify(getIdentityRequest, once())
        }

        @Test
        fun `Should return false when Identity service response status is 200 OK and response body is empty`() {
            val vetId = VetId.create()

            val getIdentityRequest = request().withMethod(GET.name).withPath("/api/identities/$vetId")

            getMockServerClient()
                .`when`(getIdentityRequest)
                .respond(response().withStatusCode(OK.code).withBody(json("")))

            val result = vetValidationHttpAdapter.isValidVetId(vetId)

            assertThat(result).isFalse()

            getMockServerClient().verify(getIdentityRequest, once())
        }

        @Test
        fun `Should return false when Identity service response status is 200 OK and roles does not contain required role`() {
            val vetId = VetId.create()

            val getIdentityRequest = request().withMethod(GET.name).withPath("/api/identities/$vetId")

            getMockServerClient()
                .`when`(getIdentityRequest)
                .respond(response().withStatusCode(OK.code).withBody(json("""
                        {
                          "id": "$vetId", "roles": ["foo", "bar", "baz"]
                        }
                """.trimIndent())))

            val result = vetValidationHttpAdapter.isValidVetId(vetId)

            assertThat(result).isFalse()

            getMockServerClient().verify(getIdentityRequest, once())
        }

        @Test
        fun `Should return true when Identity service response status is 200 OK and roles contains required role`() {
            val vetId = VetId.create()

            val getIdentityRequest = request().withMethod(GET.name).withPath("/api/identities/$vetId")

            getMockServerClient()
                .`when`(getIdentityRequest)
                .respond(response().withStatusCode(OK.code).withBody(json("""
                        {
                          "id": "$vetId", "roles": ["foo", "bar", "baz", "$requiredIdentityRoleName"]
                        }
                """.trimIndent())))

            val result = vetValidationHttpAdapter.isValidVetId(vetId)

            assertThat(result).isTrue()

            getMockServerClient().verify(getIdentityRequest, once())
        }

    }

}