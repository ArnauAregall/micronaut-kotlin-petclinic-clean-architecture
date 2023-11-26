package tech.aaregall.lab.micronaut.petclinic.identity.infrastructure.adapters.input.web.dto.response

import com.fasterxml.jackson.annotation.JsonProperty
import io.micronaut.serde.annotation.Serdeable

@Serdeable
data class IdentityResponse(

    @JsonProperty("id")
    val id: String,

    @JsonProperty("first_name")
    val firstName: String,

    @JsonProperty("last_name")
    val lastName: String
)