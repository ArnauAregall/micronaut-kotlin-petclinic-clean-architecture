package tech.aaregall.lab.petclinic.pet.application.ports.input

import tech.aaregall.lab.petclinic.common.reactive.UnitReactive
import tech.aaregall.lab.petclinic.pet.domain.model.Pet
import tech.aaregall.lab.petclinic.pet.domain.model.PetId

fun interface LoadPetUseCase {

    fun loadPet(loadPetCommand: LoadPetCommand): UnitReactive<Pet>

}

data class LoadPetCommand(val petId: PetId)