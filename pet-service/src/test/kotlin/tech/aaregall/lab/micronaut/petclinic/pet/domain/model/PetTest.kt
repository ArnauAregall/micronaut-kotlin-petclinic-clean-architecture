package tech.aaregall.lab.micronaut.petclinic.pet.domain.model

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import tech.aaregall.lab.petclinic.pet.domain.model.Pet
import tech.aaregall.lab.petclinic.pet.domain.model.PetId
import tech.aaregall.lab.petclinic.pet.domain.model.PetOwner
import tech.aaregall.lab.petclinic.pet.domain.model.PetType
import java.time.LocalDate
import java.util.UUID.randomUUID

internal class PetTest {

    @ParameterizedTest
    @EnumSource(PetType::class)
    fun `Should create a Pet of any type with not blank name, a future birth date and without owner`(petType: PetType) {
        val pet = Pet(id = PetId.create(), type = petType, name = "Foo", birthDate = LocalDate.now())

        assertThat(pet.owner).isNull()
    }

    @ParameterizedTest
    @EnumSource(PetType::class)
    fun `Should create a Pet of any type with not blank name, a future birth date and with owner`(petType: PetType) {
        val pet = Pet(
            id = PetId.create(),
            type = petType,
            name = "Foo",
            birthDate = LocalDate.now(),
            owner = PetOwner(randomUUID())
        )

        assertThat(pet.owner).isNotNull
    }

    @ParameterizedTest
    @EnumSource(PetType::class)
    fun `Name cannot be blank for any type of Pet`(petType: PetType) {
        assertThatCode { Pet(id = PetId.create(), type = petType, name = "", birthDate = LocalDate.now()) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("name cannot be blank")
    }

    @ParameterizedTest
    @EnumSource(PetType::class)
    fun `Birth date cannot be a future date for any type of Pet`(petType: PetType) {
        assertThatCode { Pet(id = PetId.create(), type = petType, name = "Foo", birthDate = LocalDate.now().plusWeeks(1)) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("birthDate cannot be a future date")
    }

}