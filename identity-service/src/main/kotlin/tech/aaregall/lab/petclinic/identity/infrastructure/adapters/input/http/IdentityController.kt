package tech.aaregall.lab.petclinic.identity.infrastructure.adapters.input.http

import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpResponse.created
import io.micronaut.http.HttpResponse.notFound
import io.micronaut.http.HttpResponse.ok
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Patch
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Status
import jakarta.validation.Valid
import tech.aaregall.lab.petclinic.identity.application.ports.input.CreateIdentityUseCase
import tech.aaregall.lab.petclinic.identity.application.ports.input.LoadIdentityCommand
import tech.aaregall.lab.petclinic.identity.application.ports.input.LoadIdentityUseCase
import tech.aaregall.lab.petclinic.identity.application.ports.input.UpdateIdentityContactDetailsCommand
import tech.aaregall.lab.petclinic.identity.application.ports.input.UpdateIdentityContactDetailsUseCase
import tech.aaregall.lab.petclinic.identity.domain.model.IdentityId
import tech.aaregall.lab.petclinic.identity.infrastructure.adapters.input.http.dto.request.CreateIdentityRequest
import tech.aaregall.lab.petclinic.identity.infrastructure.adapters.input.http.dto.request.UpdateIdentityContactDetailsRequest
import tech.aaregall.lab.petclinic.identity.infrastructure.adapters.input.http.dto.response.IdentityResponse
import tech.aaregall.lab.petclinic.identity.infrastructure.adapters.input.http.mapper.IdentityHttpMapper
import java.util.UUID

@Controller("/api/identities")
private open class IdentityController(
    private val createIdentityUseCase: CreateIdentityUseCase,
    private val loadIdentityUseCase: LoadIdentityUseCase,
    private val updateIdentityContactDetailsUseCase: UpdateIdentityContactDetailsUseCase,
    private val identityHttpMapper: IdentityHttpMapper) {

    @Post
    open fun createIdentity(@Body @Valid createIdentityRequest: CreateIdentityRequest): HttpResponse<IdentityResponse> =
        created(identityHttpMapper.mapToResponse(
            createIdentityUseCase.createIdentity(identityHttpMapper.mapCreateRequestToCommand(createIdentityRequest))))

    @Get("/{id}")
    open fun loadIdentity(@PathVariable id: UUID): HttpResponse<IdentityResponse> =
        loadIdentityUseCase.loadIdentity(LoadIdentityCommand(IdentityId.of(id)))
            ?.let { ok(identityHttpMapper.mapToResponse(it)) }
            ?: notFound()

    @Patch("/{id}/contact-details")
    @Status(HttpStatus.NO_CONTENT)
    open fun updateIdentityContactDetails(
        @PathVariable id: UUID,
        @Body @Valid updateIdentityContactDetailsRequest: UpdateIdentityContactDetailsRequest
    ) {
        updateIdentityContactDetailsUseCase.updateIdentityContactDetails(
            UpdateIdentityContactDetailsCommand(
                IdentityId.of(id),
                updateIdentityContactDetailsRequest.email,
                updateIdentityContactDetailsRequest.phoneNumber
            )
        )
    }

}