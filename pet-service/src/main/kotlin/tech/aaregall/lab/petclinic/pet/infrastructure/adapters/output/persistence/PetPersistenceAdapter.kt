package tech.aaregall.lab.petclinic.pet.infrastructure.adapters.output.persistence

import jakarta.inject.Singleton
import tech.aaregall.lab.petclinic.common.reactive.UnitReactive
import tech.aaregall.lab.petclinic.pet.application.ports.output.PetOutputPort
import tech.aaregall.lab.petclinic.pet.domain.model.Pet
import tech.aaregall.lab.petclinic.pet.domain.model.PetOwner

@Singleton
internal class PetPersistenceAdapter(
    private val petR2DBCRepository: PetR2dbcRepository,
    private val petPersistenceMapper: PetPersistenceMapper
) : PetOutputPort {

    override fun createPet(pet: Pet): UnitReactive<Pet> {
        return UnitReactive(
            petR2DBCRepository.save(petPersistenceMapper.mapToEntity(pet))
                .map(petPersistenceMapper::mapToDomain)
                .map{ it.withOwner(pet.owner)}
        )
    }

    override fun deletePetsByPetOwner(petOwner: PetOwner) {
        petR2DBCRepository.deleteByOwnerIdentityId(petOwner.identityId)
    }
}