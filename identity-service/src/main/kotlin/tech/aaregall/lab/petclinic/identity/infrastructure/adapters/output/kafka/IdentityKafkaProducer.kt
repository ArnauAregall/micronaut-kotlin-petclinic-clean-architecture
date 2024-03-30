package tech.aaregall.lab.petclinic.identity.infrastructure.adapters.output.kafka

import io.micronaut.configuration.kafka.annotation.KafkaClient
import io.micronaut.configuration.kafka.annotation.KafkaKey
import io.micronaut.configuration.kafka.annotation.Topic
import io.micronaut.messaging.annotation.MessageHeader
import io.micronaut.serde.annotation.Serdeable
import jakarta.annotation.Nullable
import jakarta.inject.Singleton
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tech.aaregall.lab.petclinic.identity.application.ports.output.IdentityEventPublisher
import tech.aaregall.lab.petclinic.identity.domain.event.IdentityCreatedEvent
import tech.aaregall.lab.petclinic.identity.domain.event.IdentityDeletedEvent
import tech.aaregall.lab.petclinic.identity.domain.model.Identity

@Singleton
internal class IdentityKafkaProducer(private val identityKafkaClient: IdentityKafkaClient): IdentityEventPublisher {

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(IdentityKafkaProducer::class.java)
    }

    override fun publishIdentityCreatedEvent(identityCreatedEvent: IdentityCreatedEvent) {
        logger.info("Publishing Identity Created Event [identity={}, time={}]", identityCreatedEvent.identity, identityCreatedEvent.date)
        identityKafkaClient.sendIdentityCreated(identityCreatedEvent.identity.id.toString(),
            toKafkaEvent(identityCreatedEvent.identity))
    }

    override fun publishIdentityDeletedEvent(identityDeletedEvent: IdentityDeletedEvent) {
        logger.info("Publishing Identity Deleted Event [identityId={}, time={}]", identityDeletedEvent.identityId, identityDeletedEvent.date)
        identityKafkaClient.sendIdentityDeleted(identityDeletedEvent.identityId.toString(), null)
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
internal interface IdentityKafkaClient {

    companion object {
        private const val ACTION_HEADER: String = "X-Action"
    }

    @Topic("identity")
    @MessageHeader(name = ACTION_HEADER, value = "CREATE")
    fun sendIdentityCreated(@KafkaKey id: String, identityCreatedKafkaEvent: IdentityCreatedKafkaEvent)

    @Topic("identity")
    @MessageHeader(name = ACTION_HEADER, value = "DELETE")
    fun sendIdentityDeleted(@KafkaKey id: String, @Nullable body: Any?)

}