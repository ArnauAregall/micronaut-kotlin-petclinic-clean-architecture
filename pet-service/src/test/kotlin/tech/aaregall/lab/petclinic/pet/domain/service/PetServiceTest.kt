package tech.aaregall.lab.petclinic.pet.domain.service

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
import tech.aaregall.lab.petclinic.pet.application.ports.input.CreatePetCommand
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

@ExtendWith(MockKExtension::class)
internal class PetServiceTest {

    @MockK
    lateinit var petOutputPort: PetOutputPort

    @MockK
    lateinit var petOwnerOutputPort: PetOwnerOutputPort

    @InjectMockKs
    lateinit var petService: PetService

    @Nested
    inner class CreatePet {

        private fun mockCreatePetOutputPort() =
            every { petOutputPort.createPet(any(Pet::class)) } answers {
                val argPet = it.invocation.args.first() as Pet
                UnitReactive(Pet(
                    id = PetId.create(),
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

            verify (exactly = 0) { petOwnerOutputPort.loadPetOwner(any()) }

            assertThat(result.toMono().block())
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

            verify { petOwnerOutputPort.loadPetOwner(LoadPetOwnerCommand(ownerIdentityId)) }

            assertThat(result.toMono().block())
                .isNotNull
                .extracting(Pet::type, Pet::name, Pet::birthDate, Pet::owner)
                .containsExactly(DOG, "Bimo", LocalDate.now(), PetOwner(ownerIdentityId))
        }

    }

}