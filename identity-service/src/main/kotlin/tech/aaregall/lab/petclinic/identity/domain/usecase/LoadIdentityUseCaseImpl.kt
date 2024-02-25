package tech.aaregall.lab.petclinic.identity.domain.usecase

import tech.aaregall.lab.petclinic.common.UseCase
import tech.aaregall.lab.petclinic.identity.application.ports.input.LoadIdentityCommand
import tech.aaregall.lab.petclinic.identity.application.ports.input.LoadIdentityUseCase
import tech.aaregall.lab.petclinic.identity.application.ports.output.IdentityOutputPort
import tech.aaregall.lab.petclinic.identity.domain.model.Identity

@UseCase
internal class LoadIdentityUseCaseImpl(private val identityOutputPort: IdentityOutputPort): LoadIdentityUseCase {

    override fun loadIdentity(loadIdentityCommand: LoadIdentityCommand): Identity? =
        identityOutputPort.loadIdentityById(loadIdentityCommand.identityId)
}