package tech.aaregall.lab.petclinic.pet.infrastructure.adapters.output.persistence

import jakarta.inject.Singleton
import tech.aaregall.lab.petclinic.common.reactive.UnitReactive
import tech.aaregall.lab.petclinic.pet.application.ports.output.PetOutputPort
import tech.aaregall.lab.petclinic.pet.domain.model.Pet
import tech.aaregall.lab.petclinic.pet.domain.model.PetId
import tech.aaregall.lab.petclinic.pet.domain.model.PetType
import java.util.*

@Singleton
internal class PetPersistenceAdapter(private val petR2DBCRepository: PetR2dbcRepository): PetOutputPort {

    override fun createPet(pet: Pet): UnitReactive<Pet> {
        val petEntity = PetPersistenceEntity(
            id = UUID.fromString(pet.id.toString()),
            type = pet.type.toString(),
            name = pet.name,
            birthDate = pet.birthDate,
            ownerIdentityId = pet.owner?.identityId)

        return UnitReactive(
            petR2DBCRepository.save(petEntity)
                .map { Pet(
                    id = PetId(it.id),
                    type = PetType.valueOf(it.type),
                    name = it.name,
                    birthDate = it.birthDate,
                    owner = pet.owner
                )}
        )
    }
}