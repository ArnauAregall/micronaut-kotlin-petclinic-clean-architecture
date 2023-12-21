package tech.aaregall.lab.petclinic.pet.infrastructure.adapters.input.http.mapper

import jakarta.inject.Singleton
import tech.aaregall.lab.petclinic.pet.application.ports.input.CreatePetCommand
import tech.aaregall.lab.petclinic.pet.domain.model.Pet
import tech.aaregall.lab.petclinic.pet.domain.model.PetType
import tech.aaregall.lab.petclinic.pet.infrastructure.adapters.input.http.dto.request.CreatePetRequest
import tech.aaregall.lab.petclinic.pet.infrastructure.adapters.input.http.dto.response.PetOwnerDTO
import tech.aaregall.lab.petclinic.pet.infrastructure.adapters.input.http.dto.response.PetResponse

@Singleton
class PetHttpMapper {

    fun mapCreateRequestToCommand(createPetRequest: CreatePetRequest): CreatePetCommand =
        CreatePetCommand(
            type = PetType.valueOf(createPetRequest.type),
            name = createPetRequest.name,
            birthDate = createPetRequest.birthDate,
            ownerIdentityId = createPetRequest.ownerIdentityId
        )

    fun mapToResponse(pet: Pet): PetResponse =
        PetResponse(
            id = pet.id.toString(),
            type = pet.type.toString(),
            name = pet.name,
            birthDate = pet.birthDate,
            petOwnerDTO = pet.owner?.let { PetOwnerDTO(it.identityId.toString()) }
        )

}