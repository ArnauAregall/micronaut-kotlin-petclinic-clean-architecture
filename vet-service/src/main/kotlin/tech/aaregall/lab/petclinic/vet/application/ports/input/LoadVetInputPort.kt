package tech.aaregall.lab.petclinic.vet.application.ports.input

import tech.aaregall.lab.petclinic.vet.domain.model.Vet
import tech.aaregall.lab.petclinic.vet.domain.model.VetId

fun interface LoadVetInputPort {

    fun loadVet(vetId: VetId): Vet?

}