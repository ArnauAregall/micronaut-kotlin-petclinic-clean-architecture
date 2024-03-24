package tech.aaregall.lab.petclinic.vet.infrastructure.adapters.input.http.dto.response

import com.fasterxml.jackson.annotation.JsonProperty
import io.micronaut.serde.annotation.Serdeable
import tech.aaregall.lab.petclinic.vet.domain.model.Speciality

@Serdeable
data class SpecialityResponse(

    @JsonProperty("id")
    val id: String,

    @JsonProperty("name")
    val name: String,

    @JsonProperty("description")
    val description: String?
) {

    companion object {
        fun fromSpeciality(speciality: Speciality): SpecialityResponse =
            SpecialityResponse(speciality.id.toString(), speciality.name, speciality.description)
    }

}
