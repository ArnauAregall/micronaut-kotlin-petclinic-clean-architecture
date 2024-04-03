package tech.aaregall.lab.petclinic.pet.infrastructure.adapters.input.kafka

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
import tech.aaregall.lab.petclinic.pet.application.ports.input.DeletePetOwnerCommand
import tech.aaregall.lab.petclinic.pet.application.ports.input.DeletePetOwnerInputPort
import java.time.Duration
import java.util.UUID.randomUUID

@MicronautTest
internal class PetOwnerIdentityKafkaConsumerIT {

    @Singleton
    @Replaces(DeletePetOwnerInputPort::class)
    fun mockDeletePetOwnerInputPort(): DeletePetOwnerInputPort = mockk()

    @BeforeEach
    fun beforeEach() = clearAllMocks()

    @KafkaClient
    fun interface IdentityProducer {

        @Topic("identity")
        fun produce(@KafkaKey key: String, @MessageHeader("X-Action") actionHeader: String, @Nullable body: Any?)
    }

    @Test
    fun `Should consume records and call PetOwnerOutputPort and DeletePetsByPetOwnerUseCase when record header X-Action is DELETE`(
        identityProducer: IdentityProducer, deletePetOwnerInputPort: DeletePetOwnerInputPort) {

        every { deletePetOwnerInputPort.deletePetOwner(any()) } answers { nothing }

        val key = randomUUID()

        identityProducer.produce(key.toString(), "DELETE", null)

        await().atMost(Duration.ofSeconds(5)).until { true }

        verify { deletePetOwnerInputPort.deletePetOwner(DeletePetOwnerCommand(key)) }
    }

    @Test
    fun `Should consume records and not call PetOwnerOutputPort nor DeletePetsByPetOwnerUseCase when record header X-Action is not DELETE`(
        identityProducer: IdentityProducer, deletePetOwnerInputPort: DeletePetOwnerInputPort) {

        identityProducer.produce(randomUUID().toString(), "ANYTHING", null)

        await().atMost(Duration.ofSeconds(5)).until { true }

        verify (exactly = 0) { deletePetOwnerInputPort.deletePetOwner(any()) }
    }

}