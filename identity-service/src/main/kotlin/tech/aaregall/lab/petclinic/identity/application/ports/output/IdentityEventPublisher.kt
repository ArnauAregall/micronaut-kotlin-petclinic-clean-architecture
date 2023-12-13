package tech.aaregall.lab.petclinic.identity.application.ports.output

import tech.aaregall.lab.petclinic.identity.domain.event.IdentityCreatedEvent

fun interface IdentityEventPublisher {

    fun publishIdentityCreatedEvent(identityCreatedEvent: IdentityCreatedEvent)

}