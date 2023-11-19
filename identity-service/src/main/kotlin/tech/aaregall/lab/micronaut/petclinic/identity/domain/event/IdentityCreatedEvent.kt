package tech.aaregall.lab.micronaut.petclinic.identity.domain.event

import tech.aaregall.lab.micronaut.petclinic.identity.domain.model.Identity
import java.time.Instant

data class IdentityCreatedEvent(val identity: Identity) {

    val date : Instant = Instant.now()

}