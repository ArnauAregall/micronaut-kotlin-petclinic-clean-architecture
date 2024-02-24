package tech.aaregall.lab.petclinic.vet.domain.model

import java.util.UUID

data class Speciality(val id: SpecialityId, val name: String, val description: String? = null)

data class SpecialityId(private val value: UUID) {

    companion object {
        fun create() = SpecialityId(UUID.randomUUID())

        fun of(uuid: UUID) = SpecialityId(uuid)

        fun of(string: String) = of(UUID.fromString(string))

    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SpecialityId

        return value == other.value
    }

    override fun toString(): String = value.toString()

    override fun hashCode(): Int = value.hashCode()
}