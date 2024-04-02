package tech.aaregall.lab.petclinic.vet.infrastructure.adapters.input.kafka

import io.micronaut.configuration.kafka.annotation.KafkaClient
import io.micronaut.configuration.kafka.annotation.KafkaKey
import io.micronaut.configuration.kafka.annotation.Topic
import io.micronaut.context.annotation.Replaces
import io.micronaut.context.annotation.Value
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
import tech.aaregall.lab.petclinic.vet.application.ports.input.LoadVetInputPort
import tech.aaregall.lab.petclinic.vet.domain.model.Vet
import tech.aaregall.lab.petclinic.vet.domain.model.VetId
import tech.aaregall.lab.petclinic.vet.infrastructure.adapters.input.kafka.VetIdentityKafkaConsumer.IdentityRecordBody
import java.time.Duration
import java.util.UUID.randomUUID

@MicronautTest
internal class VetIdentityKafkaConsumerIT {

    @Singleton
    @Replaces(LoadVetInputPort::class)
    fun mockLoadVetInputPort(): LoadVetInputPort = mockk()

    @Singleton
    @Replaces(DeleteVetInputPort::class)
    fun mockDeleteVetInputPort(): DeleteVetInputPort = mockk()

    @Value("\${app.ports.output.vet-id-validation.required-identity-role-name}")
    lateinit var requiredIdentityRoleName: String

    @BeforeEach
    fun beforeEach() = clearAllMocks()

    @KafkaClient
    fun interface IdentityTopicProducer {

        @Topic("identity")
        fun produce(@KafkaKey key: String, @MessageHeader("X-Action") actionHeader: String, @Nullable body: Any?)
    }

    @Test
    fun `Should consume records and do not call DeleteVetInputPort when LoadVetInputPort returns null`(
        loadVetInputPort: LoadVetInputPort,
        deleteVetInputPort: DeleteVetInputPort,
        identityTopicProducer: IdentityTopicProducer) {

        val key = randomUUID()

        every { loadVetInputPort.loadVet(VetId.of(key)) } answers { null }

        identityTopicProducer.produce(key.toString(), "DELETE", null)

        await().atMost(Duration.ofSeconds(5)).until { true }

        verify { loadVetInputPort.loadVet(VetId.of(key)) }
        verify (exactly = 0) { deleteVetInputPort.deleteVet(DeleteVetCommand(VetId.of(key))) }
    }

    @Test
    fun `Should consume records and call DeleteVetInputPort when LoadVetInputPort returns Vet, record X-Action is UPDATE, and Roles does not contain required role`(
        loadVetInputPort: LoadVetInputPort,
        deleteVetInputPort: DeleteVetInputPort,
        identityTopicProducer: IdentityTopicProducer) {

        val key = randomUUID()

        every { loadVetInputPort.loadVet(VetId.of(key)) } answers { Vet(id = args.first() as VetId) }
        every { deleteVetInputPort.deleteVet(any()) } answers { nothing }

        identityTopicProducer.produce(
            key.toString(), "UPDATE",
            IdentityRecordBody(roles = setOf("foo", "bar", "baz"))
        )

        await().atMost(Duration.ofSeconds(5)).until { true }

        verify { loadVetInputPort.loadVet(VetId.of(key)) }
        verify { deleteVetInputPort.deleteVet(DeleteVetCommand(VetId.of(key))) }
    }

    @Test
    fun `Should consume records and do not call DeleteVetInputPort when LoadVetInputPort returns Vet, record X-Action is UPDATE, and Roles contains required role`(
        loadVetInputPort: LoadVetInputPort,
        deleteVetInputPort: DeleteVetInputPort,
        identityTopicProducer: IdentityTopicProducer) {

        val key = randomUUID()

        every { loadVetInputPort.loadVet(VetId.of(key)) } answers { Vet(id = args.first() as VetId) }

        identityTopicProducer.produce(
            key.toString(), "UPDATE",
            IdentityRecordBody(roles = setOf("foo", "bar", requiredIdentityRoleName))
        )

        await().atMost(Duration.ofSeconds(5)).until { true }

        verify { loadVetInputPort.loadVet(VetId.of(key)) }
        verify (exactly = 0) { deleteVetInputPort.deleteVet(DeleteVetCommand(VetId.of(key))) }
    }

    @Test
    fun `Should consume records and call DeleteVetInputPort when LoadVetInputPort returns Vet and record X-Action is DELETE`(
        loadVetInputPort: LoadVetInputPort,
        deleteVetInputPort: DeleteVetInputPort,
        identityTopicProducer: IdentityTopicProducer) {

        val key = randomUUID()

        every { loadVetInputPort.loadVet(VetId.of(key)) } answers { Vet(id = args.first() as VetId) }
        every { deleteVetInputPort.deleteVet(any()) } answers { nothing }

        identityTopicProducer.produce(key.toString(), "DELETE", null)

        await().atMost(Duration.ofSeconds(5)).until { true }

        verify { loadVetInputPort.loadVet(VetId.of(key)) }
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