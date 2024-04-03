package tech.aaregall.lab.petclinic.pet.application.usecase

import tech.aaregall.lab.petclinic.common.UseCase
import tech.aaregall.lab.petclinic.pet.application.ports.input.DeletePetOwnerCommand
import tech.aaregall.lab.petclinic.pet.application.ports.input.DeletePetOwnerInputPort
import tech.aaregall.lab.petclinic.pet.application.ports.output.PetOutputPort
import tech.aaregall.lab.petclinic.pet.application.ports.output.PetOwnerOutputPort
import tech.aaregall.lab.petclinic.pet.domain.model.PetOwner

@UseCase
internal class DeletePetOwnerUseCase(
    private val petOwnerOutputPort: PetOwnerOutputPort,
    private val petOutputPort: PetOutputPort
) : DeletePetOwnerInputPort {

    override fun deletePetOwner(deletePetOwnerCommand: DeletePetOwnerCommand) {
        val petOwner = PetOwner(identityId = deletePetOwnerCommand.ownerIdentityId)
        petOwnerOutputPort.deletePetOwner(petOwner)
        petOutputPort.deletePetsByPetOwner(petOwner)
    }
}