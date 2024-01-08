package tech.aaregall.lab.petclinic.identity.application.ports.output

import tech.aaregall.lab.petclinic.identity.domain.event.IdentityCreatedEvent
import tech.aaregall.lab.petclinic.identity.domain.event.IdentityDeletedEvent

interface IdentityEventPublisher {

    fun publishIdentityCreatedEvent(identityCreatedEvent: IdentityCreatedEvent)

    fun publishIdentityDeletedEvent(identityDeletedEvent: IdentityDeletedEvent)

}