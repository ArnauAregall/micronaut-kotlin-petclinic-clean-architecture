package tech.aaregall.lab.petclinic.identity.application.ports.input

import tech.aaregall.lab.petclinic.identity.domain.model.IdentityId
import tech.aaregall.lab.petclinic.identity.domain.model.RoleId

fun interface AssignRoleToIdentityUseCase {

    fun assignRoleToIdentity(assignRoleToIdentityCommand: AssignRoleToIdentityCommand)

}

data class AssignRoleToIdentityCommand(val identityId: IdentityId, val roleId: RoleId)

class AssignRoleToIdentityCommandException(message: String, cause: Throwable? = null) : IllegalStateException("Failed assigning Role to Identity: $message", cause)