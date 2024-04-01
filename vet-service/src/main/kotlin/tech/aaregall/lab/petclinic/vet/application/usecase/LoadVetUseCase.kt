package tech.aaregall.lab.petclinic.vet.application.usecase

import tech.aaregall.lab.petclinic.common.UseCase
import tech.aaregall.lab.petclinic.vet.application.ports.input.LoadVetInputPort
import tech.aaregall.lab.petclinic.vet.application.ports.output.VetOutputPort
import tech.aaregall.lab.petclinic.vet.domain.model.Vet
import tech.aaregall.lab.petclinic.vet.domain.model.VetId

@UseCase
internal class LoadVetUseCase(private val vetOutputPort: VetOutputPort): LoadVetInputPort {

    override fun loadVet(vetId: VetId): Vet? = vetOutputPort.loadVet(vetId)

}