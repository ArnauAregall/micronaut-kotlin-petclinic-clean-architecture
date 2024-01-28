package tech.aaregall.lab.petclinic.pet.infrastructure.adapters.output.persistence

import io.micronaut.data.annotation.Query
import io.micronaut.data.model.query.builder.sql.Dialect
import io.micronaut.data.r2dbc.annotation.R2dbcRepository
import io.micronaut.data.repository.reactive.ReactorPageableRepository
import reactor.core.publisher.Flux
import java.util.UUID

@R2dbcRepository(dialect = Dialect.POSTGRES)
internal interface PetR2dbcRepository: ReactorPageableRepository<PetPersistenceEntity, UUID> {

    @Query("SELECT * FROM pet LIMIT :size OFFSET :offset")
    fun find(offset: Int, size: Int): Flux<PetPersistenceEntity>

    fun deleteByOwnerIdentityId(ownerIdentityId: UUID)

}