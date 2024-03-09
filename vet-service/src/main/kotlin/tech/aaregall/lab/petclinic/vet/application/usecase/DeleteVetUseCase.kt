package tech.aaregall.lab.petclinic.vet.application.usecase

import tech.aaregall.lab.petclinic.common.UseCase
import tech.aaregall.lab.petclinic.vet.application.ports.input.DeleteVetCommand
import tech.aaregall.lab.petclinic.vet.application.ports.input.DeleteVetCommandException
import tech.aaregall.lab.petclinic.vet.application.ports.input.DeleteVetInputPort
import tech.aaregall.lab.petclinic.vet.application.ports.output.VetOutputPort

@UseCase
internal class DeleteVetUseCase(private val vetOutputPort: VetOutputPort) : DeleteVetInputPort {

    override fun deleteVet(deleteVetCommand: DeleteVetCommand) {
        val vet = vetOutputPort.loadVet(deleteVetCommand.vetId)
            ?: throw DeleteVetCommandException("Cannot delete a non existing Vet")

        vetOutputPort.deleteVet(vet)
    }
}