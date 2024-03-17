package tech.aaregall.lab.petclinic.vet.application.usecase

import tech.aaregall.lab.petclinic.common.UseCase
import tech.aaregall.lab.petclinic.vet.application.ports.input.SetVetSpecialitiesCommand
import tech.aaregall.lab.petclinic.vet.application.ports.input.SetVetSpecialitiesCommandException
import tech.aaregall.lab.petclinic.vet.application.ports.input.SetVetSpecialitiesInputPort
import tech.aaregall.lab.petclinic.vet.application.ports.output.SpecialityOutputPort
import tech.aaregall.lab.petclinic.vet.application.ports.output.VetOutputPort
import tech.aaregall.lab.petclinic.vet.domain.model.Vet

@UseCase
internal class SetVetSpecialitiesUseCase(private val vetOutputPort: VetOutputPort, private val specialityOutputPort: SpecialityOutputPort): SetVetSpecialitiesInputPort {

    override fun setVetSpecialities(setVetSpecialitiesCommand: SetVetSpecialitiesCommand) : Vet {
        val vet = vetOutputPort.loadVet(setVetSpecialitiesCommand.vetId)
            ?: throw SetVetSpecialitiesCommandException("Cannot set Vet Specialities for a non existing Vet")

        if (setVetSpecialitiesCommand.specialitiesIds.isEmpty()) throw SetVetSpecialitiesCommandException("At least one Speciality is required")

        val specialities = setVetSpecialitiesCommand.specialitiesIds
            .map { specialityOutputPort.loadSpeciality(it) ?: throw SetVetSpecialitiesCommandException("Speciality '$it' does not exist") }

        return vetOutputPort.setVetSpecialities(vet, specialities)
    }
}