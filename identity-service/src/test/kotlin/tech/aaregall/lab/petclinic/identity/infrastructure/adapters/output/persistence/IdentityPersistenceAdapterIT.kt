package tech.aaregall.lab.petclinic.identity.infrastructure.adapters.output.persistence

import io.micronaut.data.jdbc.runtime.JdbcOperations
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import tech.aaregall.lab.petclinic.identity.application.ports.output.IdentityOutputPort
import tech.aaregall.lab.petclinic.identity.domain.model.ContactDetails
import tech.aaregall.lab.petclinic.identity.domain.model.Identity
import tech.aaregall.lab.petclinic.identity.domain.model.IdentityId
import java.util.UUID.randomUUID

@MicronautTest(transactional = false)
internal class IdentityPersistenceAdapterIT(
    private val identityOutputPort: IdentityOutputPort,
    private val jdbc: JdbcOperations) {

    @BeforeEach
    fun tearDown() {
        jdbc.execute { c -> c.prepareCall("truncate table identity").execute() }
    }

    @Nested
    inner class CreateIdentity {

        @Test
        fun `It should create an Identity`() {
            identityOutputPort.createIdentity(Identity(id = IdentityId.create(), firstName = "John", lastName = "Doe"))

            jdbc.execute { conn ->
                val resultSet = conn.prepareStatement("select count(*) as identity_count from identity").executeQuery()
                resultSet.next()
                assertThat(resultSet.getInt("identity_count")).isEqualTo(1)
            }
        }

        @Test
        fun `The default createdBy should be the System Account Audit ID`() {
            identityOutputPort.createIdentity(Identity(id = IdentityId.create(), firstName = "Bob", lastName = "Builder"))

            jdbc.execute { conn ->
                val resultSet = conn.prepareStatement("select created_by from identity").executeQuery()
                resultSet.next()
                assertThat(resultSet.getString("created_by")).isEqualTo(SYSTEM_ACCOUNT_AUDIT_ID.toString())
            }
        }

    }

    @Nested
    inner class LoadIdentityById {

        @Test
        fun `It should load an Identity with null ContactDetails`() {
            val id = randomUUID()
            jdbc.execute { conn -> conn.prepareCall("insert into identity(id, first_name, last_name) values ('${id}', 'John', 'Doe')").execute() }

            val identity = identityOutputPort.loadIdentityById(IdentityId.of(id))

            assertThat(identity!!)
                .isNotNull
                .extracting(Identity::firstName, Identity::lastName, Identity::contactDetails)
                .containsExactly("John", "Doe", null)
        }

        @Test
        fun `It should load an Identity with filled ContactDetails`() {
            val id = randomUUID()
            jdbc.execute { conn -> conn.prepareCall("""
                insert into identity(id, first_name, last_name) values ('${id}', 'John', 'Doe');
                insert into contact_details(identity_id, email, phone_number) values ('${id}', 'john.doe@test.com', '123 456 789')
            """.trimIndent()).execute() }

            val identity = identityOutputPort.loadIdentityById(IdentityId.of(id))

            assertThat(identity)
                .isNotNull
                .extracting("firstName", "lastName")
                .containsExactly("John", "Doe")

            assertThat(identity!!.contactDetails!!)
                .isNotNull
                .extracting(ContactDetails::email, ContactDetails::phoneNumber)
                .containsExactly("john.doe@test.com", "123 456 789")
        }

        @Test
        fun `It should return null when identity is not found`() {
            val id = randomUUID()

            val identity = identityOutputPort.loadIdentityById(IdentityId.of(id))

            assertThat(identity).isNull()
        }

    }

    @Nested
    inner class DeleteIdentity {

        @Test
        fun `It should delete the Identity and it's ContactDetails when it exists`() {
            val identity = Identity(
                id = IdentityId.create(),
                firstName = "John",
                lastName = "Doe",
                contactDetails = ContactDetails("john.doe@test.com", "123 456 789")
            )

            jdbc.execute { conn ->
                conn.prepareCall("""
                    insert into identity(id, first_name, last_name) values ('${identity.id}', '${identity.firstName}', '${identity.lastName}');
                    insert into contact_details(identity_id, email, phone_number) values ('${identity.id}', '${identity.contactDetails!!.email}', '${identity.contactDetails!!.phoneNumber}');
                """.trimIndent()).execute()
            }

            identityOutputPort.deleteIdentity(identity)

            jdbc.execute { conn ->
                val resultSet = conn.prepareStatement("""
                    select  count(i.*) as identity_count, 
                            count(cd.*) as contact_details_count 
                    from identity i 
                    inner join contact_details cd on i.id = cd.identity_id 
                    where i.id = '${identity.id}'
                """.trimIndent()).executeQuery()
                resultSet.next()
                assertThat(resultSet.getInt("identity_count")).isZero()
                assertThat(resultSet.getInt("contact_details_count")).isZero()
            }
        }

        @Test
        fun `It should throw IllegalStateException when Identity does not exist`() {
            val identity = Identity(id = IdentityId.create(), firstName = "Foo", lastName = "Bar")

            assertThatCode {
                identityOutputPort.deleteIdentity(identity)
            }
                .isInstanceOf(IllegalStateException::class.java)
                .hasMessage("Cannot delete Identity with ID ${identity.id} as it does not exist")
        }

    }


}