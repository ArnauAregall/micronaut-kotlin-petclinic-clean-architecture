package tech.aaregall.lab.micronaut.petclinic.identity.domain.service

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import tech.aaregall.lab.micronaut.petclinic.identity.application.ports.input.CreateIdentityCommand
import tech.aaregall.lab.micronaut.petclinic.identity.application.ports.input.LoadIdentityCommand
import tech.aaregall.lab.micronaut.petclinic.identity.application.ports.output.IdentityEventPublisher
import tech.aaregall.lab.micronaut.petclinic.identity.application.ports.output.IdentityOutputPort
import tech.aaregall.lab.micronaut.petclinic.identity.domain.event.IdentityCreatedEvent
import tech.aaregall.lab.micronaut.petclinic.identity.domain.model.Identity
import java.util.Optional
import java.util.UUID.randomUUID

@ExtendWith(MockKExtension::class)
class IdentityServiceTest {

    @MockK
    lateinit var identityOutputPort: IdentityOutputPort

    @MockK
    lateinit var identityEventPublisher: IdentityEventPublisher

    @InjectMockKs
    lateinit var identityService: IdentityService

    @Nested
    @DisplayName("CreateIdentity")
    inner class CreateIdentity {

        @Test
        fun `Creates Identity` () {
            val expectedIdentity = Identity("Foo", "Bar")

            every { identityOutputPort.createIdentity(any(Identity::class)) } answers { expectedIdentity }
            every { identityEventPublisher.publishIdentityCreatedEvent(any()) } answers { callOriginal() }

            val result = identityService.createIdentity(CreateIdentityCommand("Foo", "Bar"))

            assertThat(result).isEqualTo(expectedIdentity)

            verify { identityOutputPort.createIdentity(result) }
            verify { identityEventPublisher.publishIdentityCreatedEvent(IdentityCreatedEvent(result)) }
        }

    }

    @Nested
    @DisplayName("loadIdentityById")
    inner class LoadIdentityById {

        @Test
        fun `When output port returns empty then returns null` () {

            every { identityOutputPort.loadIdentityById(any()) } answers { Optional.empty() }

            val result = identityService.loadIdentity(LoadIdentityCommand(randomUUID()))

            assertThat(result).isNull()
        }

        @Test
        fun `When output port returns present then returns Identity` () {
            val id = randomUUID()
            val expectedIdentity = Identity("John", "Doe")

            every { identityOutputPort.loadIdentityById(eq(id)) } answers { Optional.of(expectedIdentity) }

            val result = identityService.loadIdentity(LoadIdentityCommand(id))

            assertThat(result).isEqualTo(expectedIdentity)
        }

    }



}