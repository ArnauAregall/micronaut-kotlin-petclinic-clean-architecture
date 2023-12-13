package tech.aaregall.lab.petclinic.pet.domain.model

import java.time.LocalDate
import java.util.UUID

enum class PetType {
    DOG, CAT, RABBIT, BIRD, OTHER
}

data class Pet(val id: PetId, val type: PetType, val name: String, val birthDate: LocalDate, val owner: PetOwner? = null) {
    init {
        require(name.isNotBlank()) { "name cannot be blank" }
        require(birthDate.isBefore(LocalDate.now().plusDays(1))) { "birthDate cannot be a future date" }
    }
}

data class PetId(private val value: UUID) {
    companion object {
        fun create() = PetId(UUID.randomUUID())

        fun of(uuid: UUID) = PetId(uuid)

        fun of(string: String) = of(UUID.fromString(string))
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PetId

        return value == other.value
    }

    override fun toString() = value.toString()

    override fun hashCode() = value.hashCode()

}