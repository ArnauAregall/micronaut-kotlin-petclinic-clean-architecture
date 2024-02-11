package tech.aaregall.lab.petclinic.identity.infrastructure.adapters.output.persistence

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import tech.aaregall.lab.petclinic.identity.domain.model.ContactDetails
import tech.aaregall.lab.petclinic.identity.domain.model.Identity
import tech.aaregall.lab.petclinic.identity.domain.model.IdentityId
import java.util.UUID

internal class IdentityPersistenceMapperTest {

    private val identityPersistenceMapper = IdentityPersistenceMapper(ContactDetailsPersistenceMapper())

    @Test
    fun `Maps entity to domain without ContactDetails`() {
        val jpaEntity = IdentityJpaEntity(id = UUID.randomUUID(), firstName = "John", lastName = "Doe")

        val domain = identityPersistenceMapper.mapToDomain(jpaEntity)

        assertThat(domain)
            .isNotNull
            .extracting(Identity::firstName, Identity::lastName, Identity::contactDetails)
            .containsExactly("John", "Doe", null)
    }

    @Test
    fun `Maps entity to domain with ContactDetails`() {
        val jpaEntity = IdentityJpaEntity(id = UUID.randomUUID(), firstName = "John", lastName = "Doe")
        jpaEntity.contactDetails = ContactDetailsJpaEntity(
            identityId = jpaEntity.id,
            email = "john.doe@test.com",
            phoneNumber = "123 456 789"
        )

        val domain = identityPersistenceMapper.mapToDomain(jpaEntity)

        assertThat(domain)
            .isNotNull
            .extracting(Identity::firstName, Identity::lastName, Identity::contactDetails)
            .containsExactly("John", "Doe", ContactDetails("john.doe@test.com", "123 456 789"))
    }

    @Test
    fun `Maps entity to domain without Roles`() {
        val jpaEntity = IdentityJpaEntity(id = UUID.randomUUID(), firstName = "John", lastName = "Doe")

        val domain = identityPersistenceMapper.mapToDomain(jpaEntity)

        assertThat(domain)
            .isNotNull
            .satisfies({
                assertThat(it)
                    .extracting(Identity::firstName, Identity::lastName)
                    .containsExactly("John", "Doe")
            })
            .satisfies({
                assertThat(it.roles)
                    .isNotNull
                    .isEmpty()
            })
    }

    @Test
    fun `Maps entity to domain with Roles`() {
        val jpaEntity = IdentityJpaEntity(id = UUID.randomUUID(), firstName = "John", lastName = "Doe")
        jpaEntity.roles = mutableSetOf(
            RoleJpaEntity(UUID.randomUUID(), "Mock Role", UUID.randomUUID()),
            RoleJpaEntity(UUID.randomUUID(), "Yet Another Mock Role", UUID.randomUUID()),
        )

        val domain = identityPersistenceMapper.mapToDomain(jpaEntity)

        assertThat(domain)
            .isNotNull
            .satisfies({
                assertThat(it)
                    .extracting(Identity::firstName, Identity::lastName)
                    .containsExactly("John", "Doe")
            })
            .satisfies({
                assertThat(it.roles)
                    .hasSize(2)
                    .extracting("name")
                    .containsExactly("Mock Role", "Yet Another Mock Role")
            })

    }

    @Test
    fun `Maps domain to entity without mapping ContactDetails`() {
        val domain = Identity(
            id = IdentityId.create(), firstName = "Bob", lastName = "Builder",
            contactDetails = ContactDetails(email = "bob.builder@test.com", phoneNumber = "123 456 789")
        )

        val jpaEntity = identityPersistenceMapper.mapToEntity(domain)

        assertThat(jpaEntity)
            .isNotNull
            .extracting(IdentityJpaEntity::firstName, IdentityJpaEntity::lastName, IdentityJpaEntity::contactDetails)
            .containsExactly("Bob", "Builder", null)
    }


}