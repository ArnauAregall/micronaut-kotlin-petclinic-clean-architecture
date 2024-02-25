package tech.aaregall.lab.petclinic.identity.domain.usecase

import tech.aaregall.lab.petclinic.common.UseCase
import tech.aaregall.lab.petclinic.identity.application.ports.input.CreateRoleCommand
import tech.aaregall.lab.petclinic.identity.application.ports.input.CreateRoleCommandException
import tech.aaregall.lab.petclinic.identity.application.ports.input.CreateRoleUseCase
import tech.aaregall.lab.petclinic.identity.application.ports.output.RoleOutputPort
import tech.aaregall.lab.petclinic.identity.domain.model.Role
import tech.aaregall.lab.petclinic.identity.domain.model.RoleId

@UseCase
internal class CreateRoleUseCaseImpl(private val roleOutputPort: RoleOutputPort): CreateRoleUseCase {

    override fun createRole(createRoleCommand: CreateRoleCommand): Role {
        if (roleOutputPort.roleExistsByName(createRoleCommand.name)) {
            throw CreateRoleCommandException("Role with name '${createRoleCommand.name}' already exists")
        }

        return roleOutputPort.createRole(Role(id = RoleId.create(), name = createRoleCommand.name))
    }
}