package tech.aaregall.lab.micronaut.petclinic.identity.infrastructure.adapters.output.persistence

import jakarta.inject.Singleton
import jakarta.persistence.EntityNotFoundException
import tech.aaregall.lab.micronaut.petclinic.identity.application.ports.output.IdentityOutputPort
import tech.aaregall.lab.micronaut.petclinic.identity.domain.model.Identity
import java.util.*

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

    override fun loadIdentityById(id: UUID): Identity? {
        return identityJpaRepository.findById(id)
            .map(identityPersistenceMapper::mapToDomain)
            .orElseThrow { EntityNotFoundException("Identity with id '$id' not found") }
    }

}