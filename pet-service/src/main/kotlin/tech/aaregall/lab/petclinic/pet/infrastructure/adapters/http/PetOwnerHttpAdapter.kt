package tech.aaregall.lab.petclinic.pet.infrastructure.adapters.http

import com.fasterxml.jackson.annotation.JsonProperty
import io.micronaut.core.annotation.Introspected
import io.micronaut.core.async.annotation.SingleResult
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientException
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.serde.annotation.Serdeable
import jakarta.inject.Singleton
import tech.aaregall.lab.petclinic.pet.application.ports.output.LoadPetOwnerCommand
import tech.aaregall.lab.petclinic.pet.application.ports.output.LoadPetOwnerCommandException
import tech.aaregall.lab.petclinic.pet.application.ports.output.PetOwnerOutputPort
import tech.aaregall.lab.petclinic.pet.domain.model.PetOwner
import java.lang.Exception
import java.util.UUID

@Singleton
internal class PetOwnerHttpAdapter(private val identityServiceHttpClient: IdentityServiceHttpClient): PetOwnerOutputPort {

    override fun loadPetOwner(loadPetOwnerCommand: LoadPetOwnerCommand): PetOwner? {
        try {
            val response = identityServiceHttpClient.getIdentity(loadPetOwnerCommand.ownerIdentityId)
            return when (response.status) {
                HttpStatus.OK -> PetOwner(response.body().id)
                HttpStatus.NOT_FOUND -> null
                else -> throwLoadCommandException(HttpClientResponseException("Unexpected response code", response))
            }
        } catch (exception: HttpClientException) {
            throwLoadCommandException(exception)
        }
    }

    private fun throwLoadCommandException(exception: Exception): Nothing =
        throw LoadPetOwnerCommandException("HTTP call to Identity Service returned not expected response status", exception)

}


@Client(id = "identity-service")
internal fun interface IdentityServiceHttpClient {

    @Get("/api/identities/{identityId}")
    @SingleResult
    fun getIdentity(@PathVariable identityId: UUID): HttpResponse<GetIdentityResponse>

}

@Introspected
@Serdeable
internal data class GetIdentityResponse(@JsonProperty("id") val id: UUID)