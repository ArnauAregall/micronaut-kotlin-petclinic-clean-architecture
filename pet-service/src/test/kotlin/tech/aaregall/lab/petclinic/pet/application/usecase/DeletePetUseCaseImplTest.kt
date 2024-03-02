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
import tech.aaregall.lab.petclinic.common.reactive.UnitReactive
import tech.aaregall.lab.petclinic.pet.application.ports.input.DeletePetCommand
import tech.aaregall.lab.petclinic.pet.application.ports.input.DeletePetCommandException
import tech.aaregall.lab.petclinic.pet.application.ports.output.PetOutputPort
import tech.aaregall.lab.petclinic.pet.application.ports.usecase.DeletePetUseCaseImpl
import tech.aaregall.lab.petclinic.pet.domain.model.Pet
import tech.aaregall.lab.petclinic.pet.domain.model.PetId
import tech.aaregall.lab.petclinic.pet.domain.model.PetType
import java.time.LocalDate

@ExtendWith(MockKExtension::class)
internal class DeletePetUseCaseImplTest {

    @MockK
    lateinit var petOutputPort: PetOutputPort

    @InjectMockKs
    lateinit var useCase: DeletePetUseCaseImpl

    @Nested
    inner class DeletePet {

        @Test
        fun `It should return an errored UnitReactive with a DeletePetCommandException when the PetOutputPort cannot load the Pet`() {
            val pet = Pet(PetId.create(), PetType.DOG, "Snoopy", LocalDate.now())

            every { petOutputPort.loadPetById(pet.id) } answers { UnitReactive.empty() }

            val result = useCase.deletePet(DeletePetCommand(pet.id))

            assertThatCode { result.block()!! }
                .isInstanceOf(DeletePetCommandException::class.java)
                .hasMessageContaining("Failed deleting Pet with ID ${pet.id}")
                .hasMessageContaining("Pet was not found")

            verify { petOutputPort.loadPetById(pet.id) }
            verify (exactly = 0) { petOutputPort.deletePet(pet) }
        }

        @Test
        fun `It should return an errored UnitReactive with a DeletePetCommandException when the PetOutputPort can load a Pet but cannot delete it`() {
            val pet = Pet(PetId.create(), PetType.DOG, "Marshall", LocalDate.now())

            every { petOutputPort.loadPetById(pet.id) } answers { UnitReactive(pet) }
            every { petOutputPort.deletePet(pet) } answers { UnitReactive(false) }

            val result = useCase.deletePet(DeletePetCommand(pet.id))

            assertThatCode { result.block()!! }
                .isInstanceOf(DeletePetCommandException::class.java)
                .hasMessageContaining("Failed deleting Pet with ID ${pet.id}")
                .hasMessageContaining("Pet cannot be deleted")

            verify { petOutputPort.loadPetById(pet.id) }
            verify { petOutputPort.deletePet(pet) }
        }

        @Test
        fun `It should return a UnitReactive that does not error when the PetOutputPort can load a Pet and can delete it`() {
            val pet = Pet(PetId.create(), PetType.DOG, "Rubble", LocalDate.now())

            every { petOutputPort.loadPetById(pet.id) } answers { UnitReactive(pet) }
            every { petOutputPort.deletePet(pet) } answers { UnitReactive(true) }

            val result = useCase.deletePet(DeletePetCommand(pet.id))

            assertThatCode { result.block()!! }
                .doesNotThrowAnyException()

            verify { petOutputPort.loadPetById(pet.id) }
            verify { petOutputPort.deletePet(pet) }
        }

    }

}