package tech.aaregall.lab.petclinic.identity.infrastructure.adapters.output.persistence

import jakarta.inject.Singleton
import tech.aaregall.lab.petclinic.identity.application.ports.output.RoleOutputPort
import tech.aaregall.lab.petclinic.identity.domain.model.Role
import tech.aaregall.lab.petclinic.identity.domain.model.RoleId
import java.util.UUID

@Singleton
internal class RolePersistenceAdapter(private val roleJpaRepository: RoleJpaRepository): RoleOutputPort {

    override fun loadRoleById(roleId: RoleId): Role? =
        roleJpaRepository.findById(UUID.fromString(roleId.toString()))
            .map { role -> Role(RoleId(role.id), role.name) }
            .orElse(null)

}