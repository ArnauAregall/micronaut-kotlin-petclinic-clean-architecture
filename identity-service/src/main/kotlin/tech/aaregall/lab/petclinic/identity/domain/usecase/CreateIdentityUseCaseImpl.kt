package tech.aaregall.lab.petclinic.identity.domain.usecase

import tech.aaregall.lab.petclinic.common.UseCase
import tech.aaregall.lab.petclinic.identity.application.ports.input.CreateIdentityCommand
import tech.aaregall.lab.petclinic.identity.application.ports.input.CreateIdentityUseCase
import tech.aaregall.lab.petclinic.identity.application.ports.output.IdentityEventPublisher
import tech.aaregall.lab.petclinic.identity.application.ports.output.IdentityOutputPort
import tech.aaregall.lab.petclinic.identity.domain.event.IdentityCreatedEvent
import tech.aaregall.lab.petclinic.identity.domain.model.Identity
import tech.aaregall.lab.petclinic.identity.domain.model.IdentityId

@UseCase
internal class CreateIdentityUseCaseImpl(
    private val identityOutputPort: IdentityOutputPort,
    private val identityEventPublisher: IdentityEventPublisher): CreateIdentityUseCase {

    override fun createIdentity(createIdentityCommand: CreateIdentityCommand): Identity {
        val identity = identityOutputPort.createIdentity(
            Identity(IdentityId.create(), createIdentityCommand.firstName, createIdentityCommand.lastName))
        identityEventPublisher.publishIdentityCreatedEvent(IdentityCreatedEvent(identity))
        return identity
    }
}