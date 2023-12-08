package tech.aaregall.lab.micronaut.petclinic.identity.infrastructure.adapters.output.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import java.util.UUID

@Entity
@Table(name = "contact_details")
internal class ContactDetailsJpaEntity(

    @Id
    val identityId: UUID,

    @NotNull
    @Column(name = "email")
    var email: String,

    @NotNull
    @Column(name = "phone_number")
    var phoneNumber: String,

    @NotNull
    @Column(name = "created_by")
    internal var createdBy: UUID = SYSTEM_ACCOUNT_AUDIT_ID

)