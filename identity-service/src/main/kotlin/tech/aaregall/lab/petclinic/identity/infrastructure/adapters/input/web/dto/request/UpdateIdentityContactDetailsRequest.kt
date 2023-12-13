package tech.aaregall.lab.petclinic.identity.infrastructure.adapters.input.web.dto.request

import com.fasterxml.jackson.annotation.JsonProperty
import io.micronaut.serde.annotation.Serdeable
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@Serdeable
data class UpdateIdentityContactDetailsRequest(

    @NotBlank
    @Size(max = 100, message = "is too long, max 100 characters")
    @JsonProperty("email")
    val email: String,

    @NotBlank
    @Size(max = 20, message = "is too long, max 20 characters")
    @JsonProperty("phone_number")
    val phoneNumber: String

)