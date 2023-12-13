package tech.aaregall.lab.petclinic.pet.application.ports.input

import tech.aaregall.lab.petclinic.pet.domain.model.Pet
import tech.aaregall.lab.petclinic.pet.domain.model.PetType
import java.time.LocalDate
import java.util.UUID

fun interface CreatePetUseCase {

    fun createPet(createPetCommand: CreatePetCommand): Pet

}

data class CreatePetCommand(val type: PetType, val name: String, val birthDate: LocalDate, val ownerIdentityId: UUID?)