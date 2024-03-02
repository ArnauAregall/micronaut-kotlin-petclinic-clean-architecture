package tech.aaregall.lab.petclinic.identity.application.usecase

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import tech.aaregall.lab.petclinic.identity.application.ports.input.CreateIdentityCommand
import tech.aaregall.lab.petclinic.identity.application.ports.output.IdentityEventPublisher
import tech.aaregall.lab.petclinic.identity.application.ports.output.IdentityOutputPort
import tech.aaregall.lab.petclinic.identity.application.ports.usecase.CreateIdentityUseCaseImpl
import tech.aaregall.lab.petclinic.identity.domain.event.IdentityCreatedEvent
import tech.aaregall.lab.petclinic.identity.domain.model.Identity
import tech.aaregall.lab.petclinic.identity.domain.model.IdentityId

@ExtendWith(MockKExtension::class)
internal class CreateIdentityUseCaseImplTest {

    @MockK
    lateinit var identityOutputPort: IdentityOutputPort

    @MockK
    lateinit var identityEventPublisher: IdentityEventPublisher

    @InjectMockKs
    lateinit var useCase: CreateIdentityUseCaseImpl

    @Nested
    inner class CreateIdentity {

        @Test
        fun `It should create an Identity an publish an event` () {
            val expectedIdentity = Identity(id = IdentityId.create(), firstName = "Foo", lastName = "Bar")

            every { identityOutputPort.createIdentity(any(Identity::class)) } answers { expectedIdentity }
            every { identityEventPublisher.publishIdentityCreatedEvent(any()) } answers { callOriginal() }

            val result = useCase.createIdentity(CreateIdentityCommand("Foo", "Bar"))

            assertThat(result).isEqualTo(expectedIdentity)

            verify { identityEventPublisher.publishIdentityCreatedEvent(IdentityCreatedEvent(result)) }
        }

    }

}