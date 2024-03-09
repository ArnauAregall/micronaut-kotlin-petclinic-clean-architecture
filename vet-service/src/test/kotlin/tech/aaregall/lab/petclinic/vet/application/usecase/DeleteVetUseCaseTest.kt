package tech.aaregall.lab.petclinic.vet.application.usecase

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import tech.aaregall.lab.petclinic.vet.application.ports.input.DeleteVetCommand
import tech.aaregall.lab.petclinic.vet.application.ports.input.DeleteVetCommandException
import tech.aaregall.lab.petclinic.vet.application.ports.output.VetOutputPort
import tech.aaregall.lab.petclinic.vet.domain.model.Vet
import tech.aaregall.lab.petclinic.vet.domain.model.VetId

@ExtendWith(MockKExtension::class)
internal class DeleteVetUseCaseTest {

    @MockK
    lateinit var vetOutputPort: VetOutputPort

    @InjectMockKs
    lateinit var useCase: DeleteVetUseCase

    @Nested
    inner class DeleteVet {

        @Test
        fun `It should throw a DeleteVetCommandException when the Vet does not exist`() {
            val vetId = VetId.create()

            every { vetOutputPort.loadVet(vetId) } answers { null }

            assertThatCode { useCase.deleteVet(DeleteVetCommand(vetId)) }
                .isInstanceOf(DeleteVetCommandException::class.java)
                .hasMessageContaining("Failed to delete Vet")
                .hasMessageContaining("Cannot delete a non existing Vet")

            verify { vetOutputPort.loadVet(vetId) }
            verify (exactly = 0) { vetOutputPort.deleteVet(any()) }
        }

        @Test
        fun `It should call VetOutputPort when the Vet exists`() {
            val vet = Vet(id = VetId.create(), specialities = emptySet())

            every { vetOutputPort.loadVet(vet.id) } answers { vet }
            every { vetOutputPort.deleteVet(vet) } answers { nothing }

            assertThatCode { useCase.deleteVet(DeleteVetCommand(vet.id)) }
                .doesNotThrowAnyException()

            verify { vetOutputPort.loadVet(vet.id) }
            verify { vetOutputPort.deleteVet(vet) }
        }

    }

}