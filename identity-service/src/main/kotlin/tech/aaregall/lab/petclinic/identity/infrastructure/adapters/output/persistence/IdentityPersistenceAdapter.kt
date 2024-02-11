package tech.aaregall.lab.petclinic.identity.infrastructure.adapters.output.persistence

import io.micronaut.security.utils.SecurityService
import jakarta.inject.Singleton
import jakarta.transaction.Transactional
import tech.aaregall.lab.petclinic.identity.application.ports.output.IdentityOutputPort
import tech.aaregall.lab.petclinic.identity.domain.model.Identity
import tech.aaregall.lab.petclinic.identity.domain.model.IdentityId
import java.util.UUID

@Singleton
internal open class IdentityPersistenceAdapter(
    private val identityJpaRepository: IdentityJpaRepository,
    private val identityPersistenceMapper: IdentityPersistenceMapper,
    private val securityService: SecurityService
) : IdentityOutputPort {

    override fun createIdentity(identity: Identity): Identity {
        var jpaEntity = identityPersistenceMapper.mapToEntity(identity)
        jpaEntity.createdBy = securityService.username().map(UUID::fromString).orElse(SYSTEM_ACCOUNT_AUDIT_ID)
        jpaEntity = identityJpaRepository.save(jpaEntity)
        return identityPersistenceMapper.mapToDomain(jpaEntity)
    }

    @Transactional
    override fun loadIdentityById(identityId: IdentityId): Identity? {
        return identityJpaRepository.findById(UUID.fromString(identityId.toString()))
            .map(identityPersistenceMapper::mapToDomain)
            .orElse(null)
    }

    override fun deleteIdentity(identity: Identity) {
        identityJpaRepository.findById(UUID.fromString(identity.id.toString()))
            .map(identityJpaRepository::delete)
            .orElseThrow { IllegalStateException("Cannot delete Identity with ID ${identity.id} as it does not exist") }
    }

}