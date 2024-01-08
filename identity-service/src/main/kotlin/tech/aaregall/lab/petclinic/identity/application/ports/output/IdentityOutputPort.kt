package tech.aaregall.lab.petclinic.identity.application.ports.output

import tech.aaregall.lab.petclinic.identity.domain.model.Identity
import tech.aaregall.lab.petclinic.identity.domain.model.IdentityId

interface IdentityOutputPort {

    fun createIdentity(identity: Identity) : Identity

    fun loadIdentityById(identityId: IdentityId): Identity?

    fun deleteIdentity(identity: Identity)

}