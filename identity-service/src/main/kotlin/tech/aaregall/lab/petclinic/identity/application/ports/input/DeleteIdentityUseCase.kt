package tech.aaregall.lab.petclinic.identity.application.ports.input

import tech.aaregall.lab.petclinic.identity.domain.model.IdentityId

fun interface DeleteIdentityUseCase {

    fun deleteIdentity(deleteIdentityCommand: DeleteIdentityCommand)

}

data class DeleteIdentityCommand(val identityId: IdentityId)