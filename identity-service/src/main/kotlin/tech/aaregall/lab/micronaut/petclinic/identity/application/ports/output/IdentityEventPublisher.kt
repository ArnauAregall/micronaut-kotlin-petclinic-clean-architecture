package tech.aaregall.lab.micronaut.petclinic.identity.application.ports.output

import tech.aaregall.lab.micronaut.petclinic.identity.domain.event.IdentityCreatedEvent

interface IdentityEventPublisher {

    fun publishIdentityCreatedEvent(identityCreatedEvent: IdentityCreatedEvent)

}