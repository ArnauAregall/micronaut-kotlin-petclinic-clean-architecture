package tech.aaregall.lab.petclinic.pet.infrastructure.adapters.output.http

import com.fasterxml.jackson.annotation.JsonProperty
import io.micronaut.cache.annotation.CacheConfig
import io.micronaut.core.annotation.Introspected
import io.micronaut.core.async.annotation.SingleResult
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.serde.annotation.Serdeable
import jakarta.inject.Singleton
import reactor.core.publisher.Mono
import tech.aaregall.lab.petclinic.common.reactive.UnitReactive
import tech.aaregall.lab.petclinic.pet.application.ports.output.LoadPetOwnerCommand
import tech.aaregall.lab.petclinic.pet.application.ports.output.LoadPetOwnerCommandException
import tech.aaregall.lab.petclinic.pet.application.ports.output.PetOwnerOutputPort
import tech.aaregall.lab.petclinic.pet.domain.model.PetOwner
import java.util.UUID

@Singleton
@CacheConfig(cacheNames = ["pet-owner"])
internal open class PetOwnerHttpAdapter(private val identityServiceHttpClient: IdentityServiceHttpClient): PetOwnerOutputPort {

    override fun loadPetOwner(loadPetOwnerCommand: LoadPetOwnerCommand): UnitReactive<PetOwner?> {
        return UnitReactive(loadPetOwnerFromIdentityService(loadPetOwnerCommand.ownerIdentityId))
    }

    // @io.micronaut.cache.annotation.Cacheable TODO: review interference with io.micronaut.security.token.propagation.TokenPropagationHttpClientFilter
    open fun loadPetOwnerFromIdentityService(identityId: UUID): Mono<PetOwner?> {
        return identityServiceHttpClient.getIdentity(identityId)
            .mapNotNull { PetOwner(it.body().id) }
            .onErrorResume {
                if (it is HttpClientResponseException && it.status == HttpStatus.NOT_FOUND) Mono.empty()
                else Mono.error(LoadPetOwnerCommandException("HTTP call to Identity Service returned not expected response status", it))
            }
    }

}


@Client(id = "identity-service")
internal fun interface IdentityServiceHttpClient {

    @Get("/api/identities/{identityId}")
    @SingleResult
    fun getIdentity(@PathVariable identityId: UUID): Mono<HttpResponse<GetIdentityResponse>>

}

@Introspected
@Serdeable
internal data class GetIdentityResponse(@JsonProperty("id") val id: UUID)