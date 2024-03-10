package tech.aaregall.lab.petclinic.vet.application.usecase

import tech.aaregall.lab.petclinic.common.UseCase
import tech.aaregall.lab.petclinic.vet.application.ports.input.SearchSpecialitiesCommand
import tech.aaregall.lab.petclinic.vet.application.ports.input.SearchSpecialitiesInputPort
import tech.aaregall.lab.petclinic.vet.application.ports.output.SpecialityOutputPort
import tech.aaregall.lab.petclinic.vet.domain.model.Speciality

@UseCase
internal class SearchSpecialitiesUseCase(private val specialityOutputPort: SpecialityOutputPort): SearchSpecialitiesInputPort {

    override fun searchSpecialities(searchSpecialitiesCommand: SearchSpecialitiesCommand): Collection<Speciality>? =
        specialityOutputPort.findSpecialities(searchSpecialitiesCommand.pageNumber, searchSpecialitiesCommand.pageSize)
            .orEmpty()

}