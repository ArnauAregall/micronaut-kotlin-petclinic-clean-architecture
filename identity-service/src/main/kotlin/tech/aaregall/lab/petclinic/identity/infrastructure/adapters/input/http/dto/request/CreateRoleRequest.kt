package tech.aaregall.lab.petclinic.identity.infrastructure.adapters.input.http.dto.request

import com.fasterxml.jackson.annotation.JsonProperty
import io.micronaut.serde.annotation.Serdeable
import jakarta.validation.constraints.NotBlank

@Serdeable
data class CreateRoleRequest(

    @NotBlank
    @JsonProperty("name")
    val name: String

)