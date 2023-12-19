package tech.aaregall.lab.petclinic.pet.application.ports.output

import tech.aaregall.lab.petclinic.common.reactive.UnitReactive
import tech.aaregall.lab.petclinic.pet.domain.model.PetOwner
import java.util.UUID

fun interface PetOwnerOutputPort {

    fun loadPetOwner(loadPetOwnerCommand: LoadPetOwnerCommand): UnitReactive<PetOwner?>

}

data class LoadPetOwnerCommand(val ownerIdentityId: UUID)

class LoadPetOwnerCommandException(message: String, cause: Throwable? = null) : IllegalStateException("Failed loading PetOwner: $message", cause)