package tech.aaregall.lab.petclinic.identity.infrastructure.adapters.output.persistence

import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository

@Repository
internal interface IdentityRoleJpaRepository: JpaRepository<IdentityRoleJpaEntity, IdentityRoleId>