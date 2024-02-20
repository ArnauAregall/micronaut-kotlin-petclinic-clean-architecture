package tech.aaregall.lab.petclinic.vet.domain

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.InstanceOfAssertFactories.COLLECTION
import org.junit.jupiter.api.Test
import java.util.UUID

internal class VetTest {

    @Test
    fun `Should create a Vet with a random VetId`() {
        val vet = Vet(VetId.create())

        assertThat(vet)
            .isNotNull
            .extracting(Vet::id)
            .isNotNull
    }

    @Test
    fun `Should create a Vet with a given ID`() {
        val uuid = UUID.fromString("00000000-0000-0000-0000-000000000000")
        val vet = Vet(VetId.of(uuid))

        assertThat(vet)
            .isNotNull
            .extracting(Vet::id)
            .asString()
            .isEqualTo("00000000-0000-0000-0000-000000000000")
    }

    @Test
    fun `Should create a Vet with empty specialities by default`() {
        val vet = Vet(VetId.create())

        assertThat(vet)
            .isNotNull
            .extracting(Vet::specialities)
            .asInstanceOf(COLLECTION)
            .isEmpty()
    }

    @Test
    fun `Should create a Vet with the given specialities`() {
        val vet = Vet(
            VetId.create(), specialities = setOf(
                Speciality(id = SpecialityId.create(), name = "Surgery"),
                Speciality(id = SpecialityId.create(), name = "Anesthesia"),
            )
        )

        assertThat(vet)
            .isNotNull
            .extracting(Vet::specialities)
            .asInstanceOf(COLLECTION)
            .isNotEmpty()
            .hasSize(2)
            .extracting("name")
            .containsExactly("Surgery", "Anesthesia")
    }

}