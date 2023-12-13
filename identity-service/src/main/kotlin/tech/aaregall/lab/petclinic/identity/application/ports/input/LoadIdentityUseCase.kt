package tech.aaregall.lab.petclinic.identity.application.ports.input

import tech.aaregall.lab.petclinic.identity.domain.model.Identity
import tech.aaregall.lab.petclinic.identity.domain.model.IdentityId

fun interface LoadIdentityUseCase {

    fun loadIdentity(loadIdentityCommand: LoadIdentityCommand): Identity?

}

data class LoadIdentityCommand(val identityId: IdentityId)