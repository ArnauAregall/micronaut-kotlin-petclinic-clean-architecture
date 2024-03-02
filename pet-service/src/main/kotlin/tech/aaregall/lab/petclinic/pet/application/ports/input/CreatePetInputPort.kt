package tech.aaregall.lab.petclinic.pet.application.ports.input

import tech.aaregall.lab.petclinic.common.reactive.UnitReactive
import tech.aaregall.lab.petclinic.pet.domain.model.Pet
import tech.aaregall.lab.petclinic.pet.domain.model.PetType
import java.time.LocalDate
import java.util.UUID

fun interface CreatePetInputPort {

    fun createPet(createPetCommand: CreatePetCommand): UnitReactive<Pet>

}

data class CreatePetCommand(val type: PetType, val name: String, val birthDate: LocalDate, val ownerIdentityId: UUID?)

class CreatePetCommandException(message: String, cause: Throwable? = null) : IllegalArgumentException("Failed to create Pet: $message", cause)