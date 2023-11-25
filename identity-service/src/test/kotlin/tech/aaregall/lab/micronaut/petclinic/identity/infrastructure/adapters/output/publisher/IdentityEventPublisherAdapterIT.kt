package tech.aaregall.lab.micronaut.petclinic.identity.infrastructure.adapters.output.publisher

import io.micronaut.configuration.kafka.annotation.KafkaKey
import io.micronaut.configuration.kafka.annotation.KafkaListener
import io.micronaut.configuration.kafka.annotation.OffsetReset
import io.micronaut.configuration.kafka.annotation.Topic
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.testcontainers.shaded.org.awaitility.Awaitility.await
import tech.aaregall.lab.micronaut.petclinic.identity.domain.event.IdentityCreatedEvent
import tech.aaregall.lab.micronaut.petclinic.identity.domain.model.Identity
import java.time.Duration
import java.util.concurrent.ConcurrentLinkedDeque

@MicronautTest
class IdentityEventPublisherAdapterIT {

    companion object {
        private val events: MutableCollection<IdentityCreatedKafkaEvent> = ConcurrentLinkedDeque()
    }

    @KafkaListener(offsetReset = OffsetReset.EARLIEST)
    internal class Consumer {

        @Topic("identity")
        fun consume(@KafkaKey id: String, identityCreatedKafkaEvent: IdentityCreatedKafkaEvent) {
            events.add(identityCreatedKafkaEvent)
        }

    }

    @Inject
    internal lateinit var identityEventPublisherAdapter: IdentityEventPublisherAdapter

    @AfterEach
    fun tearDown() = events.clear()

    @Nested
    inner class PublishIdentityCreatedEvent {

        @Test
        fun `Should publish and consume the event from Kafka 'identity' topic`() {
            identityEventPublisherAdapter.publishIdentityCreatedEvent(
                IdentityCreatedEvent(Identity(firstName = "John", "Doe")))

            await().atMost(Duration.ofSeconds(5)).until { events.isNotEmpty() }

            val event = events.iterator().next()

            assertThat(event)
                .isNotNull
                .extracting("firstName", "lastName")
                .containsExactly("John", "Doe")
        }

    }

}