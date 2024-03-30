package tech.aaregall.lab.petclinic.vet.infrastructure.adapters.input.kafka

import io.micronaut.configuration.kafka.annotation.KafkaKey
import io.micronaut.configuration.kafka.annotation.KafkaListener
import io.micronaut.configuration.kafka.annotation.OffsetReset
import io.micronaut.configuration.kafka.annotation.Topic
import io.micronaut.messaging.annotation.MessageHeader
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory.getLogger
import tech.aaregall.lab.petclinic.vet.application.ports.input.DeleteVetCommand
import tech.aaregall.lab.petclinic.vet.application.ports.input.DeleteVetCommandException
import tech.aaregall.lab.petclinic.vet.application.ports.input.DeleteVetInputPort
import tech.aaregall.lab.petclinic.vet.domain.model.VetId
import java.util.*

@Singleton
@KafkaListener(offsetReset = OffsetReset.EARLIEST)
internal class VetIdentityKafkaConsumer(private val deleteVetInputPort: DeleteVetInputPort) {

    private val logger = getLogger(this::class.java)

    @Topic("identity")
    fun consumeIdentityTopic(@KafkaKey key: String, @MessageHeader("X-Action") actionHeader: String) {
        when (actionHeader) {
            "DELETE" -> {
                val vetId = VetId(UUID.fromString(key))
                try {
                    deleteVetInputPort.deleteVet(DeleteVetCommand(vetId))
                } catch (e: DeleteVetCommandException) {
                    logger.warn("Failed deleting Vet with ID $vetId: ${e.message}")
                }
            }
            else -> logger.info("Ignoring action $actionHeader for record with ID $key")
        }
    }

}