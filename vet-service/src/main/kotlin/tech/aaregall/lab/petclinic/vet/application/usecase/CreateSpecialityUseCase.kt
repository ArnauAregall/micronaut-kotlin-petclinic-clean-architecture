package tech.aaregall.lab.petclinic.vet.application.usecase

import tech.aaregall.lab.petclinic.common.UseCase
import tech.aaregall.lab.petclinic.vet.application.ports.input.CreateSpecialityCommand
import tech.aaregall.lab.petclinic.vet.application.ports.input.CreateSpecialityCommandException
import tech.aaregall.lab.petclinic.vet.application.ports.input.CreateSpecialityInputPort
import tech.aaregall.lab.petclinic.vet.application.ports.output.SpecialityOutputPort
import tech.aaregall.lab.petclinic.vet.domain.model.Speciality
import tech.aaregall.lab.petclinic.vet.domain.model.SpecialityId

@UseCase
internal class CreateSpecialityUseCase(private val specialityOutputPort: SpecialityOutputPort): CreateSpecialityInputPort {

    override fun createSpeciality(createSpecialityCommand: CreateSpecialityCommand): Speciality {
        if (specialityOutputPort.specialityExistsByName(createSpecialityCommand.name)) {
            throw CreateSpecialityCommandException("Speciality with name '${createSpecialityCommand.name}' already exists")
        }
        return specialityOutputPort.createSpeciality(
            Speciality(
                id = SpecialityId.create(),
                name = createSpecialityCommand.name,
                description = createSpecialityCommand.description
            )
        )
    }
}