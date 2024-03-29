package tech.aaregall.lab.petclinic.identity.application.ports.usecase

import tech.aaregall.lab.petclinic.common.UseCase
import tech.aaregall.lab.petclinic.identity.application.ports.input.LoadIdentityCommand
import tech.aaregall.lab.petclinic.identity.application.ports.input.LoadIdentityInputPort
import tech.aaregall.lab.petclinic.identity.application.ports.output.IdentityOutputPort
import tech.aaregall.lab.petclinic.identity.domain.model.Identity

@UseCase
internal class LoadIdentityUseCase(private val identityOutputPort: IdentityOutputPort): LoadIdentityInputPort {

    override fun loadIdentity(loadIdentityCommand: LoadIdentityCommand): Identity? =
        identityOutputPort.loadIdentityById(loadIdentityCommand.identityId)
}