package tech.aaregall.lab.petclinic.pet.infrastructure.adapters.output.persistence

import jakarta.inject.Singleton
import tech.aaregall.lab.petclinic.common.reactive.CollectionReactive
import tech.aaregall.lab.petclinic.common.reactive.UnitReactive
import tech.aaregall.lab.petclinic.pet.application.ports.output.PetOutputPort
import tech.aaregall.lab.petclinic.pet.domain.model.Pet
import tech.aaregall.lab.petclinic.pet.domain.model.PetId
import tech.aaregall.lab.petclinic.pet.domain.model.PetOwner
import java.util.UUID

@Singleton
internal class PetPersistenceAdapter(
    private val petR2DBCRepository: PetR2dbcRepository,
    private val petPersistenceMapper: PetPersistenceMapper
) : PetOutputPort {

    override fun findPets(pageNumber: Int, pageSize: Int): CollectionReactive<Pet> {
        val flux = petR2DBCRepository.find(pageNumber * pageSize, pageSize)
            .map(petPersistenceMapper::mapToDomain)
        return CollectionReactive(flux)
    }

    override fun countAllPets(): UnitReactive<Long> = UnitReactive(petR2DBCRepository.count())

    override fun createPet(pet: Pet): UnitReactive<Pet> {
        return UnitReactive(
            petR2DBCRepository.save(petPersistenceMapper.mapToEntity(pet))
                .map(petPersistenceMapper::mapToDomain)
                .map{ it.withOwner(pet.owner)}
        )
    }

    override fun updatePet(pet: Pet): UnitReactive<Pet> {
        return UnitReactive(
            petR2DBCRepository.update(petPersistenceMapper.mapToEntity(pet))
                .map(petPersistenceMapper::mapToDomain)
                .map { it.withOwner(pet.owner) }
        )
    }

    override fun loadPetById(petId: PetId): UnitReactive<Pet> =
        UnitReactive(petR2DBCRepository.findById(UUID.fromString(petId.toString())))
            .map(petPersistenceMapper::mapToDomain)

    override fun deletePet(pet: Pet): UnitReactive<Boolean> =
        UnitReactive(petR2DBCRepository.deleteById(UUID.fromString(pet.id.toString())))
            .map { count -> count == 1L }

    override fun deletePetsByPetOwner(petOwner: PetOwner) {
        petR2DBCRepository.deleteByOwnerIdentityId(petOwner.identityId)
    }
}