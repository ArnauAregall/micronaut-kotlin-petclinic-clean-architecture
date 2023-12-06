package tech.aaregall.lab.micronaut.petclinic.identity.domain.model

import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.api.Test

internal class ContactDetailsTest {

    @Test
    fun `Should create ContactDetails with not blank email and phone number`() {
        assertThatCode { ContactDetails(email = "test@test.com", phoneNumber = "123 456 789") }
            .doesNotThrowAnyException()
    }

    @Test
    fun `Email cannot be blank`() {
        assertThatCode { ContactDetails(email = "", phoneNumber = "123 456 789") }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("email cannot be blank")
    }

    @Test
    fun `Phone number cannot be blank`() {
        assertThatCode { ContactDetails(email = "test@test.com", phoneNumber = "") }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("phoneNumber cannot be blank")
    }
}