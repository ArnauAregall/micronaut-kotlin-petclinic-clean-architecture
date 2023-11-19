package tech.aaregall.lab.micronaut.petclinic.identity.infrastructure.adapters.output.persistence

import jakarta.inject.Singleton
import tech.aaregall.lab.micronaut.petclinic.identity.domain.model.Identity

@Singleton
internal class IdentityPersistenceMapper {

    fun mapToDomain(entity: IdentityJpaEntity): Identity {
        return Identity(entity.firstName, entity.lastName)
    }

    fun mapToEntity(identity: Identity): IdentityJpaEntity {
        return IdentityJpaEntity(identity.firstName, identity.lastName)
    }

}