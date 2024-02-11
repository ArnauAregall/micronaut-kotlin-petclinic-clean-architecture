package tech.aaregall.lab.petclinic.identity.infrastructure.adapters.output.persistence

import io.micronaut.data.jdbc.runtime.JdbcOperations
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import tech.aaregall.lab.petclinic.identity.application.ports.output.RoleOutputPort
import tech.aaregall.lab.petclinic.identity.domain.model.Role
import tech.aaregall.lab.petclinic.identity.domain.model.RoleId
import java.util.UUID.randomUUID

@MicronautTest(transactional = false)
internal class RolePersistenceAdapterIT(
    private val roleOutputPort: RoleOutputPort,
    private val jdbc: JdbcOperations) {

    @BeforeEach
    fun tearDown() {
        jdbc.execute { c -> c.prepareCall("truncate table role").execute() }
    }

    @Nested
    inner class LoadRoleById {

        @Test
        fun `It should return a Role when it exists in the database`() {
            val id = randomUUID()
            jdbc.execute { conn -> conn.prepareCall("insert into role(id, name) values ('${id}', 'Mage')").execute() }

            val result = roleOutputPort.loadRoleById(RoleId.of(id))

            assertThat(result!!)
                .isNotNull
                .isEqualTo(Role(RoleId(id), "Mage"))
        }

        @Test
        fun `It should return null when it does not exist in the database`() {
            val result = roleOutputPort.loadRoleById(RoleId.of(randomUUID()))

            assertThat(result).isNull()
        }

    }

}