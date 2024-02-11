package tech.aaregall.lab.petclinic.identity.domain.model

import org.assertj.core.api.Assertions.assertThat
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
    fun `Should create Identity with not blank first and last name, ContactDetails and Roles`() {
        assertThatCode {
            Identity(
                id = IdentityId.create(),
                firstName = "Foo",
                lastName = "Bar",
                contactDetails = ContactDetails(email = "test@test.com", phoneNumber = "123 456 789"),
                roles = listOf(Role(id = RoleId.create(), name = "Warrior"))
            )
        }
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

    @Test
    fun `hasRole should return false when Identity does not have any Role`() {
        val identity = Identity(id = IdentityId.create(), firstName = "Foo", lastName = "Bar")

        val role = Role(id = RoleId.create(), name = "Warrior")

        val result = identity.hasRole(role)

        assertThat(result).isFalse()
    }

    @Test
    fun `hasRole should return false when Identity has Roles and does not have aimed Role`() {
        val roleWarrior = Role(id = RoleId.create(), name = "Warrior")
        val roleMage = Role(id = RoleId.create(), name = "Mage")

        val identity = Identity(
            id = IdentityId.create(), firstName = "Foo", lastName = "Bar",
            roles = listOf(roleWarrior, roleMage)
        )

        val role = Role(id = RoleId.create(), name = "Paladin")

        val result = identity.hasRole(role)

        assertThat(result).isFalse()
    }

    @Test
    fun `hasRole should return true when Identity has Roles and has aimed Role`() {
        val roleWarrior = Role(id = RoleId.create(), name = "Warrior")
        val roleMage = Role(id = RoleId.create(), name = "Mage")

        val identity = Identity(
            id = IdentityId.create(), firstName = "Foo", lastName = "Bar",
            roles = listOf(roleWarrior, roleMage)
        )

        assertThat(identity.hasRole(roleWarrior))
            .isTrue()

        assertThat(identity.hasRole(roleMage))
            .isTrue()
    }

}