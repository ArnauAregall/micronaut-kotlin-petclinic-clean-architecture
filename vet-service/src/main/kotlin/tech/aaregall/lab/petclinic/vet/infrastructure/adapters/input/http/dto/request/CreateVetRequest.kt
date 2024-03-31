package tech.aaregall.lab.petclinic.vet.infrastructure.adapters.input.http.dto.request

import com.fasterxml.jackson.annotation.JsonProperty
import io.micronaut.serde.annotation.Serdeable
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import java.util.UUID

@Serdeable
data class CreateVetRequest(

    @NotNull(message = "Vet Identity ID is required")
    @JsonProperty("identity_id")
    val identityId: UUID?,

    @NotEmpty(message = "Specialities IDs cannot be empty")
    @JsonProperty("specialities_ids")
    val specialitiesIds: Collection<UUID>
)