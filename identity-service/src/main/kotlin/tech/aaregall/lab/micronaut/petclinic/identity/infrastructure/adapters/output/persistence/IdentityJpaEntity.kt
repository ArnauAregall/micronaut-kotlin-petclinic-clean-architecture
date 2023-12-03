package tech.aaregall.lab.micronaut.petclinic.identity.infrastructure.adapters.output.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import java.util.UUID

@Entity
@Table(name = "identity")
internal class IdentityJpaEntity(

    @Id
    val id: UUID,

    @NotNull
    @Column(name = "first_name")
    var firstName: String,

    @NotNull
    @Column(name = "last_name")
    var lastName: String,

    @NotNull
    @Column(name = "created_by")
    var createdBy: UUID = SYSTEM_ACCOUNT_AUDIT_ID

)