package tech.aaregall.lab.petclinic.identity.infrastructure.adapters.input.http.mapper

import jakarta.inject.Singleton
import tech.aaregall.lab.petclinic.identity.application.ports.input.CreateRoleCommand
import tech.aaregall.lab.petclinic.identity.domain.model.Role
import tech.aaregall.lab.petclinic.identity.infrastructure.adapters.input.http.dto.request.CreateRoleRequest
import tech.aaregall.lab.petclinic.identity.infrastructure.adapters.input.http.dto.response.RoleResponse

@Singleton
class RoleHttpMapper {

    fun mapCreateRequestToCommand(createRoleRequest: CreateRoleRequest): CreateRoleCommand =
        CreateRoleCommand(createRoleRequest.name)

    fun mapToResponse(role: Role): RoleResponse =
        RoleResponse(role.id.toString(), role.name)

}