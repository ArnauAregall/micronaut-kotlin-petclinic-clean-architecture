package tech.aaregall.lab.petclinic.pet.domain.service

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.groups.Tuple.tuple
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import tech.aaregall.lab.petclinic.common.reactive.CollectionReactive
import tech.aaregall.lab.petclinic.common.reactive.UnitReactive
import tech.aaregall.lab.petclinic.pet.application.ports.input.CreatePetCommand
import tech.aaregall.lab.petclinic.pet.application.ports.input.DeletePetsByPetOwnerCommand
import tech.aaregall.lab.petclinic.pet.application.ports.input.LoadPetCommand
import tech.aaregall.lab.petclinic.pet.application.ports.input.SearchPetsCommand
import tech.aaregall.lab.petclinic.pet.application.ports.output.LoadPetOwnerCommand
import tech.aaregall.lab.petclinic.pet.application.ports.output.PetOutputPort
import tech.aaregall.lab.petclinic.pet.application.ports.output.PetOwnerOutputPort
import tech.aaregall.lab.petclinic.pet.domain.model.Pet
import tech.aaregall.lab.petclinic.pet.domain.model.PetId
import tech.aaregall.lab.petclinic.pet.domain.model.PetOwner
import tech.aaregall.lab.petclinic.pet.domain.model.PetType.CAT
import tech.aaregall.lab.petclinic.pet.domain.model.PetType.DOG
import tech.aaregall.lab.petclinic.pet.domain.model.PetType.RABBIT
import java.time.LocalDate
import java.util.UUID.randomUUID
import kotlin.random.Random
import kotlin.random.nextLong

@ExtendWith(MockKExtension::class)
internal class PetServiceTest {

    @MockK
    lateinit var petOutputPort: PetOutputPort

    @MockK
    lateinit var petOwnerOutputPort: PetOwnerOutputPort

    @InjectMockKs
    lateinit var petService: PetService

    @Nested
    inner class SearchPets {

        @Nested
        @DisplayName("When Pets do not have a PetOwner")
        inner class WithoutPetOwners {

            @Test
            fun `Should call PetOutputPort findPets with page arguments`() {
                every { petOutputPort.findPets(any(Int::class), any(Int::class)) } answers { CollectionReactive() }

                val result = petService.searchPets(SearchPetsCommand(0, 20))

                assertThat(result)
                    .isInstanceOf(CollectionReactive::class.java)
                    .satisfies({
                        assertThat(it.toFlux().collectList().block())
                            .isEmpty()
                    })

                verify { petOutputPort.findPets(0, 20) }
                verify (exactly = 0) { petOwnerOutputPort.loadPetOwner(any()) }
            }

            @Test
            fun `Should return a CollectionReactive containing Pets returned by PetOutputPort`() {
                every { petOutputPort.findPets(any(Int::class), any(Int::class)) } answers {
                    CollectionReactive(
                        Pet(id = PetId.create(), type = DOG, name = "Snoopy", birthDate = LocalDate.now()),
                        Pet(id = PetId.create(), type = CAT, name = "Garfield", birthDate = LocalDate.now()),
                        Pet(id = PetId.create(), type = RABBIT, name = "Bugs Bunny", birthDate = LocalDate.now())
                    )
                }

                val result = petService.searchPets(SearchPetsCommand(0, 3))

                assertThat(result)
                    .isInstanceOf(CollectionReactive::class.java)
                    .satisfies({
                        assertThat(it.toFlux().collectList().block())
                            .isNotEmpty
                            .hasSize(3)
                            .extracting(Pet::type, Pet::name)
                            .containsExactly(
                                tuple(DOG, "Snoopy"),
                                tuple(CAT, "Garfield"),
                                tuple(RABBIT, "Bugs Bunny")
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
                        Pet(id = PetId.create(), type = DOG, name = "Snoopy", birthDate = LocalDate.now(), owner = petOwner1),
                        Pet(id = PetId.create(), type = CAT, name = "Garfield", birthDate = LocalDate.now(), owner = petOwner2),
                        Pet(id = PetId.create(), type = RABBIT, name = "Bugs Bunny", birthDate = LocalDate.now(), owner = petOwner2)
                    )
                }

                every { petOwnerOutputPort.loadPetOwner(any()) } answers {
                    val ownerIdentityId = (args[0] as LoadPetOwnerCommand).ownerIdentityId
                    UnitReactive(loadedPetOwner(PetOwner(ownerIdentityId)))
                }

                val result = petService.searchPets(SearchPetsCommand(0, 3))

                assertThat(result)
                    .isInstanceOf(CollectionReactive::class.java)
                    .satisfies({
                        assertThat(it.toFlux().collectList().block())
                            .isNotEmpty
                            .hasSize(3)
                            .extracting(Pet::type, Pet::name, Pet::owner)
                            .containsExactly(
                                tuple(DOG, "Snoopy", loadedPetOwner(petOwner1)),
                                tuple(CAT, "Garfield", loadedPetOwner(petOwner2)),
                                tuple(RABBIT, "Bugs Bunny", loadedPetOwner(petOwner2))
                            )
                    })

                verify { petOutputPort.findPets(0, 3) }
                verify { petOwnerOutputPort.loadPetOwner(LoadPetOwnerCommand(petOwner1.identityId)) }
                verify { petOwnerOutputPort.loadPetOwner(LoadPetOwnerCommand(petOwner2.identityId)) }
            }

        }


    }

    @Nested
    inner class LoadPet {

        @Test
        fun `It should return a UnitReactive with the return of PetOutputPort when Pet has no PetOwner`() {
            val petId = PetId.create()

            every { petOutputPort.loadPetById(petId) } answers {
                UnitReactive(
                    Pet(id = petId, type = DOG, name = "Snoopy", birthDate = LocalDate.now())
                )
            }

            val result = petService.loadPet(LoadPetCommand(petId))

            assertThat(result)
                .isInstanceOf(UnitReactive::class.java)
                .satisfies({
                    assertThat(it.toMono().block())
                        .isNotNull
                        .extracting(Pet::id, Pet::type, Pet::name, Pet::birthDate, Pet::owner)
                        .containsExactly(
                            petId, DOG, "Snoopy", LocalDate.now(), null
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
                    Pet(id = petId, type = CAT, name = "Silvester", birthDate = LocalDate.now(), owner = PetOwner(petOwnerId) )
                )
            }

            every { petOwnerOutputPort.loadPetOwner(LoadPetOwnerCommand(petOwnerId)) } answers {
                UnitReactive(PetOwner(identityId = petOwnerId, firstName = "John", lastName = "Doe"))
            }

            val result = petService.loadPet(LoadPetCommand(petId))

            assertThat(result)
                .isInstanceOf(UnitReactive::class.java)
                .satisfies({
                    assertThat(it.toMono().block())
                        .isNotNull
                        .extracting(Pet::id, Pet::type, Pet::name, Pet::birthDate, Pet::owner)
                        .containsExactly(
                            petId, CAT, "Silvester", LocalDate.now(), PetOwner(petOwnerId, "John", "Doe")
                        )
                })

            verify { petOutputPort.loadPetById(petId) }
            verify { petOwnerOutputPort.loadPetOwner(LoadPetOwnerCommand(petOwnerId)) }
        }

    }

    @Nested
    inner class CountAllPets {

        @Test
        fun `Should return a UnitReactive with the exact same value returned by PetOutputPort`() {
            val fakeCount = Random.nextLong(LongRange(10, 1000))

            every { petOutputPort.countAllPets() } answers { UnitReactive(fakeCount) }

            val result = petService.countAllPets()

            assertThat(result.toMono().block())
                .isEqualTo(fakeCount)
        }

    }

    @Nested
    inner class CreatePet {

        private fun mockCreatePetOutputPort() =
            every { petOutputPort.createPet(any(Pet::class)) } answers {
                val argPet = it.invocation.args.first() as Pet
                UnitReactive(Pet(
                    id = argPet.id,
                    type = argPet.type,
                    name = argPet.name,
                    birthDate = argPet.birthDate,
                    owner = argPet.owner
                ))
            }

        @Test
        fun `Creates a Pet without Owner`() {
            mockCreatePetOutputPort()

            val result = petService.createPet(
                CreatePetCommand(
                    type = CAT, name = "Peebles", birthDate = LocalDate.now(), ownerIdentityId = null
                )
            )

            val createdPet: Pet = result.toMono().block()!!

            verify (exactly = 0) { petOwnerOutputPort.loadPetOwner(any()) }
            verify { petOutputPort.createPet(createdPet) }

            assertThat(createdPet)
                .isNotNull
                .extracting(Pet::type, Pet::name, Pet::birthDate, Pet::owner)
                .containsExactly(CAT, "Peebles", LocalDate.now(), null)
        }

        @Test
        fun `Creates a Pet with Owner`() {
            mockCreatePetOutputPort()

            every { petOwnerOutputPort.loadPetOwner(any(LoadPetOwnerCommand::class)) } answers {
                UnitReactive(PetOwner((it.invocation.args.first() as LoadPetOwnerCommand).ownerIdentityId))
            }

            val ownerIdentityId = randomUUID()
            val result = petService.createPet(
                CreatePetCommand(
                    type = DOG, name = "Bimo", birthDate = LocalDate.now(), ownerIdentityId = ownerIdentityId
                )
            )

            val createdPet: Pet = result.toMono().block()!!

            verify { petOwnerOutputPort.loadPetOwner(LoadPetOwnerCommand(ownerIdentityId)) }
            verify { petOutputPort.createPet(createdPet) }

            assertThat(result.toMono().block())
                .isNotNull
                .extracting(Pet::type, Pet::name, Pet::birthDate, Pet::owner)
                .containsExactly(DOG, "Bimo", LocalDate.now(), PetOwner(ownerIdentityId))
        }

    }

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