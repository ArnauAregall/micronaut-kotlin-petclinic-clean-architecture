package tech.aaregall.lab.petclinic.identity.application.ports.usecase

import tech.aaregall.lab.petclinic.common.UseCase
import tech.aaregall.lab.petclinic.identity.application.ports.input.AssignRoleToIdentityCommand
import tech.aaregall.lab.petclinic.identity.application.ports.input.AssignRoleToIdentityCommandException
import tech.aaregall.lab.petclinic.identity.application.ports.input.AssignRoleToIdentityInputPort
import tech.aaregall.lab.petclinic.identity.application.ports.output.IdentityOutputPort
import tech.aaregall.lab.petclinic.identity.application.ports.output.RoleOutputPort

@UseCase
internal class AssignRoleToIdentityUseCase(
    private val identityOutputPort: IdentityOutputPort,
    private val roleOutputPort: RoleOutputPort
) : AssignRoleToIdentityInputPort {

    override fun assignRoleToIdentity(assignRoleToIdentityCommand: AssignRoleToIdentityCommand) {
        val identity = identityOutputPort.loadIdentityById(assignRoleToIdentityCommand.identityId)
        require(identity != null) { "Cannot assign roles to a non existing Identity" }

        val role = roleOutputPort.loadRoleById(assignRoleToIdentityCommand.roleId)
        require(role != null) { "Cannot assign a non existing Role to Identity ${identity.id}" }

        if (identity.hasRole(role)) {
            throw AssignRoleToIdentityCommandException("Identity ${identity.id} already has Role ${role.id} assigned")
        }

        roleOutputPort.assignRoleToIdentity(identity, role)
    }
}