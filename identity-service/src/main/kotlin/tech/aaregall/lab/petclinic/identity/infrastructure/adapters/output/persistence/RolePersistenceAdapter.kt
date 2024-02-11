package tech.aaregall.lab.petclinic.identity.infrastructure.adapters.output.persistence

import jakarta.inject.Singleton
import tech.aaregall.lab.petclinic.identity.application.ports.output.RoleOutputPort
import tech.aaregall.lab.petclinic.identity.domain.model.Identity
import tech.aaregall.lab.petclinic.identity.domain.model.Role
import tech.aaregall.lab.petclinic.identity.domain.model.RoleId
import java.util.UUID

@Singleton
internal class RolePersistenceAdapter(private val roleJpaRepository: RoleJpaRepository, private val identityRoleJpaRepository: IdentityRoleJpaRepository): RoleOutputPort {

    override fun roleExistsByName(name: String): Boolean =
        roleJpaRepository.existsByNameIgnoreCase(name)

    override fun loadRoleById(roleId: RoleId): Role? =
        roleJpaRepository.findById(UUID.fromString(roleId.toString()))
            .map { role -> Role(RoleId(role.id), role.name) }
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
}