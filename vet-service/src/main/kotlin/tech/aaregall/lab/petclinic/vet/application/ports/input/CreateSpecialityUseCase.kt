package tech.aaregall.lab.petclinic.vet.application.ports.input

import tech.aaregall.lab.petclinic.vet.domain.model.Speciality

fun interface CreateSpecialityUseCase {

    fun createSpeciality(createSpecialityCommand: CreateSpecialityCommand): Speciality

}

data class CreateSpecialityCommand(val name: String, val description: String? = null)

class CreateSpecialityCommandException(message: String, cause: Throwable? = null) : IllegalStateException("Failed to create Speciality: $message", cause)