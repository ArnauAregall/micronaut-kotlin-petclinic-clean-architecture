package tech.aaregall.lab.petclinic.pet.infrastructure.adapters.input.http

import io.micronaut.http.HttpResponse
import io.micronaut.http.MutableHttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import reactor.core.publisher.Mono
import tech.aaregall.lab.petclinic.pet.application.ports.input.CreatePetUseCase
import tech.aaregall.lab.petclinic.pet.infrastructure.adapters.input.http.dto.request.CreatePetRequest
import tech.aaregall.lab.petclinic.pet.infrastructure.adapters.input.http.dto.response.PetResponse
import tech.aaregall.lab.petclinic.pet.infrastructure.adapters.input.http.mapper.PetHttpMapper

@Controller("/api/pets")
private class PetController(
    private val createPetUseCase: CreatePetUseCase,
    private val petHttpMapper: PetHttpMapper) {

    @Post
    fun createPet(@Body createPetRequest: CreatePetRequest): Mono<MutableHttpResponse<PetResponse>> =
        createPetUseCase.createPet(petHttpMapper.mapCreateRequestToCommand(createPetRequest))
            .map { petHttpMapper.mapToResponse(it) }
            .map { HttpResponse.created(it) }
            .toMono()

}