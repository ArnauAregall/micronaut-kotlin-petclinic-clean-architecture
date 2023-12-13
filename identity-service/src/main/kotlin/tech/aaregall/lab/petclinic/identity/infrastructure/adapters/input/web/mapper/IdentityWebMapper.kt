package tech.aaregall.lab.petclinic.identity.infrastructure.adapters.input.web.mapper

import jakarta.inject.Singleton
import tech.aaregall.lab.petclinic.identity.application.ports.input.CreateIdentityCommand
import tech.aaregall.lab.petclinic.identity.domain.model.Identity
import tech.aaregall.lab.petclinic.identity.infrastructure.adapters.input.web.dto.request.CreateIdentityRequest
import tech.aaregall.lab.petclinic.identity.infrastructure.adapters.input.web.dto.response.ContactDetailsDTO
import tech.aaregall.lab.petclinic.identity.infrastructure.adapters.input.web.dto.response.IdentityResponse

@Singleton
class IdentityWebMapper {

    fun mapCreateRequestToCommand(createIdentityRequest: CreateIdentityRequest) =
        CreateIdentityCommand(firstName = createIdentityRequest.firstName, lastName = createIdentityRequest.lastName)

    fun mapToResponse(identity: Identity): IdentityResponse =
        IdentityResponse(
            id = identity.id.toString(),
            firstName = identity.firstName,
            lastName = identity.lastName,
            contactDetailsDTO = identity.contactDetails?.let { ContactDetailsDTO(it.email, it.phoneNumber) }
        )

}