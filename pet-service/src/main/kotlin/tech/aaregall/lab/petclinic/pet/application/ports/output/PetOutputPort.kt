package tech.aaregall.lab.petclinic.pet.application.ports.output

import tech.aaregall.lab.petclinic.common.reactive.UnitReactive
import tech.aaregall.lab.petclinic.pet.domain.model.Pet

fun interface PetOutputPort {

    fun createPet(pet: Pet): UnitReactive<Pet>

}