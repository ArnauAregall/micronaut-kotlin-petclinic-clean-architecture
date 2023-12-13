package tech.aaregall.lab.petclinic.identity.infrastructure.adapters.output.persistence

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import tech.aaregall.lab.petclinic.identity.domain.model.ContactDetails
import tech.aaregall.lab.petclinic.identity.domain.model.Identity
import tech.aaregall.lab.petclinic.identity.domain.model.IdentityId
import java.util.UUID

internal class ContactDetailsPersistenceMapperTest {

    private val contactDetailsPersistenceMapper = ContactDetailsPersistenceMapper()

    @Test
    fun `Maps entity to domain`() {
        val jpaEntity = ContactDetailsJpaEntity(identityId = UUID.randomUUID(), email = "test@test.com", phoneNumber = "123 456 789")

        val domain = contactDetailsPersistenceMapper.mapToDomain(jpaEntity)

        assertThat(domain)
            .isNotNull
            .extracting("email", "phoneNumber")
            .containsExactly("test@test.com", "123 456 789")
    }

    @Test
    fun `Maps domain to entity`() {
        val identity = Identity(id = IdentityId.create(), firstName = "Foo", lastName = "Bar")
        val domain = ContactDetails(email = "test@test.com", phoneNumber = "123 456 789")

        val jpaEntity = contactDetailsPersistenceMapper.mapToEntity(identity, domain)

        assertThat(jpaEntity)
            .isNotNull
            .extracting("identityId", "email", "phoneNumber")
            .containsExactly(UUID.fromString(identity.id.toString()), "test@test.com", "123 456 789")
    }

}