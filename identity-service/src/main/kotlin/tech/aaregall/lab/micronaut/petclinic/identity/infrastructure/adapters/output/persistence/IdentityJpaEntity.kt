package tech.aaregall.lab.micronaut.petclinic.identity.infrastructure.adapters.output.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.jetbrains.annotations.NotNull
import java.util.UUID

@Entity
@Table(name = "identity")
internal class IdentityJpaEntity() {

    constructor(firstName: String, lastName: String) : this() {
        this.firstName = firstName
        this.lastName = lastName
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null

    @NotNull
    @Column(name = "first_name")
    lateinit var firstName: String

    @NotNull
    @Column(name = "last_name")
    lateinit var lastName: String
}