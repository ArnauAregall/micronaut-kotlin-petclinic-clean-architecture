package tech.aaregall.lab.micronaut.petclinic.pet.domain.service

import tech.aaregall.lab.micronaut.petclinic.pet.application.ports.input.CreatePetCommand
import tech.aaregall.lab.micronaut.petclinic.pet.application.ports.input.CreatePetUseCase
import tech.aaregall.lab.micronaut.petclinic.pet.application.ports.output.PetOutputPort
import tech.aaregall.lab.micronaut.petclinic.pet.domain.model.Pet
import tech.aaregall.lab.micronaut.petclinic.pet.domain.model.PetId
import tech.aaregall.lab.micronaut.petclinic.pet.domain.model.PetOwner
import tech.aaregall.lab.petclinic.common.UseCase

@UseCase
class PetService(private val petOutputPort: PetOutputPort): CreatePetUseCase {

    override fun createPet(createPetCommand: CreatePetCommand): Pet =
        petOutputPort.createPet(Pet(
            id = PetId.create(),
            type = createPetCommand.type,
            name = createPetCommand.name,
            birthDate = createPetCommand.birthDate,
            owner = createPetCommand.ownerIdentityId?.let { PetOwner(it) }
        ))
}