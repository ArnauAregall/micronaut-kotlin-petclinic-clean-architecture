package tech.aaregall.lab.micronaut.petclinic.identity.infrastructure.adapters.output.persistence

import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import java.util.UUID

@Repository
internal interface ContactDetailsJpaRepository: JpaRepository<ContactDetailsJpaEntity, UUID>