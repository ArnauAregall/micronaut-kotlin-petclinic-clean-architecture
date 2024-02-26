package tech.aaregall.lab.petclinic.pet.domain.usecase

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.groups.Tuple
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import tech.aaregall.lab.petclinic.common.reactive.CollectionReactive
import tech.aaregall.lab.petclinic.common.reactive.UnitReactive
import tech.aaregall.lab.petclinic.pet.application.ports.input.SearchPetsCommand
import tech.aaregall.lab.petclinic.pet.application.ports.output.LoadPetOwnerCommand
import tech.aaregall.lab.petclinic.pet.application.ports.output.PetOutputPort
import tech.aaregall.lab.petclinic.pet.application.ports.output.PetOwnerOutputPort
import tech.aaregall.lab.petclinic.pet.domain.model.Pet
import tech.aaregall.lab.petclinic.pet.domain.model.PetId
import tech.aaregall.lab.petclinic.pet.domain.model.PetOwner
import tech.aaregall.lab.petclinic.pet.domain.model.PetType
import java.time.LocalDate
import java.util.UUID.randomUUID

@ExtendWith(MockKExtension::class)
internal class SearchPetsUseCaseImplTest {

    @MockK
    lateinit var petOutputPort: PetOutputPort

    @MockK
    lateinit var petOwnerOutputPort: PetOwnerOutputPort

    @InjectMockKs
    lateinit var useCase: SearchPetsUseCaseImpl

    @Nested
    inner class SearchPets {

        @Nested
        @DisplayName("When Pets do not have a PetOwner")
        inner class WithoutPetOwners {

            @Test
            fun `Should call PetOutputPort findPets with page arguments`() {
                every { petOutputPort.findPets(any(Int::class), any(Int::class)) } answers { CollectionReactive() }

                val result = useCase.searchPets(SearchPetsCommand(0, 20))

                assertThat(result)
                    .isInstanceOf(CollectionReactive::class.java)
                    .satisfies({
                        assertThat(it.blockList())
                            .isEmpty()
                    })

                verify { petOutputPort.findPets(0, 20) }
                verify (exactly = 0) { petOwnerOutputPort.loadPetOwner(any()) }
            }

            @Test
            fun `Should return a CollectionReactive containing Pets returned by PetOutputPort`() {
                every { petOutputPort.findPets(any(Int::class), any(Int::class)) } answers {
                    CollectionReactive(
                        Pet(id = PetId.create(), type = PetType.DOG, name = "Snoopy", birthDate = LocalDate.now()),
                        Pet(id = PetId.create(), type = PetType.CAT, name = "Garfield", birthDate = LocalDate.now()),
                        Pet(id = PetId.create(), type = PetType.RABBIT, name = "Bugs Bunny", birthDate = LocalDate.now())
                    )
                }

                val result = useCase.searchPets(SearchPetsCommand(0, 3))

                assertThat(result)
                    .isInstanceOf(CollectionReactive::class.java)
                    .satisfies({
                        assertThat(it.blockList())
                            .isNotEmpty
                            .hasSize(3)
                            .extracting(Pet::type, Pet::name)
                            .containsExactly(
                                Tuple.tuple(PetType.DOG, "Snoopy"),
                                Tuple.tuple(PetType.CAT, "Garfield"),
                                Tuple.tuple(PetType.RABBIT, "Bugs Bunny")
                            )
                    })

                verify { petOutputPort.findPets(0, 3) }
                verify (exactly = 0) { petOwnerOutputPort.loadPetOwner(any()) }
            }

        }

        @Nested
        @DisplayName("When Pets have PetOwner")
        inner class WithPetOwners {

            private fun loadedPetOwner(petOwner: PetOwner): PetOwner =
                PetOwner(petOwner.identityId, "First Name ${petOwner.identityId}", "Last Name ${petOwner.identityId}")

            @Test
            fun `Should return a CollectionReactive containing Pets returned by PetOutputPort with PetOwners loaded from PetOwnerOutputPort only once when those are repeated`() {
                val petOwner1 = PetOwner(randomUUID())
                val petOwner2 = PetOwner(randomUUID())

                every { petOutputPort.findPets(any(Int::class), any(Int::class)) } answers {
                    CollectionReactive(
                        Pet(id = PetId.create(), type = PetType.DOG, name = "Snoopy", birthDate = LocalDate.now(), owner = petOwner1),
                        Pet(id = PetId.create(), type = PetType.CAT, name = "Garfield", birthDate = LocalDate.now(), owner = petOwner2),
                        Pet(id = PetId.create(), type = PetType.RABBIT, name = "Bugs Bunny", birthDate = LocalDate.now(), owner = petOwner2)
                    )
                }

                every { petOwnerOutputPort.loadPetOwner(any()) } answers {
                    val ownerIdentityId = (args[0] as LoadPetOwnerCommand).ownerIdentityId
                    UnitReactive(loadedPetOwner(PetOwner(ownerIdentityId)))
                }

                val result = useCase.searchPets(SearchPetsCommand(0, 3))

                assertThat(result)
                    .isInstanceOf(CollectionReactive::class.java)
                    .satisfies({
                        assertThat(it.blockList())
                            .isNotEmpty
                            .hasSize(3)
                            .extracting(Pet::type, Pet::name, Pet::owner)
                            .containsExactly(
                                Tuple.tuple(PetType.DOG, "Snoopy", loadedPetOwner(petOwner1)),
                                Tuple.tuple(PetType.CAT, "Garfield", loadedPetOwner(petOwner2)),
                                Tuple.tuple(PetType.RABBIT, "Bugs Bunny", loadedPetOwner(petOwner2))
                            )
                    })

                verify { petOutputPort.findPets(0, 3) }
                verify { petOwnerOutputPort.loadPetOwner(LoadPetOwnerCommand(petOwner1.identityId)) }
                verify { petOwnerOutputPort.loadPetOwner(LoadPetOwnerCommand(petOwner2.identityId)) }
            }

        }


    }

}