package tech.aaregall.lab.petclinic.pet.application.usecase

import tech.aaregall.lab.petclinic.common.UseCase
import tech.aaregall.lab.petclinic.common.reactive.UnitReactive
import tech.aaregall.lab.petclinic.pet.application.ports.input.AdoptPetCommand
import tech.aaregall.lab.petclinic.pet.application.ports.input.AdoptPetCommandException
import tech.aaregall.lab.petclinic.pet.application.ports.input.AdoptPetInputPort
import tech.aaregall.lab.petclinic.pet.application.ports.output.LoadPetOwnerCommand
import tech.aaregall.lab.petclinic.pet.application.ports.output.PetOutputPort
import tech.aaregall.lab.petclinic.pet.application.ports.output.PetOwnerOutputPort
import tech.aaregall.lab.petclinic.pet.domain.model.Pet
import tech.aaregall.lab.petclinic.pet.domain.model.PetOwner

@UseCase
internal class AdoptPetUseCase(
    private val petOutputPort: PetOutputPort,
    private val petOwnerOutputPort: PetOwnerOutputPort
) : AdoptPetInputPort {

    override fun adoptPet(adoptPetCommand: AdoptPetCommand): UnitReactive<Pet> =
        petOutputPort.loadPetById(adoptPetCommand.petId)
            .flatMap { pet ->
                UnitReactive(
                    petOwnerOutputPort.loadPetOwner(LoadPetOwnerCommand(adoptPetCommand.ownerIdentityId)).toMono()
                        .switchIfEmpty(
                            UnitReactive.error<PetOwner>(AdoptPetCommandException("Could not load the adopter PetOwner with ID ${adoptPetCommand.ownerIdentityId}")).toMono()
                        )
                        .map { petOwner -> pet.withOwner(petOwner) }
                        .flatMap { petOutputPort.updatePet(it).toMono() }
                )
            }
}