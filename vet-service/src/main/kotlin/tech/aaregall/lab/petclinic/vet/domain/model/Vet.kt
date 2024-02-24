package tech.aaregall.lab.petclinic.vet.domain.model

import java.util.UUID

data class Vet(val id: VetId, val specialities: Collection<Speciality> = emptySet())

data class VetId(private val value: UUID) {

    companion object {

        fun create() = VetId(UUID.randomUUID())

        fun of(uuid: UUID) = VetId(uuid)

        fun of(string: String) = of(UUID.fromString(string))

    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as VetId

        return value == other.value
    }

    override fun toString(): String = value.toString()

    override fun hashCode(): Int = value.hashCode()

}