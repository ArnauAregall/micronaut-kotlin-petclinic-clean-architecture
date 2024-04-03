package tech.aaregall.lab.petclinic.pet.application.ports.input

import java.util.UUID

fun interface DeletePetOwnerInputPort {

    fun deletePetOwner(deletePetOwnerCommand: DeletePetOwnerCommand)

}

data class DeletePetOwnerCommand(val ownerIdentityId: UUID)