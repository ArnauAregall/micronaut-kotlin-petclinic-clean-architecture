package tech.aaregall.lab.petclinic.identity.infrastructure.adapters.output.publisher

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.testcontainers.shaded.org.awaitility.Awaitility.await
import tech.aaregall.lab.petclinic.identity.spec.KafkaConsumerSpec
import tech.aaregall.lab.petclinic.identity.domain.event.IdentityCreatedEvent
import tech.aaregall.lab.petclinic.identity.domain.model.Identity
import tech.aaregall.lab.petclinic.identity.domain.model.IdentityId
import java.time.Duration

@MicronautTest
internal class IdentityEventPublisherAdapterIT(
    private val identityEventPublisherAdapter: tech.aaregall.lab.petclinic.identity.infrastructure.adapters.output.publisher.IdentityEventPublisherAdapter,
    private val kafkaConsumerSpec: KafkaConsumerSpec) {

    @AfterEach
    fun tearDown() = kafkaConsumerSpec.clear()

    @Nested
    inner class PublishIdentityCreatedEvent {

        @Test
        fun `Should publish and consume the event from Kafka 'identity' topic`() {
            val domainEvent = IdentityCreatedEvent(Identity(id = IdentityId.create(), firstName = "John", lastName = "Doe"));

            identityEventPublisherAdapter.publishIdentityCreatedEvent(domainEvent)

            await().atMost(Duration.ofSeconds(5)).until { kafkaConsumerSpec.hasConsumedRecords() }

            assertThat(kafkaConsumerSpec.get(domainEvent.identity.id))
                .isNotNull
                .extracting("firstName", "lastName")
                .containsExactly("John", "Doe")
        }

    }

}