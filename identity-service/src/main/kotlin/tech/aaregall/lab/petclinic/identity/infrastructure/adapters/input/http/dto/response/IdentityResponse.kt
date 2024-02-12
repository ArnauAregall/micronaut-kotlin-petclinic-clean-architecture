package tech.aaregall.lab.petclinic.identity.infrastructure.adapters.input.http.dto.response

import com.fasterxml.jackson.annotation.JsonProperty
import io.micronaut.serde.annotation.Serdeable

@Serdeable
data class IdentityResponse(

    @JsonProperty("id")
    val id: String,

    @JsonProperty("first_name")
    val firstName: String,

    @JsonProperty("last_name")
    val lastName: String,

    @JsonProperty("contact_details")
    val contactDetailsDTO: ContactDetailsDTO? = null,

    @JsonProperty("roles")
    val roles: Collection<String> = emptyList()
)

@Serdeable
data class ContactDetailsDTO(

    @JsonProperty("email")
    val email: String,

    @JsonProperty("phone_number")
    val phoneNumber: String
)