package tech.aaregall.lab.petclinic.pet.infrastructure.adapters.input.kafka

import io.micronaut.configuration.kafka.annotation.KafkaKey
import io.micronaut.configuration.kafka.annotation.KafkaListener
import io.micronaut.configuration.kafka.annotation.OffsetReset
import io.micronaut.configuration.kafka.annotation.Topic
import io.micronaut.messaging.annotation.MessageHeader
import jakarta.inject.Singleton
import tech.aaregall.lab.petclinic.pet.application.ports.input.DeletePetsByPetOwnerCommand
import tech.aaregall.lab.petclinic.pet.application.ports.input.DeletePetsByPetOwnerUseCase
import tech.aaregall.lab.petclinic.pet.application.ports.output.PetOwnerOutputPort
import tech.aaregall.lab.petclinic.pet.domain.model.PetOwner
import java.util.UUID

@Singleton
@KafkaListener(offsetReset = OffsetReset.EARLIEST)
class IdentityKafkaConsumer(
    private val petOwnerOutputPort: PetOwnerOutputPort,
    private val deletePetsByPetOwnerUseCase: DeletePetsByPetOwnerUseCase) {

    @Topic("identity")
    fun consumeIdentityTopic(@KafkaKey key: String, @MessageHeader("X-Action") actionHeader: String) {
        when (actionHeader) {
            "DELETE" -> {
                val identityId = UUID.fromString(key)
                petOwnerOutputPort.deletePetOwner(PetOwner(identityId))
                deletePetsByPetOwnerUseCase.deletePetsByPetOwner(DeletePetsByPetOwnerCommand(identityId))
            }

            else -> println("Ignoring action $actionHeader for record with ID $key")
        }

    }

}