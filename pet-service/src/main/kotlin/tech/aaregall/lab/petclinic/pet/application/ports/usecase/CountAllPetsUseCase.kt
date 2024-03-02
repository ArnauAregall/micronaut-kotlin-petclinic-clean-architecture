package tech.aaregall.lab.petclinic.pet.application.ports.usecase

import tech.aaregall.lab.petclinic.common.UseCase
import tech.aaregall.lab.petclinic.common.reactive.UnitReactive
import tech.aaregall.lab.petclinic.pet.application.ports.input.CountAllPetsInputPort
import tech.aaregall.lab.petclinic.pet.application.ports.output.PetOutputPort

@UseCase
internal class CountAllPetsUseCase(private val petOutputPort: PetOutputPort): CountAllPetsInputPort {

    override fun countAllPets(): UnitReactive<Long> = petOutputPort.countAllPets()

}