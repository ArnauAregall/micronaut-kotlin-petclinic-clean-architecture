package tech.aaregall.lab.petclinic.pet.application.ports.usecase

import tech.aaregall.lab.petclinic.common.UseCase
import tech.aaregall.lab.petclinic.pet.application.ports.input.DeletePetsByPetOwnerCommand
import tech.aaregall.lab.petclinic.pet.application.ports.input.DeletePetsByPetOwnerUseCase
import tech.aaregall.lab.petclinic.pet.application.ports.output.PetOutputPort
import tech.aaregall.lab.petclinic.pet.domain.model.PetOwner

@UseCase
internal class DeletePetsByPetOwnerUseCaseImpl(private val petOutputPort: PetOutputPort) : DeletePetsByPetOwnerUseCase {

    override fun deletePetsByPetOwner(deletePetsByPetOwnerCommand: DeletePetsByPetOwnerCommand) {
        petOutputPort.deletePetsByPetOwner(PetOwner(deletePetsByPetOwnerCommand.ownerIdentityId))
    }
}