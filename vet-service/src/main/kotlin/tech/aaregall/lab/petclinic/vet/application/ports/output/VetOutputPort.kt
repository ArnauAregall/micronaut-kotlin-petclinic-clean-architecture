package tech.aaregall.lab.petclinic.vet.application.ports.output

import tech.aaregall.lab.petclinic.vet.domain.model.Vet
import tech.aaregall.lab.petclinic.vet.domain.model.VetId

interface VetOutputPort {

    fun isValidVetId(vetId: VetId): Boolean

    fun createVet(vet: Vet): Vet

    fun loadVet(vetId: VetId): Vet?

}