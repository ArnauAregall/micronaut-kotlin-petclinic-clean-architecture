package tech.aaregall.lab.petclinic.identity.application.usecase

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import tech.aaregall.lab.petclinic.identity.application.ports.input.LoadIdentityCommand
import tech.aaregall.lab.petclinic.identity.application.ports.output.IdentityOutputPort
import tech.aaregall.lab.petclinic.identity.application.ports.usecase.LoadIdentityUseCaseImpl
import tech.aaregall.lab.petclinic.identity.domain.model.Identity
import tech.aaregall.lab.petclinic.identity.domain.model.IdentityId

@ExtendWith(MockKExtension::class)
internal class LoadIdentityUseCaseImplTest {

    @MockK
    lateinit var identityOutputPort: IdentityOutputPort

    @InjectMockKs
    lateinit var useCase: LoadIdentityUseCaseImpl

    @Nested
    inner class LoadIdentity {

        @Test
        fun `When output port returns empty then returns null` () {
            every { identityOutputPort.loadIdentityById(any()) } answers { null }

            val result = useCase.loadIdentity(LoadIdentityCommand(IdentityId.create()))

            assertThat(result).isNull()
        }

        @Test
        fun `When output port returns present then returns Identity` () {
            val identity = Identity(id = IdentityId.create(), firstName =  "John", lastName = "Doe")

            every { identityOutputPort.loadIdentityById(eq(identity.id)) } answers { identity }

            val result = useCase.loadIdentity(LoadIdentityCommand(identity.id))

            assertThat(result).isEqualTo(identity)
        }

    }
}