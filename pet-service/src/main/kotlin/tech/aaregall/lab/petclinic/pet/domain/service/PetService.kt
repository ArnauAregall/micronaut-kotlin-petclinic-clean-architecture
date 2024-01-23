package tech.aaregall.lab.petclinic.pet.domain.service

import tech.aaregall.lab.petclinic.common.UseCase
import tech.aaregall.lab.petclinic.common.reactive.CollectionReactive
import tech.aaregall.lab.petclinic.common.reactive.UnitReactive
import tech.aaregall.lab.petclinic.pet.application.ports.input.CountAllPetsUseCase
import tech.aaregall.lab.petclinic.pet.application.ports.input.CreatePetCommand
import tech.aaregall.lab.petclinic.pet.application.ports.input.CreatePetUseCase
import tech.aaregall.lab.petclinic.pet.application.ports.input.DeletePetsByPetOwnerCommand
import tech.aaregall.lab.petclinic.pet.application.ports.input.DeletePetsByPetOwnerUseCase
import tech.aaregall.lab.petclinic.pet.application.ports.input.SearchPetsCommand
import tech.aaregall.lab.petclinic.pet.application.ports.input.SearchPetsUseCase
import tech.aaregall.lab.petclinic.pet.application.ports.output.LoadPetOwnerCommand
import tech.aaregall.lab.petclinic.pet.application.ports.output.PetOutputPort
import tech.aaregall.lab.petclinic.pet.application.ports.output.PetOwnerOutputPort
import tech.aaregall.lab.petclinic.pet.domain.model.Pet
import tech.aaregall.lab.petclinic.pet.domain.model.PetId
import tech.aaregall.lab.petclinic.pet.domain.model.PetOwner

@UseCase
class PetService(
    private val petOutputPort: PetOutputPort,
    private val petOwnerOutputPort: PetOwnerOutputPort
): CreatePetUseCase, DeletePetsByPetOwnerUseCase, SearchPetsUseCase, CountAllPetsUseCase {

    override fun searchPets(searchPetsCommand: SearchPetsCommand): CollectionReactive<Pet> =
        petOutputPort.findPets(searchPetsCommand.pageNumber, searchPetsCommand.pageSize)

    override fun countAllPets(): UnitReactive<Long> = petOutputPort.countAllPets()

    override fun createPet(createPetCommand: CreatePetCommand): UnitReactive<Pet> {
        return createPetCommand.ownerIdentityId
            ?.let {
                petOwnerOutputPort.loadPetOwner(LoadPetOwnerCommand(it))
                    .map { petOwner -> createPetCommand.toPet(petOwner) }
                    .flatMap { pet -> petOutputPort.createPet(pet) }
            }
            ?: UnitReactive(createPetCommand.toPet())
                .flatMap(petOutputPort::createPet)
    }

    override fun deletePetsByPetOwner(deletePetsByPetOwnerCommand: DeletePetsByPetOwnerCommand) {
        petOutputPort.deletePetsByPetOwner(PetOwner(deletePetsByPetOwnerCommand.ownerIdentityId))
    }

    private fun CreatePetCommand.toPet(petOwner: PetOwner? = null): Pet =
        Pet(id = PetId.create(), type = type, name = name, birthDate = birthDate, owner = petOwner)

}