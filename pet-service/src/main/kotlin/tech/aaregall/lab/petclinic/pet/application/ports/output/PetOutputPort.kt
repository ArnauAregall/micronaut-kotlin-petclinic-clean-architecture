package tech.aaregall.lab.petclinic.pet.application.ports.output

import tech.aaregall.lab.petclinic.common.reactive.CollectionReactive
import tech.aaregall.lab.petclinic.common.reactive.UnitReactive
import tech.aaregall.lab.petclinic.pet.domain.model.Pet
import tech.aaregall.lab.petclinic.pet.domain.model.PetId
import tech.aaregall.lab.petclinic.pet.domain.model.PetOwner

interface PetOutputPort {

    fun findPets(pageNumber: Int, pageSize: Int): CollectionReactive<Pet>

    fun loadPetById(petId: PetId): UnitReactive<Pet>

    fun countAllPets(): UnitReactive<Long>

    fun createPet(pet: Pet): UnitReactive<Pet>

    fun deletePetsByPetOwner(petOwner: PetOwner)

}