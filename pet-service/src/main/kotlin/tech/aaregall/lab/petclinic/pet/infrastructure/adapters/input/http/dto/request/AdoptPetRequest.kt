package tech.aaregall.lab.petclinic.pet.infrastructure.adapters.input.http.dto.request

import com.fasterxml.jackson.annotation.JsonProperty
import io.micronaut.serde.annotation.Serdeable
import jakarta.validation.constraints.NotNull
import java.util.*

@Serdeable
data class AdoptPetRequest(

    @NotNull(message = "Pet adopter Identity ID is required")
    @JsonProperty("owner_identity_id")
    val ownerIdentityId: UUID?

)