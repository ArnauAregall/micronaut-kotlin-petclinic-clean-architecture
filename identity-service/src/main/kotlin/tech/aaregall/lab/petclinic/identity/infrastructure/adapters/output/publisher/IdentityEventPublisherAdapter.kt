package tech.aaregall.lab.petclinic.identity.infrastructure.adapters.output.publisher

import io.micronaut.configuration.kafka.annotation.KafkaClient
import io.micronaut.configuration.kafka.annotation.KafkaKey
import io.micronaut.configuration.kafka.annotation.Topic
import io.micronaut.serde.annotation.Serdeable
import jakarta.inject.Singleton
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tech.aaregall.lab.petclinic.identity.application.ports.output.IdentityEventPublisher
import tech.aaregall.lab.petclinic.identity.domain.event.IdentityCreatedEvent
import tech.aaregall.lab.petclinic.identity.domain.model.Identity

@Singleton
internal class IdentityEventPublisherAdapter(private val identityKafkaClient: IdentityKafkaClient): IdentityEventPublisher {

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(IdentityEventPublisherAdapter::class.java)
    }

    override fun publishIdentityCreatedEvent(identityCreatedEvent: IdentityCreatedEvent) {
        logger.info("Publishing Identity Created Event [identity={}, time={}]", identityCreatedEvent.identity, identityCreatedEvent.date)
        identityKafkaClient.sendIdentityCreated(identityCreatedEvent.identity.id.toString(),
            toKafkaEvent(identityCreatedEvent.identity))
    }

    private fun toKafkaEvent(identity: Identity): IdentityCreatedKafkaEvent =
        IdentityCreatedKafkaEvent(
            firstName = identity.firstName,
            lastName = identity.lastName
        )
}

@Serdeable
internal data class IdentityCreatedKafkaEvent(val firstName: String, val lastName: String)

@KafkaClient
internal fun interface IdentityKafkaClient {

    @Topic("identity")
    fun sendIdentityCreated(@KafkaKey id: String, identityCreatedKafkaEvent: IdentityCreatedKafkaEvent)

}