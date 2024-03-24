package tech.aaregall.lab.petclinic.vet.domain.model

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.api.Test
import java.util.UUID

internal class SpecialityTest {

    @Test
    fun `Should create a Speciality with a random SpecialityId without description`() {
        val speciality = Speciality(id = SpecialityId.create(), name = "Surgery")

        assertThat(speciality)
            .isNotNull
            .satisfies(
                { assertThat(it.id).isNotNull },
                {
                    assertThat(it)
                        .extracting(Speciality::name, Speciality::description)
                        .containsExactly("Surgery", null)
                }
            )
    }

    @Test
    fun `Should create a Speciality with a random SpecialityId with description`() {
        val speciality = Speciality(id = SpecialityId.create(), name = "Surgery", description = "Sample description")

        assertThat(speciality)
            .isNotNull
            .satisfies(
                { assertThat(it.id).isNotNull },
                {
                    assertThat(it)
                        .extracting(Speciality::name, Speciality::description)
                        .containsExactly("Surgery", "Sample description")
                }
            )
    }

    @Test
    fun `Should create a Speciality with a given ID without description`() {
        val uuid = UUID.fromString("00000000-0000-0000-0000-000000000000")
        val speciality = Speciality(id = SpecialityId(uuid), name = "Surgery")

        assertThat(speciality)
            .isNotNull
            .satisfies(
                {
                    assertThat(it.id)
                        .asString()
                        .isEqualTo("00000000-0000-0000-0000-000000000000") },
                {
                    assertThat(it)
                        .extracting(Speciality::name, Speciality::description)
                        .containsExactly("Surgery", null)
                }
            )
    }

    @Test
    fun `Should create a Speciality with a given ID with description`() {
        val uuid = UUID.fromString("00000000-0000-0000-0000-000000000000")
        val speciality = Speciality(id = SpecialityId(uuid), name = "Surgery", description = "Sample description")

        assertThat(speciality)
            .isNotNull
            .satisfies(
                {
                    assertThat(it.id)
                        .asString()
                        .isEqualTo("00000000-0000-0000-0000-000000000000") },
                {
                    assertThat(it)
                        .extracting(Speciality::name, Speciality::description)
                        .containsExactly("Surgery", "Sample description")
                }
            )
    }

    @Test
    fun `Name cannot be blank`() {
        assertThatCode { Speciality(id = SpecialityId.create(), name = "")}
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("name cannot be blank")
    }

    @Test
    fun `Description cannot be blank when provided`() {
        assertThatCode { Speciality(id = SpecialityId.create(), name = "Surgery", description = "")}
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("description cannot be blank")
    }

}