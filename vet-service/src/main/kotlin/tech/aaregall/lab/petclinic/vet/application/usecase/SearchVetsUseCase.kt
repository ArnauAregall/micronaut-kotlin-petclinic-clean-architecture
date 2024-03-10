package tech.aaregall.lab.petclinic.vet.application.usecase

import tech.aaregall.lab.petclinic.common.UseCase
import tech.aaregall.lab.petclinic.vet.application.ports.input.SearchVetsCommand
import tech.aaregall.lab.petclinic.vet.application.ports.input.SearchVetsInputPort
import tech.aaregall.lab.petclinic.vet.application.ports.output.VetOutputPort
import tech.aaregall.lab.petclinic.vet.domain.model.Vet

@UseCase
internal class SearchVetsUseCase(private val vetOutputPort: VetOutputPort): SearchVetsInputPort {

    override fun searchVets(searchVetsCommand: SearchVetsCommand): Collection<Vet> =
        vetOutputPort.findVets(searchVetsCommand.pageNumber, searchVetsCommand.pageSize)
            .orEmpty()

}