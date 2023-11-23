package tech.aaregall.lab.micronaut.petclinic.identity.application.ports.input

import tech.aaregall.lab.micronaut.petclinic.identity.domain.model.Identity

fun interface CreateIdentityUseCase {

    fun createIdentity(createIdentityCommand: CreateIdentityCommand): Identity

}

data class CreateIdentityCommand(val firstName: String, val lastName: String)