package tech.aaregall.lab.petclinic.identity.domain.model

import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.api.Test

internal class RoleTest {

    @Test
    fun `Should create Role with not blank name`() {
        assertThatCode { Role(RoleId.create(), "Admin")}
            .doesNotThrowAnyException()
    }

    @Test
    fun `Role name cannot be blank`() {
        assertThatCode { Role(RoleId.create(), "")}
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("name cannot be blank")
    }
}