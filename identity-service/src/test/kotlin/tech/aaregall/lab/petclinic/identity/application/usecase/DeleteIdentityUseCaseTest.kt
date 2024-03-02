package tech.aaregall.lab.petclinic.identity.application.usecase

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import tech.aaregall.lab.petclinic.identity.application.ports.input.DeleteIdentityCommand
import tech.aaregall.lab.petclinic.identity.application.ports.output.IdentityEventPublisher
import tech.aaregall.lab.petclinic.identity.application.ports.output.IdentityOutputPort
import tech.aaregall.lab.petclinic.identity.application.ports.usecase.DeleteIdentityUseCase
import tech.aaregall.lab.petclinic.identity.domain.event.IdentityDeletedEvent
import tech.aaregall.lab.petclinic.identity.domain.model.Identity
import tech.aaregall.lab.petclinic.identity.domain.model.IdentityId

@ExtendWith(MockKExtension::class)
internal class DeleteIdentityUseCaseTest {

    @MockK
    lateinit var identityOutputPort: IdentityOutputPort

    @MockK
    lateinit var identityEventPublisher: IdentityEventPublisher

    @InjectMockKs
    lateinit var useCase: DeleteIdentityUseCase

    @Nested
    inner class DeleteIdentity {

        @Test
        fun `Call output port and publishes an IdentityDeletedEvent when Identity exists`() {
            val identityId = IdentityId.create()

            every { identityOutputPort.loadIdentityById(identityId) } answers { Identity(id = args.first() as IdentityId, firstName =  "John", lastName = "Doe") }
            every { identityOutputPort.deleteIdentity(any()) } answers { nothing }
            every { identityEventPublisher.publishIdentityDeletedEvent(any()) } answers { nothing }

            useCase.deleteIdentity(DeleteIdentityCommand(identityId))

            verify { identityEventPublisher.publishIdentityDeletedEvent(IdentityDeletedEvent(identityId)) }
        }

        @Test
        fun `Throws IllegalArgumentException when Identity does not exists`() {
            val identityId = IdentityId.create()

            every { identityOutputPort.loadIdentityById(identityId) } answers { null }
            every { identityOutputPort.deleteIdentity(any()) } answers { nothing }

            assertThatCode {
                useCase.deleteIdentity(DeleteIdentityCommand(identityId))
            }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage("Cannot delete a non existing Identity")
        }

    }

}