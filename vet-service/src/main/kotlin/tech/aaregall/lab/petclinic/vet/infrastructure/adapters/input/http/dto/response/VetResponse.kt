package tech.aaregall.lab.petclinic.vet.infrastructure.adapters.input.http.dto.response

import com.fasterxml.jackson.annotation.JsonProperty
import io.micronaut.serde.annotation.Serdeable
import tech.aaregall.lab.petclinic.vet.domain.model.Vet

@Serdeable
data class VetResponse(
    @JsonProperty("id")
    val id: String,

    @JsonProperty("specialities")
    val specialities: List<SpecialityResponse>
) {

    companion object {
        fun fromVet(vet: Vet): VetResponse =
            VetResponse(vet.id.toString(), vet.specialities.map(SpecialityResponse::fromSpeciality).toList())
    }
}