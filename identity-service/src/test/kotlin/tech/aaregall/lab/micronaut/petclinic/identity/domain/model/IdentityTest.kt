package tech.aaregall.lab.micronaut.petclinic.identity.domain.model

import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.api.Test

internal class IdentityTest {

    @Test
    fun `Should create Identity with not blank first and last name`() {
        assertThatCode { Identity(id = IdentityId.create(), firstName = "Foo", lastName = "Bar") }
            .doesNotThrowAnyException()
    }

    @Test
    fun `Should create Identity with not blank first and last name and ContactDetails`() {
        assertThatCode { Identity(
            id = IdentityId.create(),
            firstName = "Foo",
            lastName = "Bar",
            contactDetails = ContactDetails(email = "test@test.com", phoneNumber = "123 456 789")) }
            .doesNotThrowAnyException()
    }

    @Test
    fun `First name cannot be blank`() {
        assertThatCode { Identity(id = IdentityId.create(), firstName = "", lastName = "Bar") }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("firstName cannot be blank")
    }

    @Test
    fun `Last name cannot be blank`() {
        assertThatCode { Identity(id = IdentityId.create(), firstName = "Foo", lastName = "") }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("lastName cannot be blank")
    }

}