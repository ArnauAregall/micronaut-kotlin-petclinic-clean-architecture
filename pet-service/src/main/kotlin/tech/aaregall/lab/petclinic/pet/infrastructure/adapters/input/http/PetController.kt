package tech.aaregall.lab.petclinic.pet.infrastructure.adapters.input.http

import io.micronaut.data.model.Page
import io.micronaut.data.model.Pageable
import io.micronaut.http.HttpResponse
import io.micronaut.http.MutableHttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Patch
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Post
import jakarta.validation.Valid
import reactor.core.publisher.Mono
import tech.aaregall.lab.petclinic.pet.application.ports.input.AdoptPetCommand
import tech.aaregall.lab.petclinic.pet.application.ports.input.AdoptPetInputPort
import tech.aaregall.lab.petclinic.pet.application.ports.input.CountAllPetsInputPort
import tech.aaregall.lab.petclinic.pet.application.ports.input.CreatePetInputPort
import tech.aaregall.lab.petclinic.pet.application.ports.input.DeletePetCommand
import tech.aaregall.lab.petclinic.pet.application.ports.input.DeletePetInputPort
import tech.aaregall.lab.petclinic.pet.application.ports.input.LoadPetCommand
import tech.aaregall.lab.petclinic.pet.application.ports.input.LoadPetInputPort
import tech.aaregall.lab.petclinic.pet.application.ports.input.SearchPetsCommand
import tech.aaregall.lab.petclinic.pet.application.ports.input.SearchPetsInputPort
import tech.aaregall.lab.petclinic.pet.domain.model.PetId
import tech.aaregall.lab.petclinic.pet.infrastructure.adapters.input.http.dto.request.AdoptPetRequest
import tech.aaregall.lab.petclinic.pet.infrastructure.adapters.input.http.dto.request.CreatePetRequest
import tech.aaregall.lab.petclinic.pet.infrastructure.adapters.input.http.dto.response.PetResponse
import tech.aaregall.lab.petclinic.pet.infrastructure.adapters.input.http.mapper.PetHttpMapper
import java.util.UUID

@Controller("/api/pets")
private open class PetController(
    private val createPetInputPort: CreatePetInputPort,
    private val loadPetUseCase: LoadPetInputPort,
    private val adoptPetInputPort: AdoptPetInputPort,
    private val searchPetsInputPort: SearchPetsInputPort,
    private val countAllPetsUseCase: CountAllPetsInputPort,
    private val deletePetInputPort: DeletePetInputPort,
    private val petHttpMapper: PetHttpMapper) {

    @Get
    open fun searchPets(pageable: Pageable): Mono<Page<PetResponse>> =
        searchPetsInputPort.searchPets(SearchPetsCommand(pageable.number, pageable.size))
            .map { petHttpMapper.mapToResponse(it) }
            .toFlux()
            .collectList()
            .zipWith(countAllPetsUseCase.countAllPets().toMono())
            .map { tuple -> Page.of(tuple.t1, pageable, tuple.t2.toLong()) }

    @Get("/{id}")
    open fun loadPet(@PathVariable id: UUID): Mono<MutableHttpResponse<PetResponse>> =
        loadPetUseCase.loadPet(LoadPetCommand(PetId.of(id)))
            .map { petHttpMapper.mapToResponse(it) }
            .map { HttpResponse.ok(it) }
            .toMono()

    @Patch("/{id}/adopt")
    open fun adoptPet(@PathVariable id: UUID, @Body @Valid adoptPetRequest: AdoptPetRequest): Mono<MutableHttpResponse<PetResponse>> =
        adoptPetInputPort.adoptPet(AdoptPetCommand(PetId.of(id), adoptPetRequest.ownerIdentityId!!))
            .map { petHttpMapper.mapToResponse(it) }
            .map { HttpResponse.ok(it) }
            .toMono()

    @Delete("/{id}")
    open fun deletePet(@PathVariable id: UUID): Mono<MutableHttpResponse<Any>> =
        deletePetInputPort.deletePet(DeletePetCommand(PetId.of(id)))
            .map { HttpResponse.noContent<Any>() }
            .toMono()

    @Post
    open fun createPet(@Body @Valid createPetRequest: CreatePetRequest): Mono<MutableHttpResponse<PetResponse>> =
        createPetInputPort.createPet(petHttpMapper.mapCreateRequestToCommand(createPetRequest))
            .map { petHttpMapper.mapToResponse(it) }
            .map { HttpResponse.created(it) }
            .toMono()

}