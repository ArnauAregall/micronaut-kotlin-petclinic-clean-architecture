package tech.aaregall.lab.petclinic.pet.application.ports.input

import tech.aaregall.lab.petclinic.common.reactive.UnitReactive
import tech.aaregall.lab.petclinic.pet.domain.model.Pet
import tech.aaregall.lab.petclinic.pet.domain.model.PetId
import java.util.UUID

fun interface AdoptPetUseCase {

    fun adoptPet(adoptPetCommand: AdoptPetCommand): UnitReactive<Pet>

}

data class AdoptPetCommand(val petId: PetId, val ownerIdentityId: UUID)

class AdoptPetCommandException(message: String, cause: Throwable? = null) : IllegalStateException("Failed to adopt Pet: $message", cause)