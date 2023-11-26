package tech.aaregall.lab.micronaut.petclinic.identity.infrastructure.adapters.input.web.dto.request

import com.fasterxml.jackson.annotation.JsonProperty
import io.micronaut.serde.annotation.Serdeable

@Serdeable
data class CreateIdentityRequest(
    @JsonProperty("first_name")
    val firstName: String,

    @JsonProperty("last_name")
    val lastName: String
)