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
import tech.aaregall.lab.micronaut.petclinic.identity.domain.model.IdentityId

@ExtendWith(MockKExtension::class)
internal class IdentityServiceTest {

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
            val expectedIdentity = Identity(id = IdentityId.create(), firstName = "Foo", lastName = "Bar")

            every { identityOutputPort.createIdentity(any(Identity::class)) } answers { expectedIdentity }
            every { identityEventPublisher.publishIdentityCreatedEvent(any()) } answers { callOriginal() }

            val result = identityService.createIdentity(CreateIdentityCommand("Foo", "Bar"))

            assertThat(result).isEqualTo(expectedIdentity)

            verify { identityEventPublisher.publishIdentityCreatedEvent(IdentityCreatedEvent(result)) }
        }

    }

    @Nested
    @DisplayName("loadIdentityById")
    inner class LoadIdentityById {

        @Test
        fun `When output port returns empty then returns null` () {
            every { identityOutputPort.loadIdentityById(any()) } answers { null }

            val result = identityService.loadIdentity(LoadIdentityCommand(IdentityId.create()))

            assertThat(result).isNull()
        }

        @Test
        fun `When output port returns present then returns Identity` () {
            val identity = Identity(id = IdentityId.create(), firstName =  "John", lastName = "Doe")

            every { identityOutputPort.loadIdentityById(eq(identity.id)) } answers { identity }

            val result = identityService.loadIdentity(LoadIdentityCommand(identity.id))

            assertThat(result).isEqualTo(identity)
        }

    }



}