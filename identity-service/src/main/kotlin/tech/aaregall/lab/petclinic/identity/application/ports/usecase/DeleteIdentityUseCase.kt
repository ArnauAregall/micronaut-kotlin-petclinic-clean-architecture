package tech.aaregall.lab.petclinic.identity.application.ports.usecase

import tech.aaregall.lab.petclinic.common.UseCase
import tech.aaregall.lab.petclinic.identity.application.ports.input.DeleteIdentityCommand
import tech.aaregall.lab.petclinic.identity.application.ports.input.DeleteIdentityInputPort
import tech.aaregall.lab.petclinic.identity.application.ports.output.IdentityEventPublisher
import tech.aaregall.lab.petclinic.identity.application.ports.output.IdentityOutputPort
import tech.aaregall.lab.petclinic.identity.domain.event.IdentityDeletedEvent

@UseCase
internal class DeleteIdentityUseCase(
    private val identityOutputPort: IdentityOutputPort,
    private val identityEventPublisher: IdentityEventPublisher): DeleteIdentityInputPort {

    override fun deleteIdentity(deleteIdentityCommand: DeleteIdentityCommand) {
        val identity = identityOutputPort.loadIdentityById(deleteIdentityCommand.identityId)

        require(identity != null) { "Cannot delete a non existing Identity" }

        identityOutputPort.deleteIdentity(identity)
        identityEventPublisher.publishIdentityDeletedEvent(IdentityDeletedEvent(identity.id))
    }
}