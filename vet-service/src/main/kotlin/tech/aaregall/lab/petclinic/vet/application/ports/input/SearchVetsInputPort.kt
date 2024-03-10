package tech.aaregall.lab.petclinic.vet.application.ports.input

import tech.aaregall.lab.petclinic.vet.domain.model.Vet

fun interface SearchVetsInputPort {

    fun searchVets(searchVetsCommand: SearchVetsCommand): Collection<Vet>

}

data class SearchVetsCommand(val pageNumber: Int, val pageSize: Int)