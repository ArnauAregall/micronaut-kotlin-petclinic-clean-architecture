package tech.aaregall.lab.petclinic.identity.domain.service

import tech.aaregall.lab.petclinic.common.UseCase
import tech.aaregall.lab.petclinic.identity.application.ports.input.AssignRoleToIdentityCommand
import tech.aaregall.lab.petclinic.identity.application.ports.input.AssignRoleToIdentityUseCase
import tech.aaregall.lab.petclinic.identity.application.ports.input.AssignRoleToIdentityCommandException
import tech.aaregall.lab.petclinic.identity.application.ports.input.CreateRoleCommand
import tech.aaregall.lab.petclinic.identity.application.ports.input.CreateRoleCommandException
import tech.aaregall.lab.petclinic.identity.application.ports.input.CreateRoleUseCase
import tech.aaregall.lab.petclinic.identity.application.ports.input.LoadIdentityCommand
import tech.aaregall.lab.petclinic.identity.application.ports.input.LoadIdentityUseCase
import tech.aaregall.lab.petclinic.identity.application.ports.output.RoleOutputPort
import tech.aaregall.lab.petclinic.identity.domain.model.Role
import tech.aaregall.lab.petclinic.identity.domain.model.RoleId

@UseCase
internal class RoleService(
    private val loadIdentityUseCase: LoadIdentityUseCase,
    private val roleOutputPort: RoleOutputPort
) : CreateRoleUseCase, AssignRoleToIdentityUseCase {

    override fun createRole(createRoleCommand: CreateRoleCommand): Role {
        if (roleOutputPort.roleExistsByName(createRoleCommand.name)) {
            throw CreateRoleCommandException("Role with name '${createRoleCommand.name}' already exists")
        }

        return roleOutputPort.createRole(Role(id = RoleId.create(), name = createRoleCommand.name))
    }

    override fun assignRoleToIdentity(assignRoleToIdentityCommand: AssignRoleToIdentityCommand) {
        val identity = loadIdentityUseCase.loadIdentity(LoadIdentityCommand(assignRoleToIdentityCommand.identityId))
        require(identity != null) { "Cannot assign roles to a non existing Identity" }

        val role = roleOutputPort.loadRoleById(assignRoleToIdentityCommand.roleId)
        require(role != null) { "Cannot assign a non existing Role to Identity ${identity.id}" }

        if (identity.hasRole(role)) {
            throw AssignRoleToIdentityCommandException("Identity ${identity.id} already has Role ${role.id} assigned")
        }

        roleOutputPort.assignRoleToIdentity(identity, role)
    }
}