package tech.aaregall.lab.petclinic.pet.application.usecase

import tech.aaregall.lab.petclinic.common.UseCase
import tech.aaregall.lab.petclinic.common.reactive.UnitReactive
import tech.aaregall.lab.petclinic.pet.application.ports.input.DeletePetCommand
import tech.aaregall.lab.petclinic.pet.application.ports.input.DeletePetCommandException
import tech.aaregall.lab.petclinic.pet.application.ports.input.DeletePetInputPort
import tech.aaregall.lab.petclinic.pet.application.ports.output.PetOutputPort
import tech.aaregall.lab.petclinic.pet.domain.model.Pet

@UseCase
internal class DeletePetUseCase(private val petOutputPort: PetOutputPort): DeletePetInputPort {

    override fun deletePet(deletePetCommand: DeletePetCommand): UnitReactive<Unit> =
        UnitReactive(
            petOutputPort.loadPetById(deletePetCommand.petId).toMono()
                .switchIfEmpty(UnitReactive.error<Pet>(DeletePetCommandException(deletePetCommand, "Pet was not found")).toMono())
                .flatMap { pet ->
                    petOutputPort.deletePet(pet).toMono()
                        .flatMap { canBeDeleted ->
                            if (!canBeDeleted) UnitReactive.error<Unit>(DeletePetCommandException(deletePetCommand, "Pet cannot be deleted")).toMono()
                            else UnitReactive(Unit).toMono()
                        }
                }
        )

}