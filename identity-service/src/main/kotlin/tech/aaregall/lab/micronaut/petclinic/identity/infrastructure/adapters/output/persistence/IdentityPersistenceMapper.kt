package tech.aaregall.lab.micronaut.petclinic.identity.infrastructure.adapters.output.persistence

import jakarta.inject.Singleton
import tech.aaregall.lab.micronaut.petclinic.identity.domain.model.Identity
import tech.aaregall.lab.micronaut.petclinic.identity.domain.model.IdentityId
import java.util.UUID

@Singleton
internal class IdentityPersistenceMapper {

    fun mapToDomain(entity: IdentityJpaEntity): Identity =
        Identity(IdentityId.of(entity.id), entity.firstName, entity.lastName)


    fun mapToEntity(identity: Identity): IdentityJpaEntity =
        IdentityJpaEntity(UUID.fromString(identity.id.toString()), identity.firstName, identity.lastName)

}