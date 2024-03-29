package tech.aaregall.lab.petclinic.pet.application.usecase

import tech.aaregall.lab.petclinic.common.UseCase
import tech.aaregall.lab.petclinic.common.reactive.UnitReactive
import tech.aaregall.lab.petclinic.pet.application.ports.input.LoadPetCommand
import tech.aaregall.lab.petclinic.pet.application.ports.input.LoadPetInputPort
import tech.aaregall.lab.petclinic.pet.application.ports.output.LoadPetOwnerCommand
import tech.aaregall.lab.petclinic.pet.application.ports.output.PetOutputPort
import tech.aaregall.lab.petclinic.pet.application.ports.output.PetOwnerOutputPort
import tech.aaregall.lab.petclinic.pet.domain.model.Pet

@UseCase
internal class LoadPetUseCase(
    private val petOutputPort: PetOutputPort,
    private val petOwnerOutputPort: PetOwnerOutputPort
): LoadPetInputPort {

    override fun loadPet(loadPetCommand: LoadPetCommand): UnitReactive<Pet> =
        petOutputPort.loadPetById(loadPetCommand.petId)
            .flatMap { pet ->
                pet.owner?.let { petOwner ->
                    petOwnerOutputPort.loadPetOwner(LoadPetOwnerCommand(petOwner.identityId))
                        .map { pet.withOwner(it) }
                } ?: UnitReactive(pet)
            }
}