package tech.aaregall.lab.petclinic.vet.application.usecase

import tech.aaregall.lab.petclinic.common.UseCase
import tech.aaregall.lab.petclinic.vet.application.ports.input.CreateVetCommand
import tech.aaregall.lab.petclinic.vet.application.ports.input.CreateVetCommandException
import tech.aaregall.lab.petclinic.vet.application.ports.input.CreateVetInputPort
import tech.aaregall.lab.petclinic.vet.application.ports.output.SpecialityOutputPort
import tech.aaregall.lab.petclinic.vet.application.ports.output.VetOutputPort
import tech.aaregall.lab.petclinic.vet.application.ports.output.VetValidationOutputPort
import tech.aaregall.lab.petclinic.vet.domain.model.Vet
import tech.aaregall.lab.petclinic.vet.domain.model.VetId

@UseCase
internal class CreateVetUseCase(
    private val vetOutputPort: VetOutputPort,
    private val vetValidationOutputPort: VetValidationOutputPort,
    private val specialityOutputPort: SpecialityOutputPort
) : CreateVetInputPort {

    override fun createVet(createVetCommand: CreateVetCommand): Vet {
        val vetId = VetId.of(createVetCommand.identityId)
        if (!vetValidationOutputPort.isValidVetId(vetId)) throw CreateVetCommandException("Vet ID '$vetId' is not valid")

        if (createVetCommand.specialitiesIds.isEmpty()) throw CreateVetCommandException("It must have at least one Speciality")

        val specialities = createVetCommand.specialitiesIds
            .map {
                specialityOutputPort.loadSpeciality(it)
                    ?: throw CreateVetCommandException("Speciality '$it' does not exist")
            }

        val vet = Vet(id = VetId.create(), specialities = specialities)

        return vetOutputPort.createVet(vet)
    }
}