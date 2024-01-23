package tech.aaregall.lab.petclinic.pet.application.ports.input

import tech.aaregall.lab.petclinic.common.reactive.UnitReactive

fun interface CountAllPetsUseCase {

    fun countAllPets(): UnitReactive<Long>

}