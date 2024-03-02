package tech.aaregall.lab.petclinic.pet.application.usecase

import tech.aaregall.lab.petclinic.common.UseCase
import tech.aaregall.lab.petclinic.common.reactive.UnitReactive
import tech.aaregall.lab.petclinic.pet.application.ports.input.CreatePetCommand
import tech.aaregall.lab.petclinic.pet.application.ports.input.CreatePetCommandException
import tech.aaregall.lab.petclinic.pet.application.ports.input.CreatePetInputPort
import tech.aaregall.lab.petclinic.pet.application.ports.output.LoadPetOwnerCommand
import tech.aaregall.lab.petclinic.pet.application.ports.output.PetOutputPort
import tech.aaregall.lab.petclinic.pet.application.ports.output.PetOwnerOutputPort
import tech.aaregall.lab.petclinic.pet.domain.model.Pet
import tech.aaregall.lab.petclinic.pet.domain.model.PetId
import tech.aaregall.lab.petclinic.pet.domain.model.PetOwner

@UseCase
internal class CreatePetUseCase(
    private val petOutputPort: PetOutputPort,
    private val petOwnerOutputPort: PetOwnerOutputPort
) : CreatePetInputPort {

    override fun createPet(createPetCommand: CreatePetCommand): UnitReactive<Pet> =
        createPetCommand.ownerIdentityId
            ?.let { ownerIdentityId ->
                UnitReactive(
                    petOwnerOutputPort.loadPetOwner(LoadPetOwnerCommand(ownerIdentityId)).toMono()
                        .switchIfEmpty(UnitReactive.error<PetOwner>(CreatePetCommandException("Could not load the PetOwner with ID ${createPetCommand.ownerIdentityId}")).toMono())
                        .map { petOwner -> createPetCommand.toPet(petOwner) }
                        .flatMap { pet -> petOutputPort.createPet(pet).toMono() }
                )
            }
            ?: UnitReactive(createPetCommand.toPet())
                .flatMap(petOutputPort::createPet)

    private fun CreatePetCommand.toPet(petOwner: PetOwner? = null): Pet =
        Pet(id = PetId.create(), type = type, name = name, birthDate = birthDate, owner = petOwner)

}