package tech.aaregall.lab.petclinic.identity.infrastructure.adapters.output.persistence

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import tech.aaregall.lab.petclinic.identity.domain.model.Identity
import tech.aaregall.lab.petclinic.identity.domain.model.IdentityId
import java.util.UUID

internal class IdentityPersistenceMapperTest {

    private val identityPersistenceMapper = IdentityPersistenceMapper()

    @Test
    fun `Maps entity to domain`() {
        val jpaEntity = IdentityJpaEntity(id = UUID.randomUUID(), firstName = "John", lastName = "Doe")

        val domain = identityPersistenceMapper.mapToDomain(jpaEntity)

        assertThat(domain)
            .isNotNull
            .extracting("firstName", "lastName")
            .containsExactly("John", "Doe")
    }

    @Test
    fun `Maps domain to entity`() {
        val domain = Identity(id = IdentityId.create(), firstName = "Bob", lastName = "Builder")

        val jpaEntity = identityPersistenceMapper.mapToEntity(domain)

        assertThat(jpaEntity)
            .isNotNull
            .extracting("firstName", "lastName")
            .containsExactly("Bob", "Builder")
    }


}