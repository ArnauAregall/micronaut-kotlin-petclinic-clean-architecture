package tech.aaregall.lab.petclinic.identity.application.ports.input

import tech.aaregall.lab.petclinic.identity.domain.model.IdentityId
import tech.aaregall.lab.petclinic.identity.domain.model.RoleId

fun interface RevokeRoleFromIdentityUseCase {

    fun revokeRoleFromIdentity(revokeRoleFromIdentityCommand: RevokeRoleFromIdentityCommand)

}

data class RevokeRoleFromIdentityCommand(val identityId: IdentityId, val roleId: RoleId)

class RevokeRoleFromIdentityCommandException(message: String, cause: Throwable? = null) : IllegalStateException("Failed revoking Role from Identity: $message", cause)