package tech.aaregall.lab.petclinic.pet.domain.usecase

import tech.aaregall.lab.petclinic.common.UseCase
import tech.aaregall.lab.petclinic.common.reactive.UnitReactive
import tech.aaregall.lab.petclinic.pet.application.ports.input.CountAllPetsUseCase
import tech.aaregall.lab.petclinic.pet.application.ports.output.PetOutputPort

@UseCase
internal class CountAllPetsUseCaseImpl(private val petOutputPort: PetOutputPort): CountAllPetsUseCase {

    override fun countAllPets(): UnitReactive<Long> = petOutputPort.countAllPets()

}