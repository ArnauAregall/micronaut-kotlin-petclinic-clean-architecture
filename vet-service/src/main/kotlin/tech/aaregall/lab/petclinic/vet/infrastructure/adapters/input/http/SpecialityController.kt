package tech.aaregall.lab.petclinic.vet.infrastructure.adapters.input.http

import io.micronaut.data.model.Page
import io.micronaut.data.model.Pageable
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpResponse.created
import io.micronaut.http.HttpResponse.ok
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import jakarta.validation.Valid
import tech.aaregall.lab.petclinic.vet.application.ports.input.CountAllSpecialitiesInputPort
import tech.aaregall.lab.petclinic.vet.application.ports.input.CreateSpecialityCommand
import tech.aaregall.lab.petclinic.vet.application.ports.input.CreateSpecialityInputPort
import tech.aaregall.lab.petclinic.vet.application.ports.input.SearchSpecialitiesCommand
import tech.aaregall.lab.petclinic.vet.application.ports.input.SearchSpecialitiesInputPort
import tech.aaregall.lab.petclinic.vet.infrastructure.adapters.input.http.dto.request.CreateSpecialityRequest
import tech.aaregall.lab.petclinic.vet.infrastructure.adapters.input.http.dto.response.SpecialityResponse

@Controller("/api/specialities")
private open class SpecialityController(
    private val searchSpecialitiesInputPort: SearchSpecialitiesInputPort,
    private val countAllSpecialitiesInputPort: CountAllSpecialitiesInputPort,
    private val createSpecialityInputPort: CreateSpecialityInputPort) {

    @Get
    open fun searchSpecialities(pageable: Pageable): HttpResponse<Page<SpecialityResponse>> =
        ok(
            Page.of(
                searchSpecialitiesInputPort.searchSpecialities(SearchSpecialitiesCommand(pageable.number, pageable.size))
                    .map { SpecialityResponse.fromSpeciality(it) },
                pageable, countAllSpecialitiesInputPort.countAllSpecialities().toLong())
        )

    @Post
    open fun createSpeciality(@Body @Valid createSpecialityRequest: CreateSpecialityRequest): HttpResponse<SpecialityResponse> =
        created(
            createSpecialityInputPort.createSpeciality(CreateSpecialityCommand(createSpecialityRequest.name, createSpecialityRequest.description))
                .let {
                    SpecialityResponse.fromSpeciality(it)
                }
        )

}