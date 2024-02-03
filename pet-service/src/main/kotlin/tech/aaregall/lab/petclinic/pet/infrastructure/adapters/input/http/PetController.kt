package tech.aaregall.lab.petclinic.pet.infrastructure.adapters.input.http

import io.micronaut.data.model.Page
import io.micronaut.data.model.Pageable
import io.micronaut.http.HttpResponse
import io.micronaut.http.MutableHttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import jakarta.validation.Valid
import reactor.core.publisher.Mono
import tech.aaregall.lab.petclinic.pet.application.ports.input.CountAllPetsUseCase
import tech.aaregall.lab.petclinic.pet.application.ports.input.CreatePetUseCase
import tech.aaregall.lab.petclinic.pet.application.ports.input.SearchPetsCommand
import tech.aaregall.lab.petclinic.pet.application.ports.input.SearchPetsUseCase
import tech.aaregall.lab.petclinic.pet.infrastructure.adapters.input.http.dto.request.CreatePetRequest
import tech.aaregall.lab.petclinic.pet.infrastructure.adapters.input.http.dto.response.PetResponse
import tech.aaregall.lab.petclinic.pet.infrastructure.adapters.input.http.mapper.PetHttpMapper

@Controller("/api/pets")
private open class PetController(
    private val createPetUseCase: CreatePetUseCase,
    private val searchPetsUseCase: SearchPetsUseCase,
    private val countAllPetsUseCase: CountAllPetsUseCase,
    private val petHttpMapper: PetHttpMapper) {

    @Get
    open fun searchPets(pageable: Pageable): Mono<Page<PetResponse>> =
        searchPetsUseCase.searchPets(SearchPetsCommand(pageable.number, pageable.size))
            .map { petHttpMapper.mapToResponse(it) }
            .toFlux()
            .collectList()
            .zipWith(countAllPetsUseCase.countAllPets().toMono())
            .map { tuple -> Page.of(tuple.t1, pageable, tuple.t2.toLong()) }


    @Post
    open fun createPet(@Body @Valid createPetRequest: CreatePetRequest): Mono<MutableHttpResponse<PetResponse>> =
        createPetUseCase.createPet(petHttpMapper.mapCreateRequestToCommand(createPetRequest))
            .map { petHttpMapper.mapToResponse(it) }
            .map { HttpResponse.created(it) }
            .toMono()

}