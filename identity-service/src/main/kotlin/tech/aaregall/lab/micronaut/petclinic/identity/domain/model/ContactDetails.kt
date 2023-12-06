package tech.aaregall.lab.micronaut.petclinic.identity.domain.model

data class ContactDetails(
    val email: String, val phoneNumber: String
) {
    init {
        require(email.isNotBlank()) { "email cannot be blank" }
        require(phoneNumber.isNotBlank()) { "phoneNumber cannot be blank" }
    }
}