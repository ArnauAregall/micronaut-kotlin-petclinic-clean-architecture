package tech.aaregall.lab.petclinic.pet.infrastructure.adapters.output.persistence

import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.r2dbc.annotation.R2dbcRepository
import io.micronaut.data.repository.reactive.ReactorPageableRepository
import java.util.UUID

@R2dbcRepository(dialect = Dialect.POSTGRES)
internal interface PetR2dbcRepository: ReactorPageableRepository<PetPersistenceEntity, UUID> {

    fun deleteByOwnerIdentityId(ownerIdentityId: UUID)

}