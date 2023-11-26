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
import tech.aaregall.lab.micronaut.petclinic.identity.domain.model.IdentityId
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

@MicronautTest
class IdentityEventPublisherAdapterIT {

    companion object {
        private val consumedEventsMap: MutableMap<IdentityId, IdentityCreatedKafkaEvent> = ConcurrentHashMap()
    }

    @KafkaListener(offsetReset = OffsetReset.EARLIEST)
    internal class Consumer {

        @Topic("identity")
        fun consume(@KafkaKey id: String, identityCreatedKafkaEvent: IdentityCreatedKafkaEvent) {
            consumedEventsMap[IdentityId.of(id)] = identityCreatedKafkaEvent
        }

    }

    @Inject
    internal lateinit var identityEventPublisherAdapter: IdentityEventPublisherAdapter

    @AfterEach
    fun tearDown() = consumedEventsMap.clear()

    @Nested
    inner class PublishIdentityCreatedEvent {

        @Test
        fun `Should publish and consume the event from Kafka 'identity' topic`() {
            val domainEvent = IdentityCreatedEvent(Identity(id = IdentityId.create(), firstName = "John", lastName = "Doe"));

            identityEventPublisherAdapter.publishIdentityCreatedEvent(domainEvent)

            await().atMost(Duration.ofSeconds(5)).until { consumedEventsMap.isNotEmpty() }

            assertThat(consumedEventsMap[domainEvent.identity.id])
                .isNotNull
                .extracting("firstName", "lastName")
                .containsExactly("John", "Doe")
        }

    }

}