package tech.aaregall.lab.petclinic.vet.domain

import org.assertj.core.api.Assertions.assertThat
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

}