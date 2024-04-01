package tech.aaregall.lab.petclinic.identity.application.ports.output

import tech.aaregall.lab.petclinic.identity.domain.event.IdentityCreatedEvent
import tech.aaregall.lab.petclinic.identity.domain.event.IdentityDeletedEvent
import tech.aaregall.lab.petclinic.identity.domain.event.IdentityUpdatedEvent

interface IdentityEventPublisher {

    fun publishIdentityCreatedEvent(identityCreatedEvent: IdentityCreatedEvent)

    fun publishIdentityUpdatedEvent(identityUpdatedEvent: IdentityUpdatedEvent)

    fun publishIdentityDeletedEvent(identityDeletedEvent: IdentityDeletedEvent)

}