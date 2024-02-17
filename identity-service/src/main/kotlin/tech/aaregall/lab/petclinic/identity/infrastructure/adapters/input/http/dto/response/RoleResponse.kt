package tech.aaregall.lab.petclinic.identity.infrastructure.adapters.input.http.dto.response

import com.fasterxml.jackson.annotation.JsonProperty
import io.micronaut.serde.annotation.Serdeable

@Serdeable
data class RoleResponse(

    @JsonProperty("id")
    val id: String,

    @JsonProperty("name")
    val name: String

)
