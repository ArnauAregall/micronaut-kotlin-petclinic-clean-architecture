package tech.aaregall.lab.petclinic.identity.application.ports.usecase

import tech.aaregall.lab.petclinic.common.UseCase
import tech.aaregall.lab.petclinic.identity.application.ports.input.UpdateIdentityContactDetailsCommand
import tech.aaregall.lab.petclinic.identity.application.ports.input.UpdateIdentityContactDetailsInputPort
import tech.aaregall.lab.petclinic.identity.application.ports.output.ContactDetailsOutputPort
import tech.aaregall.lab.petclinic.identity.application.ports.output.IdentityOutputPort
import tech.aaregall.lab.petclinic.identity.domain.model.ContactDetails

@UseCase
internal class UpdateIdentityContactDetailsUseCase(
    private val identityOutputPort: IdentityOutputPort,
    private val contactDetailsOutputPort: ContactDetailsOutputPort): UpdateIdentityContactDetailsInputPort {

    override fun updateIdentityContactDetails(updateIdentityContactDetailsCommand: UpdateIdentityContactDetailsCommand): ContactDetails {
        val identity = identityOutputPort.loadIdentityById(updateIdentityContactDetailsCommand.identityId)

        require(identity != null) { "Cannot update ContactDetails for a non existing Identity" }

        return contactDetailsOutputPort.updateIdentityContactDetails(
            identity,
            ContactDetails(
                updateIdentityContactDetailsCommand.email,
                updateIdentityContactDetailsCommand.phoneNumber
            )
        )
    }
}