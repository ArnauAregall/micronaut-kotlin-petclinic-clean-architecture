package tech.aaregall.lab.petclinic.vet.application.usecase

import tech.aaregall.lab.petclinic.common.UseCase
import tech.aaregall.lab.petclinic.vet.application.ports.input.CountAllSpecialitiesInputPort
import tech.aaregall.lab.petclinic.vet.application.ports.output.SpecialityOutputPort

@UseCase
internal class CountAllSpecialitiesUseCase(private val specialityOutputPort: SpecialityOutputPort) :
    CountAllSpecialitiesInputPort {

    override fun countAllSpecialities(): Int = specialityOutputPort.countAll()
}