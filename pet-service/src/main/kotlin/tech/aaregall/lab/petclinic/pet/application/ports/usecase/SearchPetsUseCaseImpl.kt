package tech.aaregall.lab.petclinic.pet.application.ports.usecase

import tech.aaregall.lab.petclinic.common.UseCase
import tech.aaregall.lab.petclinic.common.reactive.CollectionReactive
import tech.aaregall.lab.petclinic.pet.application.ports.input.SearchPetsCommand
import tech.aaregall.lab.petclinic.pet.application.ports.input.SearchPetsUseCase
import tech.aaregall.lab.petclinic.pet.application.ports.output.LoadPetOwnerCommand
import tech.aaregall.lab.petclinic.pet.application.ports.output.PetOutputPort
import tech.aaregall.lab.petclinic.pet.application.ports.output.PetOwnerOutputPort
import tech.aaregall.lab.petclinic.pet.domain.model.Pet

@UseCase
internal class SearchPetsUseCaseImpl(
    private val petOutputPort: PetOutputPort,
    private val petOwnerOutputPort: PetOwnerOutputPort
): SearchPetsUseCase {

    override fun searchPets(searchPetsCommand: SearchPetsCommand): CollectionReactive<Pet> =
        petOutputPort.findPets(searchPetsCommand.pageNumber, searchPetsCommand.pageSize)
            .loadOwners()

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