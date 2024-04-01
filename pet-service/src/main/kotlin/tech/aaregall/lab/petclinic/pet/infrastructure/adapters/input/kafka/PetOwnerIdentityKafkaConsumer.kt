package tech.aaregall.lab.petclinic.pet.infrastructure.adapters.input.kafka

import io.micronaut.configuration.kafka.annotation.KafkaKey
import io.micronaut.configuration.kafka.annotation.KafkaListener
import io.micronaut.configuration.kafka.annotation.OffsetReset
import io.micronaut.configuration.kafka.annotation.Topic
import io.micronaut.messaging.annotation.MessageHeader
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory.getLogger
import tech.aaregall.lab.petclinic.pet.application.ports.input.DeletePetsByPetOwnerCommand
import tech.aaregall.lab.petclinic.pet.application.ports.input.DeletePetsByPetOwnerInputPort
import tech.aaregall.lab.petclinic.pet.application.ports.output.PetOwnerOutputPort
import tech.aaregall.lab.petclinic.pet.domain.model.PetOwner
import java.util.UUID

@Singleton
@KafkaListener(offsetReset = OffsetReset.EARLIEST)
class PetOwnerIdentityKafkaConsumer(
    private val petOwnerOutputPort: PetOwnerOutputPort,
    private val deletePetsByPetOwnerInputPort: DeletePetsByPetOwnerInputPort) {

    private val logger = getLogger(this::class.java)

    @Topic("identity")
    fun consumeIdentityTopic(@KafkaKey key: String, @MessageHeader("X-Action") actionHeader: String) {
        when (actionHeader) {
            "DELETE" -> {
                val identityId = UUID.fromString(key)
                petOwnerOutputPort.deletePetOwner(PetOwner(identityId))
                deletePetsByPetOwnerInputPort.deletePetsByPetOwner(DeletePetsByPetOwnerCommand(identityId))
            }

            else -> logger.info("Ignoring action $actionHeader for record with ID $key")
        }

    }

}