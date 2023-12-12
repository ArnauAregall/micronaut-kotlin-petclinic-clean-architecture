package tech.aaregall.lab.micronaut.petclinic.pet.domain.model

import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

internal class PetTest {

    @ParameterizedTest
    @EnumSource(PetType::class)
    fun `Should create Pet of any type with not blank name`(petType: PetType) {
        assertThatCode { Pet(id = PetId.create(), type = petType, name = "Java")}
            .doesNotThrowAnyException()
    }

    @ParameterizedTest
    @EnumSource(PetType::class)
    fun `Name cannot be blank for every type of Pet`(petType: PetType) {
        assertThatCode { Pet(id = PetId.create(), type = petType, name = "") }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("name cannot be blank")
    }
}