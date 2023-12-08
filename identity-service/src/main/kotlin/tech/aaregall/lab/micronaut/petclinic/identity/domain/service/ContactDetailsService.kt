package tech.aaregall.lab.micronaut.petclinic.identity.domain.service

import tech.aaregall.lab.micronaut.petclinic.identity.application.ports.input.LoadIdentityCommand
import tech.aaregall.lab.micronaut.petclinic.identity.application.ports.input.LoadIdentityUseCase
import tech.aaregall.lab.micronaut.petclinic.identity.application.ports.input.UpdateIdentityContactDetailsCommand
import tech.aaregall.lab.micronaut.petclinic.identity.application.ports.input.UpdateIdentityContactDetailsUseCase
import tech.aaregall.lab.micronaut.petclinic.identity.application.ports.output.ContactDetailsOutputPort
import tech.aaregall.lab.micronaut.petclinic.identity.common.UseCase
import tech.aaregall.lab.micronaut.petclinic.identity.domain.model.ContactDetails

@UseCase
class ContactDetailsService(
    private val contactDetailsOutputPort: ContactDetailsOutputPort,
    private val loadIdentityUseCase: LoadIdentityUseCase
) : UpdateIdentityContactDetailsUseCase {

    override fun updateIdentityContactDetails(updateIdentityContactDetailsCommand: UpdateIdentityContactDetailsCommand): ContactDetails {
        val identity = loadIdentityUseCase.loadIdentity(LoadIdentityCommand(updateIdentityContactDetailsCommand.identityId))

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