package tech.aaregall.lab.micronaut.petclinic.identity.application.ports.input

import tech.aaregall.lab.micronaut.petclinic.identity.domain.model.ContactDetails
import tech.aaregall.lab.micronaut.petclinic.identity.domain.model.IdentityId

fun interface UpdateIdentityContactDetailsUseCase {

    fun updateIdentityContactDetails(updateIdentityContactDetailsCommand: UpdateIdentityContactDetailsCommand): ContactDetails
}

data class UpdateIdentityContactDetailsCommand(val identityId: IdentityId, val email: String, val phoneNumber: String)