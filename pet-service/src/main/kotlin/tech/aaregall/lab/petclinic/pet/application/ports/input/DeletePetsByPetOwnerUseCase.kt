package tech.aaregall.lab.petclinic.pet.application.ports.input

import java.util.UUID

fun interface DeletePetsByPetOwnerUseCase {

    fun deletePetsByPetOwner(deletePetsByPetOwnerCommand: DeletePetsByPetOwnerCommand)

}

data class DeletePetsByPetOwnerCommand(val ownerIdentityId: UUID)