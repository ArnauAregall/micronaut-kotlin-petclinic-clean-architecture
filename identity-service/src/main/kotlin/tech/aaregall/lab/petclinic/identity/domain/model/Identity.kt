package tech.aaregall.lab.petclinic.identity.domain.model

import java.util.UUID

data class Identity(val id: IdentityId, val firstName: String, val lastName: String, val contactDetails: ContactDetails? = null) {
    init {
        require(firstName.isNotBlank()) { "firstName cannot be blank" }
        require(lastName.isNotBlank()) { "lastName cannot be blank" }
    }
}

data class IdentityId(private val value: UUID) {
    companion object {
        fun create() = IdentityId(UUID.randomUUID())

        fun of(uuid: UUID) = IdentityId(uuid)

        fun of(string: String) = of(UUID.fromString(string))
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IdentityId

        return value == other.value
    }

    override fun toString() = value.toString()

    override fun hashCode() = value.hashCode()

}