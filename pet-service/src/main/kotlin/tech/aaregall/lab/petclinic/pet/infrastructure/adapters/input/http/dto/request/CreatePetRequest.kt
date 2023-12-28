package tech.aaregall.lab.petclinic.pet.infrastructure.adapters.input.http.dto.request

import com.fasterxml.jackson.annotation.JsonProperty
import io.micronaut.serde.annotation.Serdeable
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.PastOrPresent
import java.time.LocalDate
import java.util.UUID

@Serdeable
data class CreatePetRequest(

    @NotBlank
    @JsonProperty("type")
    val type: String,

    @NotBlank
    @JsonProperty("name")
    val name: String,

    @NotNull
    @PastOrPresent
    @JsonProperty("birth_date")
    val birthDate: LocalDate?,

    @JsonProperty("owner_identity_id")
    val ownerIdentityId: UUID? = null
)