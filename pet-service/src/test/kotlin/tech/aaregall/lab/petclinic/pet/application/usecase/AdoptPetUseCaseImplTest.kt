package tech.aaregall.lab.petclinic.pet.application.usecase

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
import tech.aaregall.lab.petclinic.pet.application.ports.input.AdoptPetCommand
import tech.aaregall.lab.petclinic.pet.application.ports.input.AdoptPetCommandException
import tech.aaregall.lab.petclinic.pet.application.ports.output.LoadPetOwnerCommand
import tech.aaregall.lab.petclinic.pet.application.ports.output.PetOutputPort
import tech.aaregall.lab.petclinic.pet.application.ports.output.PetOwnerOutputPort
import tech.aaregall.lab.petclinic.pet.application.ports.usecase.AdoptPetUseCaseImpl
import tech.aaregall.lab.petclinic.pet.domain.model.Pet
import tech.aaregall.lab.petclinic.pet.domain.model.PetId
import tech.aaregall.lab.petclinic.pet.domain.model.PetOwner
import tech.aaregall.lab.petclinic.pet.domain.model.PetType
import java.time.LocalDate
import java.util.UUID.randomUUID

@ExtendWith(MockKExtension::class)
internal class AdoptPetUseCaseImplTest {

    @MockK
    lateinit var petOutputPort: PetOutputPort

    @MockK
    lateinit var petOwnerOutputPort: PetOwnerOutputPort

    @InjectMockKs
    lateinit var useCase: AdoptPetUseCaseImpl

    @Nested
    inner class AdoptPet {

        @Test
        fun `It should return an errored UnitReactive when the PetOutputPort fails to load the Pet`() {
            val petId = PetId.create()

            every { petOutputPort.loadPetById(petId) } answers { UnitReactive.error(IllegalStateException("Cannot load Pet")) }

            val result = useCase.adoptPet(AdoptPetCommand(petId, randomUUID()))

            assertThatCode { result.block() }
                .isInstanceOf(IllegalStateException::class.java)
                .hasMessage("Cannot load Pet")

            verify { petOutputPort.loadPetById(petId) }
            verify (exactly = 0) { petOwnerOutputPort.loadPetOwner(any()) }
            verify (exactly = 0) { petOutputPort.updatePet(any()) }
        }

        @Test
        fun `It should return an errored UnitReactive when the PetOwnerOutputPort fails to load the PetOwner`() {
            val petId = PetId.create()
            val ownerIdentityId = randomUUID()

            every { petOutputPort.loadPetById(petId) } answers {
                UnitReactive(
                    Pet(id = petId, type = PetType.CAT, name = "Garfield", birthDate = LocalDate.now())
                )
            }

            every { petOwnerOutputPort.loadPetOwner(any()) } answers { UnitReactive.error(IllegalStateException("Cannot load PetOwner")) }

            val result = useCase.adoptPet(AdoptPetCommand(petId, ownerIdentityId))

            assertThatCode { result.block() }
                .isInstanceOf(IllegalStateException::class.java)
                .hasMessage("Cannot load PetOwner")

            verify { petOutputPort.loadPetById(petId) }
            verify { petOwnerOutputPort.loadPetOwner(LoadPetOwnerCommand(ownerIdentityId)) }
            verify (exactly = 0) { petOutputPort.updatePet(any()) }
        }

        @Test
        fun `It should return an errored UnitReactive when the PetOwnerOutputPort returns an empty PetOwner`() {
            val petId = PetId.create()
            val ownerIdentityId = randomUUID()

            every { petOutputPort.loadPetById(petId) } answers {
                UnitReactive(
                    Pet(id = petId, type = PetType.CAT, name = "Garfield", birthDate = LocalDate.now())
                )
            }

            every { petOwnerOutputPort.loadPetOwner(any()) } answers { UnitReactive.empty() }

            val result = useCase.adoptPet(AdoptPetCommand(petId, ownerIdentityId))

            assertThatCode { result.block() }
                .isInstanceOf(AdoptPetCommandException::class.java)
                .hasMessageContaining("Failed to adopt Pet")
                .hasMessageContaining("Could not load the adopter PetOwner with ID")
                .hasMessageContaining(ownerIdentityId.toString())

            verify { petOutputPort.loadPetById(petId) }
            verify { petOwnerOutputPort.loadPetOwner(LoadPetOwnerCommand(ownerIdentityId)) }
            verify (exactly = 0) { petOutputPort.updatePet(any()) }
        }

        @Test
        fun `It should return a UnitReactive with the updated Pet when the adopter PetOwner is loaded`() {
            val petId = PetId.create()
            val ownerIdentityId = randomUUID()

            val mockPetOwner = PetOwner(identityId = ownerIdentityId, firstName = "Jonathan Q", lastName = "Arbuckle")

            every { petOutputPort.loadPetById(petId) } answers {
                UnitReactive(
                    Pet(id = petId, type = PetType.CAT, name = "Garfield", birthDate = LocalDate.now())
                )
            }

            every { petOwnerOutputPort.loadPetOwner(LoadPetOwnerCommand(ownerIdentityId)) } answers {
                UnitReactive(
                    mockPetOwner
                )
            }

            every { petOutputPort.updatePet(any(Pet::class)) } answers { UnitReactive(args.first() as Pet) }

            val result = useCase.adoptPet(AdoptPetCommand(petId, ownerIdentityId))

            assertThat(result.block()!!)
                .isNotNull
                .isInstanceOf(Pet::class.java)
                .satisfies({
                    assertThat(it.owner)
                        .isNotNull
                        .isEqualTo(mockPetOwner)
                })

            verify { petOutputPort.loadPetById(petId) }
            verify { petOwnerOutputPort.loadPetOwner(LoadPetOwnerCommand(ownerIdentityId)) }
            verify { petOutputPort.updatePet(any(Pet::class)) }
        }

    }


}