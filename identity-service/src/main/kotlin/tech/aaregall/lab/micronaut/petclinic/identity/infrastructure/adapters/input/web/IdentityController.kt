package tech.aaregall.lab.micronaut.petclinic.identity.infrastructure.adapters.input.web

import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpResponse.created
import io.micronaut.http.HttpResponse.notFound
import io.micronaut.http.HttpResponse.ok
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Post
import jakarta.validation.Valid
import tech.aaregall.lab.micronaut.petclinic.identity.application.ports.input.CreateIdentityUseCase
import tech.aaregall.lab.micronaut.petclinic.identity.application.ports.input.LoadIdentityCommand
import tech.aaregall.lab.micronaut.petclinic.identity.application.ports.input.LoadIdentityUseCase
import tech.aaregall.lab.micronaut.petclinic.identity.domain.model.IdentityId
import tech.aaregall.lab.micronaut.petclinic.identity.infrastructure.adapters.input.web.dto.request.CreateIdentityRequest
import tech.aaregall.lab.micronaut.petclinic.identity.infrastructure.adapters.input.web.dto.response.IdentityResponse
import tech.aaregall.lab.micronaut.petclinic.identity.infrastructure.adapters.input.web.mapper.IdentityWebMapper
import java.util.UUID

@Controller("/api/identities")
private open class IdentityController(
    private val createIdentityUseCase: CreateIdentityUseCase,
    private val loadIdentityUseCase: LoadIdentityUseCase,
    private val identityWebMapper: IdentityWebMapper) {

    @Post
    open fun createIdentity(@Body @Valid createIdentityRequest: CreateIdentityRequest): HttpResponse<IdentityResponse> =
        created(identityWebMapper.mapToResponse(
            createIdentityUseCase.createIdentity(identityWebMapper.mapCreateRequestToCommand(createIdentityRequest))))

    @Get("/{id}")
    open fun loadIdentity(@PathVariable id: UUID): HttpResponse<IdentityResponse> =
        loadIdentityUseCase.loadIdentity(LoadIdentityCommand(IdentityId.of(id)))
            ?.let { ok(identityWebMapper.mapToResponse(it)) }
            ?: notFound()

}