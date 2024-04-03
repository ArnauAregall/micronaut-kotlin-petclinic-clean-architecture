package tech.aaregall.lab.petclinic.pet.application.usecase

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import tech.aaregall.lab.petclinic.pet.application.ports.input.DeletePetOwnerCommand
import tech.aaregall.lab.petclinic.pet.application.ports.output.PetOutputPort
import tech.aaregall.lab.petclinic.pet.application.ports.output.PetOwnerOutputPort
import tech.aaregall.lab.petclinic.pet.domain.model.PetOwner
import java.util.UUID.randomUUID

@ExtendWith(MockKExtension::class)
internal class DeletePetOwnerUseCaseTest {

    @MockK
    lateinit var petOwnerOutputPort: PetOwnerOutputPort

    @MockK
    lateinit var petOutputPort: PetOutputPort

    @InjectMockKs
    lateinit var useCase: DeletePetOwnerUseCase

    @Nested
    inner class DeletePetOwner {

        @Test
        fun `Should call PetOwnerOutputPort and PetOutputPort delete operations`() {
            every { petOwnerOutputPort.deletePetOwner(any()) } answers { nothing }
            every { petOutputPort.deletePetsByPetOwner(any()) } answers { nothing }

            val ownerIdentityId = randomUUID()

            assertThatCode { useCase.deletePetOwner(DeletePetOwnerCommand(ownerIdentityId))}
                .doesNotThrowAnyException()

            verify { petOwnerOutputPort.deletePetOwner(PetOwner(ownerIdentityId)) }
            verify { petOutputPort.deletePetsByPetOwner(PetOwner(ownerIdentityId)) }
        }

    }

}