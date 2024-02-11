package tech.aaregall.lab.petclinic.identity.infrastructure.adapters.output.persistence

import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import java.util.UUID

@Repository
internal interface RoleJpaRepository: JpaRepository<RoleJpaEntity, UUID> {

    fun existsByNameIgnoreCase(name: String): Boolean
}