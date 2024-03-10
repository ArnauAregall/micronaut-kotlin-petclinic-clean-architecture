package tech.aaregall.lab.petclinic.vet.application.ports.output

import tech.aaregall.lab.petclinic.vet.domain.model.Speciality
import tech.aaregall.lab.petclinic.vet.domain.model.SpecialityId
import tech.aaregall.lab.petclinic.vet.domain.model.Vet

interface SpecialityOutputPort {

    fun specialityExistsByName(name: String): Boolean

    fun findSpecialities(pageNumber: Int, pageSize: Int): Collection<Speciality>?

    fun createSpeciality(speciality: Speciality): Speciality

    fun loadSpeciality(specialityId: SpecialityId): Speciality?

    fun setVetSpecialities(vet: Vet, specialities: Collection<Speciality>): Vet

}