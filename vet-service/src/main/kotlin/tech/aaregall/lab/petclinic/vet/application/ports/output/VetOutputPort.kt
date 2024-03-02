package tech.aaregall.lab.petclinic.vet.application.ports.output

import tech.aaregall.lab.petclinic.vet.domain.model.Vet

fun interface VetOutputPort {

    fun createVet(vet: Vet): Vet

}