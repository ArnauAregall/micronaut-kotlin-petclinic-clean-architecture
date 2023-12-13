package tech.aaregall.lab.micronaut.petclinic.pet.domain.service

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import tech.aaregall.lab.micronaut.petclinic.pet.application.ports.input.CreatePetCommand
import tech.aaregall.lab.micronaut.petclinic.pet.application.ports.output.PetOutputPort
import tech.aaregall.lab.micronaut.petclinic.pet.domain.model.Pet
import tech.aaregall.lab.micronaut.petclinic.pet.domain.model.PetId
import tech.aaregall.lab.micronaut.petclinic.pet.domain.model.PetOwner
import tech.aaregall.lab.micronaut.petclinic.pet.domain.model.PetType.CAT
import tech.aaregall.lab.micronaut.petclinic.pet.domain.model.PetType.DOG
import java.time.LocalDate
import java.util.UUID.randomUUID

@ExtendWith(MockKExtension::class)
internal class PetServiceTest {

    @MockK
    lateinit var petOutputPort: PetOutputPort

    @InjectMockKs
    lateinit var petService: PetService

    @Nested
    inner class CreatePet {

        @Test
        fun `Creates a Pet without Owner`() {
            every { petOutputPort.createPet(any(Pet::class)) } answers {
                val argPet = it.invocation.args.first() as Pet
                Pet(id = PetId.create(), type = argPet.type, name = argPet.name, birthDate = argPet.birthDate, owner = argPet.owner)
            }

            val result = petService.createPet(
                CreatePetCommand(
                    type = CAT, name = "Peebles", birthDate = LocalDate.now(), ownerIdentityId = null
                )
            )

            assertThat(result)
                .isNotNull
                .extracting(Pet::type, Pet::name, Pet::birthDate, Pet::owner)
                .containsExactly(CAT, "Peebles", LocalDate.now(), null)
        }

        @Test
        fun `Creates a Pet with Owner`() {
            every { petOutputPort.createPet(any(Pet::class)) } answers {
                val argPet = it.invocation.args.first() as Pet
                Pet(
                    id = PetId.create(),
                    type = argPet.type,
                    name = argPet.name,
                    birthDate = argPet.birthDate,
                    owner = argPet.owner
                )
            }

            val ownerIdentityId = randomUUID()
            val result = petService.createPet(
                CreatePetCommand(
                    type = DOG, name = "Bimo", birthDate = LocalDate.now(), ownerIdentityId = ownerIdentityId
                )
            )

            assertThat(result)
                .isNotNull
                .extracting(Pet::type, Pet::name, Pet::birthDate, Pet::owner)
                .containsExactly(DOG, "Bimo", LocalDate.now(), PetOwner(ownerIdentityId))
        }

    }

}