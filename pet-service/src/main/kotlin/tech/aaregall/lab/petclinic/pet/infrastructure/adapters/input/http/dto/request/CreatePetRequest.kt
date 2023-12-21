package tech.aaregall.lab.petclinic.pet.infrastructure.adapters.input.http.dto.request

import com.fasterxml.jackson.annotation.JsonProperty
import io.micronaut.serde.annotation.Serdeable
import java.time.LocalDate
import java.util.UUID

@Serdeable
data class CreatePetRequest(

    @JsonProperty("type")
    val type: String,

    @JsonProperty("name")
    val name: String,

    @JsonProperty("birth_date")
    val birthDate: LocalDate,

    @JsonProperty("owner_identity_id")
    val ownerIdentityId: UUID? = null
)