package tech.aaregall.lab.micronaut.petclinic.identity.infrastructure.adapters.output.persistence

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import tech.aaregall.lab.micronaut.petclinic.identity.domain.model.Identity

internal class IdentityPersistenceMapperTest {

    private val identityPersistenceMapper = IdentityPersistenceMapper()

    @Test
    fun `Maps entity to domain`() {
        val jpaEntity = IdentityJpaEntity(firstName = "John", lastName = "Doe")

        val domain = identityPersistenceMapper.mapToDomain(jpaEntity)

        assertThat(domain)
            .isNotNull
            .extracting("firstName", "lastName")
            .containsExactly("John", "Doe")
    }

    @Test
    fun `Maps domain to entity`() {
        val domain = Identity(firstName = "Bob", lastName = "Builder")

        val jpaEntity = identityPersistenceMapper.mapToEntity(domain)

        assertThat(jpaEntity)
            .isNotNull
            .extracting("firstName", "lastName")
            .containsExactly("Bob", "Builder")
    }


}