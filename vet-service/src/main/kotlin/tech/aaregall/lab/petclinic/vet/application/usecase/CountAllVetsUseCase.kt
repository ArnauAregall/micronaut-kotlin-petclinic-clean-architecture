package tech.aaregall.lab.petclinic.vet.application.usecase

import tech.aaregall.lab.petclinic.common.UseCase
import tech.aaregall.lab.petclinic.vet.application.ports.input.CountAllVetsInputPort
import tech.aaregall.lab.petclinic.vet.application.ports.output.VetOutputPort

@UseCase
internal class CountAllVetsUseCase(private val vetOutputPort: VetOutputPort): CountAllVetsInputPort {

    override fun countAllVets(): Int = vetOutputPort.countAll()

}