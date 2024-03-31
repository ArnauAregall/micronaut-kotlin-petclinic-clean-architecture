package tech.aaregall.lab.petclinic.vet.application.ports.input

import tech.aaregall.lab.petclinic.vet.domain.model.SpecialityId
import tech.aaregall.lab.petclinic.vet.domain.model.Vet
import tech.aaregall.lab.petclinic.vet.domain.model.VetId

fun interface SetVetSpecialitiesInputPort {

    fun setVetSpecialities(setVetSpecialitiesCommand: SetVetSpecialitiesCommand): Vet

}

data class SetVetSpecialitiesCommand(val vetId: VetId, val specialitiesIds: Collection<SpecialityId>)

class SetVetSpecialitiesCommandException(message: String, cause: Throwable? = null) : IllegalArgumentException("Failed to set Vet Specialities: $message", cause)