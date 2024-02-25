package tech.aaregall.lab.petclinic.vet.domain.service

import tech.aaregall.lab.petclinic.common.UseCase
import tech.aaregall.lab.petclinic.vet.application.ports.input.CreateSpecialityCommand
import tech.aaregall.lab.petclinic.vet.application.ports.input.CreateSpecialityCommandException
import tech.aaregall.lab.petclinic.vet.application.ports.input.CreateSpecialityUseCase
import tech.aaregall.lab.petclinic.vet.application.ports.output.SpecialityOutputPort
import tech.aaregall.lab.petclinic.vet.domain.model.Speciality
import tech.aaregall.lab.petclinic.vet.domain.model.SpecialityId

@UseCase
class SpecialityService(private val specialityOutputPort: SpecialityOutputPort): CreateSpecialityUseCase {

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