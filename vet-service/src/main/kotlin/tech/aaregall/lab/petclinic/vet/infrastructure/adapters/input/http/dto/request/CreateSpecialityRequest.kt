package tech.aaregall.lab.petclinic.vet.infrastructure.adapters.input.http.dto.request

import com.fasterxml.jackson.annotation.JsonProperty
import io.micronaut.serde.annotation.Serdeable
import jakarta.annotation.Nullable
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Null
import jakarta.validation.constraints.Size

@Serdeable
data class CreateSpecialityRequest(

    @NotBlank
    @JsonProperty("name")
    val name: String,

    @Size(min = 1, max = 1000)
    @JsonProperty("description")
    val description: String? = null
)
