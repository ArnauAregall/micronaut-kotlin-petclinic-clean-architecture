package tech.aaregall.lab.petclinic.identity.infrastructure.adapters.output.persistence

import jakarta.inject.Singleton
import tech.aaregall.lab.petclinic.identity.domain.model.Identity
import tech.aaregall.lab.petclinic.identity.domain.model.IdentityId
import java.util.UUID

@Singleton
internal class IdentityPersistenceMapper(private val contactDetailsPersistenceMapper: ContactDetailsPersistenceMapper) {

    fun mapToDomain(entity: IdentityJpaEntity): Identity =
        Identity(IdentityId.of(entity.id), entity.firstName, entity.lastName,
            entity.contactDetails?.let { contactDetailsPersistenceMapper.mapToDomain(it) })

    fun mapToEntity(identity: Identity): IdentityJpaEntity =
        IdentityJpaEntity(UUID.fromString(identity.id.toString()), identity.firstName, identity.lastName)

}