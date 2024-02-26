package tech.aaregall.lab.petclinic.pet.domain.service

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import tech.aaregall.lab.petclinic.pet.application.ports.input.DeletePetsByPetOwnerCommand
import tech.aaregall.lab.petclinic.pet.application.ports.output.PetOutputPort
import tech.aaregall.lab.petclinic.pet.domain.model.PetOwner
import java.util.UUID.randomUUID

@ExtendWith(MockKExtension::class)
internal class PetServiceTest {

    @MockK
    lateinit var petOutputPort: PetOutputPort

    @InjectMockKs
    lateinit var petService: PetService

    @Nested
    inner class DeletePetsByPetOwner {

        @Test
        fun `Should call PetOutputPort`() {
            every { petOutputPort.deletePetsByPetOwner(any()) } answers { nothing }

            val command = DeletePetsByPetOwnerCommand(randomUUID())

            petService.deletePetsByPetOwner(command)

            verify { petOutputPort.deletePetsByPetOwner(PetOwner(command.ownerIdentityId)) }
        }

    }

}