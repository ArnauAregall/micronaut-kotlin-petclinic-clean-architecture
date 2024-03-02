package tech.aaregall.lab.petclinic.pet.application.ports.input

import tech.aaregall.lab.petclinic.common.reactive.UnitReactive
import tech.aaregall.lab.petclinic.pet.domain.model.PetId

fun interface DeletePetInputPort {

    fun deletePet(deletePetCommand: DeletePetCommand): UnitReactive<Unit>

}

data class DeletePetCommand(val petId: PetId)

class DeletePetCommandException(deletePetCommand: DeletePetCommand, message: String, cause: Throwable? = null)
    : IllegalArgumentException("Failed deleting Pet with ID ${deletePetCommand.petId}: $message", cause)
