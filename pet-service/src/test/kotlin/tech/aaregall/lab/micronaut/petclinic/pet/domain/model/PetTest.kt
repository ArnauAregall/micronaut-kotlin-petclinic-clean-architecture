package tech.aaregall.lab.micronaut.petclinic.pet.domain.model

import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.api.Test

internal class PetTest {

    @Test
    fun `Should create Pet with not blank name`() {
        assertThatCode { Pet(id = PetId.create(), name = "Java")}
            .doesNotThrowAnyException()
    }

    @Test
    fun `Name cannot be blank`() {
        assertThatCode { Pet(id = PetId.create(), name = "") }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("name cannot be blank")
    }
}