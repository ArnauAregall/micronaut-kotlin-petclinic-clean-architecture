package tech.aaregall.lab.petclinic.identity.infrastructure.adapters.output.kafka

import io.micronaut.configuration.kafka.annotation.KafkaClient
import io.micronaut.configuration.kafka.annotation.KafkaKey
import io.micronaut.configuration.kafka.annotation.Topic
import io.micronaut.messaging.annotation.MessageBody
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
        identityKafkaClient.produceRecord(identityCreatedEvent.identity.id.toString(), "CREATE",
            toRecord(identityCreatedEvent.identity))
    }

    override fun publishIdentityDeletedEvent(identityDeletedEvent: IdentityDeletedEvent) {
        logger.info("Publishing Identity Deleted Event [identityId={}, time={}]", identityDeletedEvent.identityId, identityDeletedEvent.date)
        identityKafkaClient.produceRecord(identityDeletedEvent.identityId.toString(), "DELETE")
    }

    private fun toRecord(identity: Identity): IdentityRecord =
        IdentityRecord(
            firstName = identity.firstName,
            lastName = identity.lastName,
            roles = identity.roles.orEmpty().map { it.name }.toList()
        )
}

@Serdeable
internal data class IdentityRecord(val firstName: String, val lastName: String, val roles: Collection<String>)

@KafkaClient
internal interface IdentityKafkaClient {

    companion object {
        private const val ACTION_HEADER: String = "X-Action"
    }

    @Topic("identity")
    fun produceRecord(@KafkaKey id: String,
                      @MessageHeader(name = ACTION_HEADER) action: String,
                      @MessageBody @Nullable identityRecord: IdentityRecord? = null)

}