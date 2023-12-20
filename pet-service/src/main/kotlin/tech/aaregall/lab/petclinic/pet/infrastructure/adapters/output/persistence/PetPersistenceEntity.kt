package tech.aaregall.lab.petclinic.pet.infrastructure.adapters.output.persistence

import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity
import io.micronaut.serde.annotation.Serdeable
import java.time.LocalDate
import java.util.UUID

@Serdeable
@MappedEntity("pet")
internal data class PetPersistenceEntity(
    @Id
    val id: UUID,
    val type: String,
    val name: String,
    val birthDate: LocalDate,
    val ownerIdentityId: UUID?
)