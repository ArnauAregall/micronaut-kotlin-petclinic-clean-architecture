package tech.aaregall.lab.micronaut.petclinic.identity.application.ports.input

import tech.aaregall.lab.micronaut.petclinic.identity.domain.model.Identity
import tech.aaregall.lab.micronaut.petclinic.identity.domain.model.IdentityId

fun interface LoadIdentityUseCase {

    fun loadIdentity(loadIdentityCommand: LoadIdentityCommand): Identity?

}

data class LoadIdentityCommand(val identityId: IdentityId)