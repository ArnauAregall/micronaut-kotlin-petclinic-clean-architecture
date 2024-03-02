package tech.aaregall.lab.petclinic.identity.application.ports.usecase

import tech.aaregall.lab.petclinic.common.UseCase
import tech.aaregall.lab.petclinic.identity.application.ports.input.RevokeRoleFromIdentityCommand
import tech.aaregall.lab.petclinic.identity.application.ports.input.RevokeRoleFromIdentityCommandException
import tech.aaregall.lab.petclinic.identity.application.ports.input.RevokeRoleFromIdentityInputPort
import tech.aaregall.lab.petclinic.identity.application.ports.output.IdentityOutputPort
import tech.aaregall.lab.petclinic.identity.application.ports.output.RoleOutputPort

@UseCase
internal class RevokeRoleFromIdentityUseCase(
    private val identityOutputPort: IdentityOutputPort,
    private val roleOutputPort: RoleOutputPort
): RevokeRoleFromIdentityInputPort {

    override fun revokeRoleFromIdentity(revokeRoleFromIdentityCommand: RevokeRoleFromIdentityCommand) {
        val identity = identityOutputPort.loadIdentityById(revokeRoleFromIdentityCommand.identityId)
        require(identity != null) { "Cannot revoke a Role from a non existing Identity" }

        val role = roleOutputPort.loadRoleById(revokeRoleFromIdentityCommand.roleId)
        require(role != null) { "Cannot revoke a non existing Role from Identity ${identity.id}" }

        if (!identity.hasRole(role)) {
            throw RevokeRoleFromIdentityCommandException("Identity ${identity.id} does not have the Role ${role.id} assigned")
        }

        roleOutputPort.revokeRoleFromIdentity(identity, role)
    }
}