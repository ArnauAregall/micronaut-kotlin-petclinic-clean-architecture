package tech.aaregall.lab.micronaut.petclinic.identity.application.ports.output

import tech.aaregall.lab.micronaut.petclinic.identity.domain.model.Identity
import tech.aaregall.lab.micronaut.petclinic.identity.domain.model.IdentityId

interface IdentityOutputPort {

    fun createIdentity(identity: Identity) : Identity

    fun loadIdentityById(identityId: IdentityId): Identity?

}