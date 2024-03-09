package tech.aaregall.lab.petclinic.vet.application.ports.input

import tech.aaregall.lab.petclinic.vet.domain.model.VetId

fun interface DeleteVetInputPort {

    fun deleteVet(deleteVetCommand: DeleteVetCommand)

}

data class DeleteVetCommand(val vetId: VetId)

class DeleteVetCommandException(message: String, cause: Throwable? = null) : IllegalStateException("Failed to delete Vet: $message", cause)