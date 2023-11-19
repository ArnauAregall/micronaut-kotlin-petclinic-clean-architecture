package tech.aaregall.lab.micronaut.petclinic.identity.application.ports.output

import tech.aaregall.lab.micronaut.petclinic.identity.domain.model.Identity
import java.util.Optional
import java.util.UUID

interface IdentityOutputPort {

    fun createIdentity(identity: Identity) : Identity

    fun loadIdentityById(id: UUID): Optional<Identity>

}