package tech.aaregall.lab.petclinic.identity.application.ports.input

import tech.aaregall.lab.petclinic.identity.domain.model.IdentityId
import tech.aaregall.lab.petclinic.identity.domain.model.RoleId

fun interface AssignRoleToIdentityUseCase {

    fun assignRoleToIdentity(assignRolesToIdentityCommand: AssignRolesToIdentityCommand)

}

data class AssignRolesToIdentityCommand(val identityId: IdentityId, val roleId: RoleId)

class AssignRolesToIdentityCommandException(message: String, cause: Throwable? = null) : IllegalStateException("Failed assigning Role to Identity: $message", cause)