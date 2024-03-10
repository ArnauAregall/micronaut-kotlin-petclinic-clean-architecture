package tech.aaregall.lab.petclinic.vet.application.ports.input

import tech.aaregall.lab.petclinic.vet.domain.model.Speciality

fun interface SearchSpecialitiesInputPort {

    fun searchSpecialities(searchSpecialitiesCommand: SearchSpecialitiesCommand): Collection<Speciality>?

}

data class SearchSpecialitiesCommand(val pageNumber: Int, val pageSize: Int)