package tech.aaregall.lab.petclinic.pet.domain.usecase

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import tech.aaregall.lab.petclinic.common.reactive.UnitReactive
import tech.aaregall.lab.petclinic.pet.application.ports.input.CreatePetCommand
import tech.aaregall.lab.petclinic.pet.application.ports.input.CreatePetCommandException
import tech.aaregall.lab.petclinic.pet.application.ports.output.LoadPetOwnerCommand
import tech.aaregall.lab.petclinic.pet.application.ports.output.PetOutputPort
import tech.aaregall.lab.petclinic.pet.application.ports.output.PetOwnerOutputPort
import tech.aaregall.lab.petclinic.pet.domain.model.Pet
import tech.aaregall.lab.petclinic.pet.domain.model.PetOwner
import tech.aaregall.lab.petclinic.pet.domain.model.PetType
import java.time.LocalDate
import java.util.UUID.randomUUID

@ExtendWith(MockKExtension::class)
internal class CreatePetUseCaseImplTest {
    
    @MockK
    lateinit var petOutputPort: PetOutputPort
    
    @MockK
    lateinit var petOwnerOutputPort: PetOwnerOutputPort
    
    @InjectMockKs
    lateinit var useCase: CreatePetUseCaseImpl

    @Nested
    inner class CreatePet {

        private fun mockCreatePetOutputPort() =
            every { petOutputPort.createPet(any(Pet::class)) } answers {
                val argPet = it.invocation.args.first() as Pet
                UnitReactive(
                    Pet(
                    id = argPet.id,
                    type = argPet.type,
                    name = argPet.name,
                    birthDate = argPet.birthDate,
                    owner = argPet.owner
                )
                )
            }

        @Test
        fun `Creates a Pet without Owner`() {
            mockCreatePetOutputPort()

            val result = useCase.createPet(
                CreatePetCommand(
                    type = PetType.CAT, name = "Peebles", birthDate = LocalDate.now(), ownerIdentityId = null
                )
            )

            val createdPet: Pet = result.block()!!

            verify (exactly = 0) { petOwnerOutputPort.loadPetOwner(any()) }
            verify { petOutputPort.createPet(createdPet) }

            assertThat(createdPet)
                .isNotNull
                .extracting(Pet::type, Pet::name, Pet::birthDate, Pet::owner)
                .containsExactly(PetType.CAT, "Peebles", LocalDate.now(), null)
        }

        @Test
        fun `Should throw a CreatePetCommandException when PetOwnerOutputPort returns empty PetOwner`() {
            val ownerIdentityId = randomUUID()

            every { petOwnerOutputPort.loadPetOwner(LoadPetOwnerCommand(ownerIdentityId)) } answers { UnitReactive.empty() }

            val result = useCase.createPet(
                CreatePetCommand(
                    type = PetType.DOG,
                    name = "Bimo",
                    birthDate = LocalDate.now(),
                    ownerIdentityId = ownerIdentityId
                )
            )

            assertThatCode { result.block() }
                .isInstanceOf(CreatePetCommandException::class.java)
                .hasMessageContaining("Failed to create Pet")
                .hasMessageContaining("Could not load the PetOwner with ID $ownerIdentityId")
        }

        @Test
        fun `Creates a Pet with Owner when PetOwner exists`() {
            mockCreatePetOutputPort()

            every { petOwnerOutputPort.loadPetOwner(any(LoadPetOwnerCommand::class)) } answers {
                UnitReactive(PetOwner((it.invocation.args.first() as LoadPetOwnerCommand).ownerIdentityId))
            }

            val ownerIdentityId = randomUUID()
            val result = useCase.createPet(
                CreatePetCommand(
                    type = PetType.DOG, name = "Bimo", birthDate = LocalDate.now(), ownerIdentityId = ownerIdentityId
                )
            )

            val createdPet: Pet = result.block()!!

            verify { petOwnerOutputPort.loadPetOwner(LoadPetOwnerCommand(ownerIdentityId)) }
            verify { petOutputPort.createPet(createdPet) }

            assertThat(result.block()!!)
                .isNotNull
                .extracting(Pet::type, Pet::name, Pet::birthDate, Pet::owner)
                .containsExactly(PetType.DOG, "Bimo", LocalDate.now(), PetOwner(ownerIdentityId))
        }

    }
    
}