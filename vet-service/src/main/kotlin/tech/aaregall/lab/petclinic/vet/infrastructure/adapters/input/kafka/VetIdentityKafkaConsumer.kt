package tech.aaregall.lab.petclinic.vet.infrastructure.adapters.input.kafka

import io.micronaut.configuration.kafka.annotation.KafkaKey
import io.micronaut.configuration.kafka.annotation.KafkaListener
import io.micronaut.configuration.kafka.annotation.OffsetReset
import io.micronaut.configuration.kafka.annotation.Topic
import io.micronaut.context.annotation.Value
import io.micronaut.core.annotation.Introspected
import io.micronaut.messaging.annotation.MessageBody
import io.micronaut.messaging.annotation.MessageHeader
import io.micronaut.serde.annotation.Serdeable
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory.getLogger
import tech.aaregall.lab.petclinic.vet.application.ports.input.DeleteVetCommand
import tech.aaregall.lab.petclinic.vet.application.ports.input.DeleteVetInputPort
import tech.aaregall.lab.petclinic.vet.application.ports.input.LoadVetInputPort
import tech.aaregall.lab.petclinic.vet.domain.model.VetId

@Singleton
@KafkaListener(offsetReset = OffsetReset.EARLIEST)
internal class VetIdentityKafkaConsumer(
    private val loadVetInputPort: LoadVetInputPort,
    private val deleteVetInputPort: DeleteVetInputPort,
    @Value("\${app.ports.output.vet-id-validation.required-identity-role-name}") private val requiredIdentityRoleName: String) {

    private val logger = getLogger(this::class.java)

    @Topic("identity")
    fun consumeIdentityTopic(@KafkaKey key: String, @MessageHeader("X-Action") actionHeader: String, @MessageBody body: IdentityRecordBody? = null) {
        val vet = loadVetInputPort.loadVet(VetId.of(key)) ?: return

        when (actionHeader) {
            "UPDATE" ->
                if (body?.hasVetRole(requiredIdentityRoleName) == false)
                    deleteVetInputPort.deleteVet(DeleteVetCommand(vet.id))
                        .also { logger.info("Deleted Vet with ID '${vet.id}' as the consumed record from Kafka was an update and Identity no longer had role '${requiredIdentityRoleName}'")  }
            "DELETE" ->
                deleteVetInputPort.deleteVet(DeleteVetCommand(vet.id))
                    .also { logger.info("Deleted Vet with ID '${vet.id}'")  }
            else -> logger.info("Ignoring action $actionHeader for record with ID $key")
        }
    }

    @Introspected
    @Serdeable
    data class IdentityRecordBody(val roles: Set<String>) {
        fun hasVetRole(vetRole: String): Boolean = roles.contains(vetRole)
    }

}