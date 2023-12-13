package tech.aaregall.lab.petclinic.identity.infrastructure.adapters.output.persistence

import io.micronaut.data.jdbc.runtime.JdbcOperations
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import tech.aaregall.lab.petclinic.identity.application.ports.output.ContactDetailsOutputPort
import tech.aaregall.lab.petclinic.identity.application.ports.output.IdentityOutputPort
import tech.aaregall.lab.petclinic.identity.domain.model.ContactDetails
import tech.aaregall.lab.petclinic.identity.domain.model.Identity
import tech.aaregall.lab.petclinic.identity.domain.model.IdentityId

@MicronautTest(transactional = false)
internal class ContactDetailsPersistenceAdapterIT(
    private val identityOutputPort: IdentityOutputPort,
    private val contactDetailsOutputPort: ContactDetailsOutputPort,
    private val jdbc: JdbcOperations) {

    @AfterEach
    fun tearDown() {
        jdbc.execute { c -> c.prepareCall("truncate table contact_details").execute() }
    }

    @Nested
    inner class UpdateIdentityContactDetails {

        @Test
        fun `When Identity does not have Contact Details it should insert a new record`() {
            val identity = identityOutputPort.createIdentity(Identity(id = IdentityId.create(), firstName = "John", lastName = "Doe"))
            val contactDetails = ContactDetails(email = "john.doe@test.com", phoneNumber = "123 456 789")

            val result = contactDetailsOutputPort.updateIdentityContactDetails(identity, contactDetails)

            assertThat(result)
                .isNotNull
                .isEqualTo(contactDetails)

            jdbc.execute { conn ->
                val resultSet = conn.prepareStatement(
                    """
                        select email, phone_number, created_by
                        from contact_details
                        where identity_id = '${identity.id}'
                    """.trimIndent()
                ).executeQuery()

                resultSet.next()

                assertThat(resultSet.getString("email"))
                    .isEqualTo("john.doe@test.com")

                assertThat(resultSet.getString("phone_number"))
                    .isEqualTo("123 456 789")

                assertThat(resultSet.getString("created_by"))
                    .isEqualTo(SYSTEM_ACCOUNT_AUDIT_ID.toString())
            }
        }

        @Test
        fun `When Identity has Contact Details it should update the existing record`() {
            val identity = identityOutputPort.createIdentity(Identity(id = IdentityId.create(), firstName = "Bob", lastName = "Builder"))

            jdbc.execute { conn -> conn.prepareCall("""
                insert into contact_details(identity_id, email, phone_number)
                values ('${identity.id}', 'bob.builder@test.com', '+34 111 222 333')
            """.trimIndent()).execute() }

            val result = contactDetailsOutputPort.updateIdentityContactDetails(identity,
                ContactDetails(email = "bob.builder@company.com", phoneNumber = "+34 444 555 666")
            )

            assertThat(result)
                .isNotNull
                .extracting(ContactDetails::email, ContactDetails::phoneNumber)
                .containsExactly("bob.builder@company.com", "+34 444 555 666")

            jdbc.execute { conn ->
                val resultSet = conn.prepareStatement(
                    """
                        select count(*) as test_count
                        from contact_details
                        where 
                            identity_id = '${identity.id}' 
                            and email = 'bob.builder@company.com' 
                            and phone_number = '+34 444 555 666'
                    """.trimIndent()
                ).executeQuery()

                resultSet.next()

                assertThat(resultSet.getInt("test_count")).isEqualTo(1)
            }

        }

    }

}