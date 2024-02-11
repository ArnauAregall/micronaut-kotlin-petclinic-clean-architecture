package tech.aaregall.lab.petclinic.identity.infrastructure.adapters.output.persistence

import io.micronaut.security.utils.SecurityService
import jakarta.inject.Singleton
import tech.aaregall.lab.petclinic.identity.application.ports.output.RoleOutputPort
import tech.aaregall.lab.petclinic.identity.domain.model.Identity
import tech.aaregall.lab.petclinic.identity.domain.model.Role
import tech.aaregall.lab.petclinic.identity.domain.model.RoleId
import java.util.UUID

@Singleton
internal class RolePersistenceAdapter(
    private val roleJpaRepository: RoleJpaRepository,
    private val identityRoleJpaRepository: IdentityRoleJpaRepository,
    private val securityService: SecurityService
): RoleOutputPort {

    override fun createRole(role: Role): Role {
        var jpaEntity = mapToEntity(role)
        jpaEntity.createdBy = securityService.username().map(UUID::fromString).orElse(SYSTEM_ACCOUNT_AUDIT_ID)
        jpaEntity = roleJpaRepository.save(jpaEntity)
        return mapToDomain(jpaEntity)
    }

    override fun roleExistsByName(name: String): Boolean =
        roleJpaRepository.existsByNameIgnoreCase(name)

    override fun loadRoleById(roleId: RoleId): Role? =
        roleJpaRepository.findById(UUID.fromString(roleId.toString()))
            .map(this::mapToDomain)
            .orElse(null)

    override fun assignRoleToIdentity(identity: Identity, role: Role) {
        val identityRoleJpaEntity = IdentityRoleJpaEntity(
            IdentityRoleId(
                identityId = UUID.fromString(identity.id.toString()),
                roleId = UUID.fromString(role.id.toString())
            )
        )
        identityRoleJpaRepository.save(identityRoleJpaEntity)
    }

    private fun mapToEntity(domain: Role): RoleJpaEntity =
        RoleJpaEntity(UUID.fromString(domain.id.toString()), domain.name)

    private fun mapToDomain(jpaEntity: RoleJpaEntity): Role =
        Role(RoleId(jpaEntity.id), jpaEntity.name)

}