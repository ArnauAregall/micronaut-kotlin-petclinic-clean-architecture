package tech.aaregall.lab.petclinic.vet.infrastructure.adapters.input.kafka

import io.micronaut.configuration.kafka.annotation.KafkaClient
import io.micronaut.configuration.kafka.annotation.KafkaKey
import io.micronaut.configuration.kafka.annotation.Topic
import io.micronaut.context.annotation.Replaces
import io.micronaut.messaging.annotation.MessageHeader
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import jakarta.annotation.Nullable
import jakarta.inject.Singleton
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.testcontainers.shaded.org.awaitility.Awaitility.await
import tech.aaregall.lab.petclinic.vet.application.ports.input.DeleteVetCommand
import tech.aaregall.lab.petclinic.vet.application.ports.input.DeleteVetInputPort
import tech.aaregall.lab.petclinic.vet.domain.model.VetId
import java.time.Duration
import java.util.UUID.randomUUID

@MicronautTest
internal class VetIdentityKafkaConsumerIT {

    @Singleton
    @Replaces(DeleteVetInputPort::class)
    fun mockInputPort(): DeleteVetInputPort = mockk()

    @BeforeEach
    fun beforeEach() = clearAllMocks()

    @KafkaClient
    fun interface IdentityTopicProducer {

        @Topic("identity")
        fun produce(@KafkaKey key: String, @MessageHeader("X-Action") actionHeader: String, @Nullable body: Any?)
    }

    @Test
    fun `Should consume records an call DeleteVetInputPort when record X-Action is DELETE`(
        identityTopicProducer: IdentityTopicProducer,
        deleteVetInputPort: DeleteVetInputPort) {

        every { deleteVetInputPort.deleteVet(any()) } answers { nothing }

        val key = randomUUID()

        identityTopicProducer.produce(key.toString(), "DELETE", null)

        await().atMost(Duration.ofSeconds(5)).until { true }

        verify { deleteVetInputPort.deleteVet(DeleteVetCommand(VetId.of(key))) }
    }

    @Test
    fun `Should consume records an not call DeleteVetInputPort when record X-Action is not DELETE`(
        identityTopicProducer: IdentityTopicProducer,
        deleteVetInputPort: DeleteVetInputPort) {

        identityTopicProducer.produce(randomUUID().toString(), "ANYTHING", null)

        await().atMost(Duration.ofSeconds(5)).until { true }

        verify (exactly = 0) { deleteVetInputPort.deleteVet(any()) }
    }

}