package tech.aaregall.lab.micronaut.petclinic.pet.application.ports.output

import tech.aaregall.lab.micronaut.petclinic.pet.domain.model.Pet

fun interface PetOutputPort {

    fun createPet(pet: Pet): Pet

}