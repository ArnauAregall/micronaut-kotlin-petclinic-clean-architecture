package tech.aaregall.lab.petclinic.vet.application.ports.output

import tech.aaregall.lab.petclinic.vet.domain.model.VetId

fun interface VetValidationOutputPort {

    fun isValidVetId(vetId: VetId): Boolean

}