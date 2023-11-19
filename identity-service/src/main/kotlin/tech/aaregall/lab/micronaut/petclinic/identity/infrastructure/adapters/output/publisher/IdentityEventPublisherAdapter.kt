package tech.aaregall.lab.micronaut.petclinic.identity.infrastructure.adapters.output.publisher

import jakarta.inject.Singleton
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tech.aaregall.lab.micronaut.petclinic.identity.application.ports.output.IdentityEventPublisher
import tech.aaregall.lab.micronaut.petclinic.identity.domain.event.IdentityCreatedEvent

@Singleton
internal class IdentityEventPublisherAdapter: IdentityEventPublisher {

    companion object {
        val logger: Logger = LoggerFactory.getLogger(IdentityEventPublisherAdapter::class.java)
    }

    override fun publishIdentityCreatedEvent(identityCreatedEvent: IdentityCreatedEvent) {
        logger.info("Publishing Identity Created Event [identity={}, time={}]", identityCreatedEvent.identity, identityCreatedEvent.date)
    }
}