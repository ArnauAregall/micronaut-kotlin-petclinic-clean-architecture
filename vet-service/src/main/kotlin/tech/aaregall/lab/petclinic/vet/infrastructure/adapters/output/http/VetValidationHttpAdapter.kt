package tech.aaregall.lab.petclinic.vet.infrastructure.adapters.output.http

import io.micronaut.context.annotation.Value
import io.micronaut.core.annotation.Introspected
import io.micronaut.core.type.Argument
import io.micronaut.http.HttpRequest.GET
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientException
import io.micronaut.serde.annotation.Serdeable
import jakarta.inject.Singleton
import tech.aaregall.lab.petclinic.vet.application.ports.output.VetValidationOutputPort
import tech.aaregall.lab.petclinic.vet.domain.model.VetId
import java.util.UUID

@Singleton
internal class VetValidationHttpAdapter(
    @Client("identity-service") private val httpClient: HttpClient,
    @Value("\${app.ports.output.vet-id-validation.required-identity-role-name}") private val requiredIdentityRoleName: String): VetValidationOutputPort {

    override fun isValidVetId(vetId: VetId): Boolean =
        try {
            httpClient.toBlocking()
                .exchange(GET<Identity>("/api/identities/$vetId"), Argument.of(Identity::class.java))
                .let { response ->
                    HttpStatus.OK == response.status && response.body.filter {
                        it.roles.orEmpty().contains(requiredIdentityRoleName)
                    }.isPresent
                }
        } catch (e: HttpClientException) {
            false
        }

}

@Serdeable
@Introspected
internal data class Identity(val id: UUID, val roles: Set<String>?)