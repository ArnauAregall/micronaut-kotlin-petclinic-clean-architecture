package tech.aaregall.lab.petclinic.pet.infrastructure.adapters.output.persistence

import io.micronaut.data.model.Pageable
import jakarta.inject.Singleton
import tech.aaregall.lab.petclinic.common.reactive.CollectionReactive
import tech.aaregall.lab.petclinic.common.reactive.UnitReactive
import tech.aaregall.lab.petclinic.pet.application.ports.output.PetOutputPort
import tech.aaregall.lab.petclinic.pet.domain.model.Pet
import tech.aaregall.lab.petclinic.pet.domain.model.PetOwner

@Singleton
internal class PetPersistenceAdapter(
    private val petR2DBCRepository: PetR2dbcRepository,
    private val petPersistenceMapper: PetPersistenceMapper
) : PetOutputPort {

    override fun findPets(pageNumber: Int, pageSize: Int): CollectionReactive<Pet> {
        val flux = petR2DBCRepository.findAll(Pageable.from(pageNumber, pageSize))
            .flatMapIterable { it.content }
            .map(petPersistenceMapper::mapToDomain)
        return CollectionReactive(flux)
    }

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