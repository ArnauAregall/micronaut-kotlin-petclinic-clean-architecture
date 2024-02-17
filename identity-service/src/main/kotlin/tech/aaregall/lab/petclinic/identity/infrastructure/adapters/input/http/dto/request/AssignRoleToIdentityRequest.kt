package tech.aaregall.lab.petclinic.identity.infrastructure.adapters.input.http.dto.request

import com.fasterxml.jackson.annotation.JsonProperty
import io.micronaut.serde.annotation.Serdeable
import java.util.UUID

@Serdeable
data class AssignRoleToIdentityRequest(

    @JsonProperty("role_id")
    val roleId: UUID
)