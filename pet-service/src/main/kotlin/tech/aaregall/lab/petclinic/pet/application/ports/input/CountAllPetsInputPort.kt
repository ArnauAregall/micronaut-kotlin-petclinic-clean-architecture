package tech.aaregall.lab.petclinic.pet.application.ports.input

import tech.aaregall.lab.petclinic.common.reactive.UnitReactive

fun interface CountAllPetsInputPort {

    fun countAllPets(): UnitReactive<Long>

}