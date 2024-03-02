package tech.aaregall.lab.petclinic.vet.application.ports.output

import tech.aaregall.lab.petclinic.vet.domain.model.Speciality
import tech.aaregall.lab.petclinic.vet.domain.model.SpecialityId

interface SpecialityOutputPort {

    fun specialityExistsByName(name: String): Boolean

    fun createSpeciality(speciality: Speciality): Speciality

    fun loadSpeciality(specialityId: SpecialityId): Speciality?

}