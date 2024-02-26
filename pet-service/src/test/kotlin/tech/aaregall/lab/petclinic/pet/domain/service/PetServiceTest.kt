package tech.aaregall.lab.petclinic.pet.domain.service

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
import tech.aaregall.lab.petclinic.pet.application.ports.input.CreatePetCommand
import tech.aaregall.lab.petclinic.pet.application.ports.input.CreatePetCommandException
import tech.aaregall.lab.petclinic.pet.application.ports.input.DeletePetCommand
import tech.aaregall.lab.petclinic.pet.application.ports.input.DeletePetCommandException
import tech.aaregall.lab.petclinic.pet.application.ports.input.DeletePetsByPetOwnerCommand
import tech.aaregall.lab.petclinic.pet.application.ports.input.LoadPetCommand
import tech.aaregall.lab.petclinic.pet.application.ports.output.LoadPetOwnerCommand
import tech.aaregall.lab.petclinic.pet.application.ports.output.PetOutputPort
import tech.aaregall.lab.petclinic.pet.application.ports.output.PetOwnerOutputPort
import tech.aaregall.lab.petclinic.pet.domain.model.Pet
import tech.aaregall.lab.petclinic.pet.domain.model.PetId
import tech.aaregall.lab.petclinic.pet.domain.model.PetOwner
import tech.aaregall.lab.petclinic.pet.domain.model.PetType.CAT
import tech.aaregall.lab.petclinic.pet.domain.model.PetType.DOG
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
                    assertThat(it.block()!!)
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
                    assertThat(it.block()!!)
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

            assertThat(result.block()!!)
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

            val createdPet: Pet = result.block()!!

            verify (exactly = 0) { petOwnerOutputPort.loadPetOwner(any()) }
            verify { petOutputPort.createPet(createdPet) }

            assertThat(createdPet)
                .isNotNull
                .extracting(Pet::type, Pet::name, Pet::birthDate, Pet::owner)
                .containsExactly(CAT, "Peebles", LocalDate.now(), null)
        }

        @Test
        fun `Should throw a CreatePetCommandException when PetOwnerOutputPort returns empty PetOwner`() {
            val ownerIdentityId = randomUUID()

            every { petOwnerOutputPort.loadPetOwner(LoadPetOwnerCommand(ownerIdentityId)) } answers { UnitReactive.empty() }

            val result = petService.createPet(
                CreatePetCommand(
                    type = DOG,
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
            val result = petService.createPet(
                CreatePetCommand(
                    type = DOG, name = "Bimo", birthDate = LocalDate.now(), ownerIdentityId = ownerIdentityId
                )
            )

            val createdPet: Pet = result.block()!!

            verify { petOwnerOutputPort.loadPetOwner(LoadPetOwnerCommand(ownerIdentityId)) }
            verify { petOutputPort.createPet(createdPet) }

            assertThat(result.block()!!)
                .isNotNull
                .extracting(Pet::type, Pet::name, Pet::birthDate, Pet::owner)
                .containsExactly(DOG, "Bimo", LocalDate.now(), PetOwner(ownerIdentityId))
        }

    }

    @Nested
    inner class AdoptPet {

        @Test
        fun `It should return an errored UnitReactive when the PetOutputPort fails to load the Pet`() {
            val petId = PetId.create()

            every { petOutputPort.loadPetById(petId) } answers { UnitReactive.error(IllegalStateException("Cannot load Pet")) }

            val result = petService.adoptPet(AdoptPetCommand(petId, randomUUID()))

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
                    Pet(id = petId, type = CAT, name = "Garfield", birthDate = LocalDate.now())
                )
            }

            every { petOwnerOutputPort.loadPetOwner(any()) } answers { UnitReactive.error(IllegalStateException("Cannot load PetOwner")) }

            val result = petService.adoptPet(AdoptPetCommand(petId, ownerIdentityId))

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
                    Pet(id = petId, type = CAT, name = "Garfield", birthDate = LocalDate.now())
                )
            }

            every { petOwnerOutputPort.loadPetOwner(any()) } answers { UnitReactive.empty() }

            val result = petService.adoptPet(AdoptPetCommand(petId, ownerIdentityId))

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
                    Pet(id = petId, type = CAT, name = "Garfield", birthDate = LocalDate.now())
                )
            }

            every { petOwnerOutputPort.loadPetOwner(LoadPetOwnerCommand(ownerIdentityId)) } answers {
                UnitReactive(
                    mockPetOwner
                )
            }

            every { petOutputPort.updatePet(any(Pet::class)) } answers { UnitReactive(args.first() as Pet) }

            val result = petService.adoptPet(AdoptPetCommand(petId, ownerIdentityId))

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

    @Nested
    inner class DeletePet {

        @Test
        fun `It should return an errored UnitReactive with a DeletePetCommandException when the PetOutputPort cannot load the Pet`() {
            val pet = Pet(PetId.create(), DOG, "Snoopy", LocalDate.now())

            every { petOutputPort.loadPetById(pet.id) } answers { UnitReactive.empty() }

            val result = petService.deletePet(DeletePetCommand(pet.id))

            assertThatCode { result.block()!! }
                .isInstanceOf(DeletePetCommandException::class.java)
                .hasMessageContaining("Failed deleting Pet with ID ${pet.id}")
                .hasMessageContaining("Pet was not found")

            verify { petOutputPort.loadPetById(pet.id) }
            verify (exactly = 0) { petOutputPort.deletePet(pet) }
        }

        @Test
        fun `It should return an errored UnitReactive with a DeletePetCommandException when the PetOutputPort can load a Pet but cannot delete it`() {
            val pet = Pet(PetId.create(), DOG, "Marshall", LocalDate.now())

            every { petOutputPort.loadPetById(pet.id) } answers { UnitReactive(pet) }
            every { petOutputPort.deletePet(pet) } answers { UnitReactive(false) }

            val result = petService.deletePet(DeletePetCommand(pet.id))

            assertThatCode {result.block()!!  }
                .isInstanceOf(DeletePetCommandException::class.java)
                .hasMessageContaining("Failed deleting Pet with ID ${pet.id}")
                .hasMessageContaining("Pet cannot be deleted")

            verify { petOutputPort.loadPetById(pet.id) }
            verify { petOutputPort.deletePet(pet) }
        }

        @Test
        fun `It should return a UnitReactive that does not error when the PetOutputPort can load a Pet and can delete it`() {
            val pet = Pet(PetId.create(), DOG, "Rubble", LocalDate.now())

            every { petOutputPort.loadPetById(pet.id) } answers { UnitReactive(pet) }
            every { petOutputPort.deletePet(pet) } answers { UnitReactive(true) }

            val result = petService.deletePet(DeletePetCommand(pet.id))

            assertThatCode {result.block()!!  }
                .doesNotThrowAnyException()

            verify { petOutputPort.loadPetById(pet.id) }
            verify { petOutputPort.deletePet(pet) }
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