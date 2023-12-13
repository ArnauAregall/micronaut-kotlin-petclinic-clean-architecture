package tech.aaregall.lab.micronaut.petclinic.pet.application.ports.output

import tech.aaregall.lab.micronaut.petclinic.pet.domain.model.PetOwner
import java.util.UUID

fun interface PetOwnerOutputPort {

    fun loadPetOwner(loadPetOwnerCommand: LoadPetOwnerCommand): PetOwner

}

data class LoadPetOwnerCommand(val ownerIdentityId: UUID)