package tech.aaregall.lab.petclinic.pet.infrastructure.adapters.input.http.dto.response

import com.fasterxml.jackson.annotation.JsonProperty
import io.micronaut.serde.annotation.Serdeable
import java.time.LocalDate

@Serdeable
data class PetResponse(

    @JsonProperty("id")
    val id: String,

    @JsonProperty("type")
    val type: String,

    @JsonProperty("name")
    val name: String,

    @JsonProperty("birth_date")
    val birthDate: LocalDate,

    @JsonProperty("owner")
    val petOwnerDTO: PetOwnerDTO?

)

@Serdeable
data class PetOwnerDTO(

    @JsonProperty("id")
    val id: String

)
