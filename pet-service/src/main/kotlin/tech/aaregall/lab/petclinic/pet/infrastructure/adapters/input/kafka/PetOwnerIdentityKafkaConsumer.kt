package tech.aaregall.lab.petclinic.pet.infrastructure.adapters.input.kafka

import io.micronaut.configuration.kafka.annotation.KafkaKey
import io.micronaut.configuration.kafka.annotation.KafkaListener
import io.micronaut.configuration.kafka.annotation.OffsetReset
import io.micronaut.configuration.kafka.annotation.Topic
import io.micronaut.messaging.annotation.MessageHeader
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory.getLogger
import tech.aaregall.lab.petclinic.pet.application.ports.input.DeletePetOwnerCommand
import tech.aaregall.lab.petclinic.pet.application.ports.input.DeletePetOwnerInputPort
import java.util.UUID

@Singleton
@KafkaListener(offsetReset = OffsetReset.EARLIEST)
internal class PetOwnerIdentityKafkaConsumer(private val deletePetOwnerInputPort: DeletePetOwnerInputPort) {

    private val logger = getLogger(this::class.java)

    @Topic("identity")
    fun consumeIdentityTopic(@KafkaKey key: String, @MessageHeader("X-Action") actionHeader: String) {
        when (actionHeader) {
            "DELETE" -> {
                deletePetOwnerInputPort.deletePetOwner(DeletePetOwnerCommand(ownerIdentityId = UUID.fromString(key)))
                    .also {
                        logger.info("Deleted PetOwner with ID '$key'")
                    }
            }

            else -> logger.info("Ignoring action $actionHeader for record key '$key'")
        }

    }

}