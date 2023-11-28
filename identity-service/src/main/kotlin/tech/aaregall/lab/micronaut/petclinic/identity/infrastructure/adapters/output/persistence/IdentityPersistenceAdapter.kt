package tech.aaregall.lab.micronaut.petclinic.identity.infrastructure.adapters.output.persistence

import jakarta.inject.Singleton
import tech.aaregall.lab.micronaut.petclinic.identity.application.ports.output.IdentityOutputPort
import tech.aaregall.lab.micronaut.petclinic.identity.domain.model.Identity
import tech.aaregall.lab.micronaut.petclinic.identity.domain.model.IdentityId
import java.util.UUID

@Singleton
internal class IdentityPersistenceAdapter(
    private val identityJpaRepository: IdentityJpaRepository,
    private val identityPersistenceMapper: IdentityPersistenceMapper
) : IdentityOutputPort {

    override fun createIdentity(identity: Identity): Identity {
        var jpaEntity = identityPersistenceMapper.mapToEntity(identity)
        jpaEntity = identityJpaRepository.save(jpaEntity)
        return identityPersistenceMapper.mapToDomain(jpaEntity)
    }

    override fun loadIdentityById(identityId: IdentityId): Identity? {
        return identityJpaRepository.findById(UUID.fromString(identityId.toString()))
            .map(identityPersistenceMapper::mapToDomain)
            .orElse(null)
    }

}