package tech.aaregall.lab.petclinic.vet.application.ports.output

import tech.aaregall.lab.petclinic.vet.domain.model.Speciality
import tech.aaregall.lab.petclinic.vet.domain.model.Vet
import tech.aaregall.lab.petclinic.vet.domain.model.VetId

interface VetOutputPort {

    fun findVets(pageNumber: Int, pageSize: Int): Collection<Vet>?

    fun createVet(vet: Vet): Vet

    fun loadVet(vetId: VetId): Vet?

    fun deleteVet(vet: Vet): Boolean

    fun setVetSpecialities(vet: Vet, specialities: Collection<Speciality>): Vet

    fun countAll(): Int

}