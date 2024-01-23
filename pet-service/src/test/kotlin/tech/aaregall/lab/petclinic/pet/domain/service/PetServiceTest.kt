package tech.aaregall.lab.petclinic.pet.domain.service

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.groups.Tuple.tuple
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import tech.aaregall.lab.petclinic.common.reactive.CollectionReactive
import tech.aaregall.lab.petclinic.common.reactive.UnitReactive
import tech.aaregall.lab.petclinic.pet.application.ports.input.CreatePetCommand
import tech.aaregall.lab.petclinic.pet.application.ports.input.DeletePetsByPetOwnerCommand
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