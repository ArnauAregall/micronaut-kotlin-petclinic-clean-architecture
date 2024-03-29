package tech.aaregall.lab.petclinic.identity.infrastructure.adapters.input.http

import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpResponse.created
import io.micronaut.http.HttpResponse.notFound
import io.micronaut.http.HttpResponse.ok
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Patch
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Put
import io.micronaut.http.annotation.Status
import jakarta.validation.Valid
import tech.aaregall.lab.petclinic.identity.application.ports.input.AssignRoleToIdentityCommand
import tech.aaregall.lab.petclinic.identity.application.ports.input.AssignRoleToIdentityInputPort
import tech.aaregall.lab.petclinic.identity.application.ports.input.CreateIdentityInputPort
import tech.aaregall.lab.petclinic.identity.application.ports.input.DeleteIdentityCommand
import tech.aaregall.lab.petclinic.identity.application.ports.input.DeleteIdentityInputPort
import tech.aaregall.lab.petclinic.identity.application.ports.input.LoadIdentityCommand
import tech.aaregall.lab.petclinic.identity.application.ports.input.LoadIdentityInputPort
import tech.aaregall.lab.petclinic.identity.application.ports.input.RevokeRoleFromIdentityCommand
import tech.aaregall.lab.petclinic.identity.application.ports.input.RevokeRoleFromIdentityInputPort
import tech.aaregall.lab.petclinic.identity.application.ports.input.UpdateIdentityContactDetailsCommand
import tech.aaregall.lab.petclinic.identity.application.ports.input.UpdateIdentityContactDetailsInputPort
import tech.aaregall.lab.petclinic.identity.domain.model.IdentityId
import tech.aaregall.lab.petclinic.identity.domain.model.RoleId
import tech.aaregall.lab.petclinic.identity.infrastructure.adapters.input.http.dto.request.AssignRoleToIdentityRequest
import tech.aaregall.lab.petclinic.identity.infrastructure.adapters.input.http.dto.request.CreateIdentityRequest
import tech.aaregall.lab.petclinic.identity.infrastructure.adapters.input.http.dto.request.UpdateIdentityContactDetailsRequest
import tech.aaregall.lab.petclinic.identity.infrastructure.adapters.input.http.dto.response.IdentityResponse
import tech.aaregall.lab.petclinic.identity.infrastructure.adapters.input.http.mapper.IdentityHttpMapper
import java.util.UUID

@Controller("/api/identities")
private open class IdentityController(
    private val createIdentityInputPort: CreateIdentityInputPort,
    private val loadIdentityUseCase: LoadIdentityInputPort,
    private val updateIdentityContactDetailsInputPort: UpdateIdentityContactDetailsInputPort,
    private val deleteIdentityInputPort: DeleteIdentityInputPort,
    private val assignRoleToIdentityInputPort: AssignRoleToIdentityInputPort,
    private val revokeRoleFromIdentityInputPort: RevokeRoleFromIdentityInputPort,
    private val identityHttpMapper: IdentityHttpMapper) {

    @Post
    open fun createIdentity(@Body @Valid createIdentityRequest: CreateIdentityRequest): HttpResponse<IdentityResponse> =
        created(identityHttpMapper.mapToResponse(
            createIdentityInputPort.createIdentity(identityHttpMapper.mapCreateRequestToCommand(createIdentityRequest))))

    @Get("/{id}")
    fun loadIdentity(@PathVariable id: UUID): HttpResponse<IdentityResponse> =
        loadIdentityUseCase.loadIdentity(LoadIdentityCommand(IdentityId.of(id)))
            ?.let { ok(identityHttpMapper.mapToResponse(it)) }
            ?: notFound()

    @Patch("/{id}/contact-details")
    @Status(HttpStatus.NO_CONTENT)
    open fun updateIdentityContactDetails(@PathVariable id: UUID, @Body @Valid updateIdentityContactDetailsRequest: UpdateIdentityContactDetailsRequest) {
        updateIdentityContactDetailsInputPort.updateIdentityContactDetails(
            UpdateIdentityContactDetailsCommand(
                IdentityId.of(id),
                updateIdentityContactDetailsRequest.email,
                updateIdentityContactDetailsRequest.phoneNumber
            )
        )
    }

    @Put("/{id}/role")
    @Status(HttpStatus.OK)
    open fun assignRoleToIdentity(@PathVariable id: UUID, @Body @Valid assignRoleToIdentityRequest: AssignRoleToIdentityRequest) =
        assignRoleToIdentityInputPort.assignRoleToIdentity(
            AssignRoleToIdentityCommand(
                identityId = IdentityId.of(id), roleId = RoleId.of(assignRoleToIdentityRequest.roleId)
            )
        )

    @Delete("/{id}/role/{roleId}")
    @Status(HttpStatus.NO_CONTENT)
    fun revokeRoleFromIdentity(@PathVariable id: UUID, @PathVariable roleId: UUID) =
        revokeRoleFromIdentityInputPort.revokeRoleFromIdentity(
            RevokeRoleFromIdentityCommand(
                identityId = IdentityId.of(id), roleId = RoleId.of(roleId)
            )
        )

    @Delete("/{id}")
    @Status(HttpStatus.NO_CONTENT)
    fun deleteIdentity(@PathVariable id: UUID) =
        deleteIdentityInputPort.deleteIdentity(DeleteIdentityCommand(IdentityId.of(id)))

}