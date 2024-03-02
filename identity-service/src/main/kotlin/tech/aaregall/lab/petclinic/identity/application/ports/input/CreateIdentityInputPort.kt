package tech.aaregall.lab.petclinic.identity.application.ports.input

import tech.aaregall.lab.petclinic.identity.domain.model.Identity

fun interface CreateIdentityInputPort {

    fun createIdentity(createIdentityCommand: CreateIdentityCommand): Identity

}

data class CreateIdentityCommand(val firstName: String, val lastName: String)