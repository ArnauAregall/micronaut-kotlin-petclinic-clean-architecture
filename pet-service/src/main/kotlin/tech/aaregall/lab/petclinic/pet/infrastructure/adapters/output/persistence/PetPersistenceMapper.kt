package tech.aaregall.lab.petclinic.pet.infrastructure.adapters.output.persistence

import jakarta.inject.Singleton
import tech.aaregall.lab.petclinic.pet.domain.model.Pet
import tech.aaregall.lab.petclinic.pet.domain.model.PetId
import tech.aaregall.lab.petclinic.pet.domain.model.PetOwner
import tech.aaregall.lab.petclinic.pet.domain.model.PetType
import java.util.UUID

@Singleton
internal class PetPersistenceMapper {

    fun mapToDomain(entity: PetPersistenceEntity): Pet =
        Pet(
            id = PetId.of(entity.id),
            type = PetType.valueOf(entity.type),
            name = entity.name,
            birthDate = entity.birthDate,
            owner = entity.ownerIdentityId?.let { PetOwner(it) }
        )

    fun mapToEntity(pet: Pet): PetPersistenceEntity =
        PetPersistenceEntity(
            id = UUID.fromString(pet.id.toString()),
            type = pet.type.toString(),
            name = pet.name,
            birthDate = pet.birthDate,
            ownerIdentityId = pet.owner?.identityId
        )

}