package tech.aaregall.lab.petclinic.vet.infrastructure.adapters.input.http

import io.micronaut.data.model.Page
import io.micronaut.data.model.Pageable
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpResponse.created
import io.micronaut.http.HttpResponse.ok
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Put
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.scheduling.annotation.ExecuteOn
import jakarta.validation.Valid
import tech.aaregall.lab.petclinic.vet.application.ports.input.CountAllVetsInputPort
import tech.aaregall.lab.petclinic.vet.application.ports.input.CreateVetCommand
import tech.aaregall.lab.petclinic.vet.application.ports.input.CreateVetInputPort
import tech.aaregall.lab.petclinic.vet.application.ports.input.SearchVetsCommand
import tech.aaregall.lab.petclinic.vet.application.ports.input.SearchVetsInputPort
import tech.aaregall.lab.petclinic.vet.application.ports.input.SetVetSpecialitiesCommand
import tech.aaregall.lab.petclinic.vet.application.ports.input.SetVetSpecialitiesInputPort
import tech.aaregall.lab.petclinic.vet.domain.model.SpecialityId
import tech.aaregall.lab.petclinic.vet.domain.model.VetId
import tech.aaregall.lab.petclinic.vet.infrastructure.adapters.input.http.dto.request.CreateVetRequest
import tech.aaregall.lab.petclinic.vet.infrastructure.adapters.input.http.dto.request.SetVetSpecialitiesRequest
import tech.aaregall.lab.petclinic.vet.infrastructure.adapters.input.http.dto.response.VetResponse
import java.util.UUID

@Controller("/api/vets")
private open class VetController(
    private val searchVetsInputPort: SearchVetsInputPort,
    private val countAllVetsInputPort: CountAllVetsInputPort,
    private val createVetInputPort: CreateVetInputPort,
    private val setVetSpecialitiesInputPort: SetVetSpecialitiesInputPort) {

    @Get
    open fun searchVets(pageable: Pageable): HttpResponse<Page<VetResponse>> =
        ok(
            Page.of(
                searchVetsInputPort.searchVets(
                    SearchVetsCommand(
                        pageNumber = pageable.number,
                        pageSize = pageable.size
                    )
                ).map { VetResponse.fromVet(it) },
                pageable, countAllVetsInputPort.countAllVets().toLong()
            )
        )

    @Post
    @ExecuteOn(TaskExecutors.BLOCKING)
    open fun createVet(@Body @Valid createVetRequest: CreateVetRequest): HttpResponse<VetResponse> =
        created(
            createVetInputPort.createVet(
                CreateVetCommand(
                    identityId = createVetRequest.identityId!!,
                    specialitiesIds = createVetRequest.specialitiesIds.map { SpecialityId.of(it) }.toSet()
                )
            ).let { VetResponse.fromVet(it) }
        )

    @Put("/{id}/specialities")
    open fun setVetSpecialities(@PathVariable id: UUID, @Body @Valid setVetSpecialitiesRequest: SetVetSpecialitiesRequest): HttpResponse<VetResponse> =
        ok(
            setVetSpecialitiesInputPort.setVetSpecialities(
                SetVetSpecialitiesCommand(
                    vetId = VetId.of(id),
                    specialitiesIds = setVetSpecialitiesRequest.specialitiesIds.map { SpecialityId.of(it) }.toSet()
                )
            ).let { VetResponse.fromVet(it) }
        )

}