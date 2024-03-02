package tech.aaregall.lab.petclinic.pet.application.usecase

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import tech.aaregall.lab.petclinic.common.reactive.UnitReactive
import tech.aaregall.lab.petclinic.pet.application.ports.input.LoadPetCommand
import tech.aaregall.lab.petclinic.pet.application.ports.output.LoadPetOwnerCommand
import tech.aaregall.lab.petclinic.pet.application.ports.output.PetOutputPort
import tech.aaregall.lab.petclinic.pet.application.ports.output.PetOwnerOutputPort
import tech.aaregall.lab.petclinic.pet.application.ports.usecase.LoadPetUseCaseImpl
import tech.aaregall.lab.petclinic.pet.domain.model.Pet
import tech.aaregall.lab.petclinic.pet.domain.model.PetId
import tech.aaregall.lab.petclinic.pet.domain.model.PetOwner
import tech.aaregall.lab.petclinic.pet.domain.model.PetType
import java.time.LocalDate
import java.util.UUID.randomUUID

@ExtendWith(MockKExtension::class)
internal class LoadPetUseCaseImplTest {

    @MockK
    lateinit var petOutputPort: PetOutputPort

    @MockK
    lateinit var petOwnerOutputPort: PetOwnerOutputPort

    @InjectMockKs
    lateinit var useCase: LoadPetUseCaseImpl

    @Nested
    inner class LoadPet {

        @Test
        fun `It should return a UnitReactive with the return of PetOutputPort when Pet has no PetOwner`() {
            val petId = PetId.create()

            every { petOutputPort.loadPetById(petId) } answers {
                UnitReactive(
                    Pet(id = petId, type = PetType.DOG, name = "Snoopy", birthDate = LocalDate.now())
                )
            }

            val result = useCase.loadPet(LoadPetCommand(petId))

            assertThat(result)
                .isInstanceOf(UnitReactive::class.java)
                .satisfies({
                    assertThat(it.block()!!)
                        .isNotNull
                        .extracting(Pet::id, Pet::type, Pet::name, Pet::birthDate, Pet::owner)
                        .containsExactly(
                            petId, PetType.DOG, "Snoopy", LocalDate.now(), null
                        )
                })

            verify { petOutputPort.loadPetById(petId) }
            verify (exactly = 0) { petOwnerOutputPort.loadPetOwner(any()) }
        }

        @Test
        fun `It should return a UnitReactive with the return of PetOutputPort and with PetOwner as the return of PetOwnerOutputPort when Pet has PetOwner`() {
            val petId = PetId.create()
            val petOwnerId = randomUUID()

            every { petOutputPort.loadPetById(petId) } answers {
                UnitReactive(
                    Pet(id = petId, type = PetType.CAT, name = "Silvester", birthDate = LocalDate.now(), owner = PetOwner(petOwnerId) )
                )
            }

            every { petOwnerOutputPort.loadPetOwner(LoadPetOwnerCommand(petOwnerId)) } answers {
                UnitReactive(PetOwner(identityId = petOwnerId, firstName = "John", lastName = "Doe"))
            }

            val result = useCase.loadPet(LoadPetCommand(petId))

            assertThat(result)
                .isInstanceOf(UnitReactive::class.java)
                .satisfies({
                    assertThat(it.block()!!)
                        .isNotNull
                        .extracting(Pet::id, Pet::type, Pet::name, Pet::birthDate, Pet::owner)
                        .containsExactly(
                            petId, PetType.CAT, "Silvester", LocalDate.now(), PetOwner(petOwnerId, "John", "Doe")
                        )
                })

            verify { petOutputPort.loadPetById(petId) }
            verify { petOwnerOutputPort.loadPetOwner(LoadPetOwnerCommand(petOwnerId)) }
        }

    }

}