package tech.aaregall.lab.petclinic.identity.domain.event

import tech.aaregall.lab.petclinic.identity.domain.model.IdentityId
import java.time.Instant

data class IdentityDeletedEvent(val identityId: IdentityId) {

    val date : Instant = Instant.now()
}
