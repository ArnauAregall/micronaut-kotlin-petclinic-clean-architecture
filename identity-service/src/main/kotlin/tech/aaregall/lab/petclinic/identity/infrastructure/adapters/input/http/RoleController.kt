package tech.aaregall.lab.petclinic.identity.infrastructure.adapters.input.http

import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpResponse.created
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import jakarta.validation.Valid
import tech.aaregall.lab.petclinic.identity.application.ports.input.CreateRoleInputPort
import tech.aaregall.lab.petclinic.identity.infrastructure.adapters.input.http.dto.request.CreateRoleRequest
import tech.aaregall.lab.petclinic.identity.infrastructure.adapters.input.http.dto.response.RoleResponse
import tech.aaregall.lab.petclinic.identity.infrastructure.adapters.input.http.mapper.RoleHttpMapper

@Controller("/api/roles")
private open class RoleController(
    private val createRoleInputPort: CreateRoleInputPort,
    private val roleHttpMapper: RoleHttpMapper) {

    @Post
    open fun createRole(@Body @Valid createRoleRequest: CreateRoleRequest): HttpResponse<RoleResponse> =
        created(roleHttpMapper.mapToResponse(
            createRoleInputPort.createRole(roleHttpMapper.mapCreateRequestToCommand(createRoleRequest))))


}