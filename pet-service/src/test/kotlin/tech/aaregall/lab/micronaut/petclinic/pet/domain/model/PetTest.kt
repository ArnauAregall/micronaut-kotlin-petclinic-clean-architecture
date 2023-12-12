package tech.aaregall.lab.micronaut.petclinic.pet.domain.model

import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.time.LocalDate

internal class PetTest {

    @ParameterizedTest
    @EnumSource(PetType::class)
    fun `Should create Pet of any type with not blank name`(petType: PetType) {
        assertThatCode { Pet(id = PetId.create(), type = petType, name = "Foo", birthDate = LocalDate.now())}
            .doesNotThrowAnyException()
    }

    @ParameterizedTest
    @EnumSource(PetType::class)
    fun `Name cannot be blank for every type of Pet`(petType: PetType) {
        assertThatCode { Pet(id = PetId.create(), type = petType, name = "", birthDate = LocalDate.now()) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("name cannot be blank")
    }

    @ParameterizedTest
    @EnumSource(PetType::class)
    fun `Birth date cannot be a future date for every type of Pet`(petType: PetType) {
        assertThatCode { Pet(id = PetId.create(), type = petType, name = "Foo", birthDate = LocalDate.now().plusWeeks(1)) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("birthDate cannot be a future date")
    }

}