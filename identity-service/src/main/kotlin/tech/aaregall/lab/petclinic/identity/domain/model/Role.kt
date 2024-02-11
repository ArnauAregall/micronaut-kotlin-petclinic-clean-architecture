package tech.aaregall.lab.petclinic.identity.domain.model

import java.util.UUID

data class Role(val id: RoleId, val name: String) {
    init {
        require(name.isNotBlank()) { "name cannot be blank" }
    }
}

data class RoleId(private val value: UUID) {
    companion object {
        fun create() = RoleId(UUID.randomUUID())

        fun of(uuid: UUID) = RoleId(uuid)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RoleId

        return value == other.value
    }

    override fun toString() = value.toString()

    override fun hashCode() = value.hashCode()
}