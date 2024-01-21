package tech.aaregall.lab.petclinic.pet.domain.model

import java.io.Serializable
import java.util.UUID

data class PetOwner(val identityId: UUID, val firstName: String? = null, val lastName: String? = null): Serializable