package tech.aaregall.lab.petclinic.identity.domain.service

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import tech.aaregall.lab.petclinic.identity.application.ports.input.LoadIdentityCommand
import tech.aaregall.lab.petclinic.identity.application.ports.input.LoadIdentityUseCase
import tech.aaregall.lab.petclinic.identity.application.ports.input.UpdateIdentityContactDetailsCommand
import tech.aaregall.lab.petclinic.identity.application.ports.output.ContactDetailsOutputPort
import tech.aaregall.lab.petclinic.identity.domain.model.ContactDetails
import tech.aaregall.lab.petclinic.identity.domain.model.Identity
import tech.aaregall.lab.petclinic.identity.domain.model.IdentityId

@ExtendWith(MockKExtension::class)
internal class ContactDetailsServiceTest {

    @MockK
    lateinit var loadIdentityUseCase: LoadIdentityUseCase

    @MockK
    lateinit var contactDetailsOutputPort: ContactDetailsOutputPort

    @InjectMockKs
    lateinit var contactDetailsService: ContactDetailsService

    @Nested
    @DisplayName("updateIdentityContactDetails")
    inner class UpdateIdentityContactDetails {

        @Test
        fun `Calls ContactDetailsOutputPort when Identity exists`() {
            val identityId = IdentityId.create()
            val identity = Identity(id = identityId, firstName = "Foo", lastName = "Bar")

            every { loadIdentityUseCase.loadIdentity(LoadIdentityCommand(identityId)) } answers { identity }
            every {
                contactDetailsOutputPort.updateIdentityContactDetails(
                    eq(identity),
                    any(ContactDetails::class)
                )
            } answers { it.invocation.args.last() as ContactDetails }

            val result = contactDetailsService.updateIdentityContactDetails(UpdateIdentityContactDetailsCommand(
                identityId = identityId, email = "foo.bar@test.com", phoneNumber = "123 456 789"
            ))

            assertThat(result)
                .isNotNull
                .extracting(ContactDetails::email, ContactDetails::phoneNumber)
                .containsExactly("foo.bar@test.com", "123 456 789")

            verify {
                contactDetailsOutputPort.updateIdentityContactDetails(identity,
                    ContactDetails(email = "foo.bar@test.com", phoneNumber = "123 456 789"))
            }
        }

        @Test
        fun `Throws IllegalArgumentException when Identity does not exists`() {
            val identityId = IdentityId.create()

            every { loadIdentityUseCase.loadIdentity(LoadIdentityCommand(identityId)) } answers { null }

            assertThatCode {
                contactDetailsService.updateIdentityContactDetails(
                    UpdateIdentityContactDetailsCommand(
                        identityId = identityId, email = "foo.bar@test.com", phoneNumber = "123 456 789"
                    )
                )
            }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage("Cannot update ContactDetails for a non existing Identity")
        }

    }

}