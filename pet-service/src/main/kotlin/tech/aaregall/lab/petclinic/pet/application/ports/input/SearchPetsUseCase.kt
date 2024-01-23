package tech.aaregall.lab.petclinic.pet.application.ports.input

import tech.aaregall.lab.petclinic.common.reactive.CollectionReactive
import tech.aaregall.lab.petclinic.pet.domain.model.Pet

fun interface SearchPetsUseCase {

    fun searchPets(searchPetsCommand: SearchPetsCommand): CollectionReactive<Pet>

}

data class SearchPetsCommand(val pageNumber: Int, val pageSize: Int)