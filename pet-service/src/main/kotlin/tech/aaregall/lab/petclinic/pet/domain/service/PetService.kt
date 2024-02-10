package tech.aaregall.lab.petclinic.pet.domain.service

import tech.aaregall.lab.petclinic.common.UseCase
import tech.aaregall.lab.petclinic.common.reactive.CollectionReactive
import tech.aaregall.lab.petclinic.common.reactive.UnitReactive
import tech.aaregall.lab.petclinic.pet.application.ports.input.AdoptPetCommand
import tech.aaregall.lab.petclinic.pet.application.ports.input.AdoptPetCommandException
import tech.aaregall.lab.petclinic.pet.application.ports.input.AdoptPetUseCase
import tech.aaregall.lab.petclinic.pet.application.ports.input.CountAllPetsUseCase
import tech.aaregall.lab.petclinic.pet.application.ports.input.CreatePetCommand
import tech.aaregall.lab.petclinic.pet.application.ports.input.CreatePetUseCase
import tech.aaregall.lab.petclinic.pet.application.ports.input.DeletePetCommand
import tech.aaregall.lab.petclinic.pet.application.ports.input.DeletePetCommandException
import tech.aaregall.lab.petclinic.pet.application.ports.input.DeletePetUseCase
import tech.aaregall.lab.petclinic.pet.application.ports.input.DeletePetsByPetOwnerCommand
import tech.aaregall.lab.petclinic.pet.application.ports.input.DeletePetsByPetOwnerUseCase
import tech.aaregall.lab.petclinic.pet.application.ports.input.LoadPetCommand
import tech.aaregall.lab.petclinic.pet.application.ports.input.LoadPetUseCase
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
): SearchPetsUseCase, LoadPetUseCase, CountAllPetsUseCase, CreatePetUseCase, AdoptPetUseCase, DeletePetUseCase, DeletePetsByPetOwnerUseCase {

    override fun searchPets(searchPetsCommand: SearchPetsCommand): CollectionReactive<Pet> =
        petOutputPort.findPets(searchPetsCommand.pageNumber, searchPetsCommand.pageSize)
            .loadOwners()

    override fun loadPet(loadPetCommand: LoadPetCommand): UnitReactive<Pet> =
        petOutputPort.loadPetById(loadPetCommand.petId)
            .flatMap { pet ->
                pet.owner?.let { petOwner ->
                    petOwnerOutputPort.loadPetOwner(LoadPetOwnerCommand(petOwner.identityId))
                        .map { pet.withOwner(it) }
                } ?: UnitReactive(pet)
            }

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

    override fun deletePetsByPetOwner(deletePetsByPetOwnerCommand: DeletePetsByPetOwnerCommand) {
        petOutputPort.deletePetsByPetOwner(PetOwner(deletePetsByPetOwnerCommand.ownerIdentityId))
    }

    private fun CreatePetCommand.toPet(petOwner: PetOwner? = null): Pet =
        Pet(id = PetId.create(), type = type, name = name, birthDate = birthDate, owner = petOwner)

    private fun CollectionReactive<Pet>.loadOwners(): CollectionReactive<Pet> {
        val pets = this.toFlux()

        val ownersMap = pets
            .filter { it.owner != null }
            .mapNotNull { it.owner!!.identityId }
            .distinct()
            .flatMapSequential { petOwnerOutputPort.loadPetOwner(LoadPetOwnerCommand(it)).toMono() }
            .collectMap({ petOwner -> petOwner!!.identityId }, { petOwner -> petOwner!! })
            .cache()

        return CollectionReactive(
            ownersMap.flatMapMany { map ->
                pets.map { pet ->
                    pet.owner?.let { petOwner ->
                        pet.withOwner(map[petOwner.identityId])
                    } ?: pet
                }
            }
        )
    }

}