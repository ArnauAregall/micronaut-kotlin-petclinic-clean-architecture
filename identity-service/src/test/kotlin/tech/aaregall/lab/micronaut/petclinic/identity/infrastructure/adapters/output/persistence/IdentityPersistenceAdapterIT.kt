package tech.aaregall.lab.micronaut.petclinic.identity.infrastructure.adapters.output.persistence

import io.micronaut.data.jdbc.runtime.JdbcOperations
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import jakarta.persistence.EntityNotFoundException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import tech.aaregall.lab.micronaut.petclinic.identity.application.ports.output.IdentityOutputPort
import tech.aaregall.lab.micronaut.petclinic.identity.domain.model.Identity
import java.util.UUID.randomUUID

@MicronautTest(transactional = false)
class IdentityPersistenceAdapterIT {

    @Inject
    private lateinit var identityOutputPort: IdentityOutputPort

    @Inject
    private lateinit var jdbc: JdbcOperations

    @AfterEach
    fun tearDown() {
        jdbc.execute { c -> c.prepareCall("truncate table identity").execute() }
    }

    @Nested
    inner class CreateIdentity {

        @Test
        fun `It should create an Identity`() {
            identityOutputPort.createIdentity(Identity(firstName = "John", lastName = "Doe"))

            jdbc.execute { conn ->
                val resultSet = conn.prepareStatement("select count(*) as identity_count from identity").executeQuery()
                resultSet.next()
                assertThat(resultSet.getInt("identity_count")).isEqualTo(1)
            }
        }


    }

    @Nested
    inner class LoadIdentityById {

        @Test
        fun `It should load an Identity`() {
            val id = randomUUID()
            jdbc.execute { conn -> conn.prepareCall("insert into identity(id, first_name, last_name) values ('${id}', 'John', 'Doe')").execute() }

            val identity = identityOutputPort.loadIdentityById(id)

            assertThat(identity)
                .isNotNull
                .extracting("firstName", "lastName")
                .containsExactly("John", "Doe")
        }

        @Test
        fun `It should throw a exception when entity is not found`() {
            val id = randomUUID()

            assertThatCode { identityOutputPort.loadIdentityById(id) }
                .isInstanceOf(EntityNotFoundException::class.java)
                .extracting(Throwable::message)
                .isEqualTo("Identity with id '$id' not found")
        }

    }


}