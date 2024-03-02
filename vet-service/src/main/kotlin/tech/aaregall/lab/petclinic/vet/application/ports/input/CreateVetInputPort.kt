package tech.aaregall.lab.petclinic.vet.application.ports.input

import tech.aaregall.lab.petclinic.vet.domain.model.SpecialityId
import tech.aaregall.lab.petclinic.vet.domain.model.Vet

fun interface CreateVetInputPort {

    fun createVet(createVetCommand: CreateVetCommand): Vet

}

data class CreateVetCommand(val specialitiesIds: Collection<SpecialityId>)

class CreateVetCommandException(message: String, cause: Throwable? = null) : IllegalArgumentException("Failed to create Vet: $message", cause)