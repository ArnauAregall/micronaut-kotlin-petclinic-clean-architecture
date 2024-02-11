package tech.aaregall.lab.petclinic.identity.infrastructure.adapters.output.persistence

import io.micronaut.configuration.hibernate.jpa.proxy.GenerateProxy
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import java.util.UUID

@Entity
@Table(name = "role")
@GenerateProxy
internal class RoleJpaEntity(

    @Id
    val id: UUID,

    @NotNull
    @Column(name = "name")
    val name: String,

    @NotNull
    @Column(name = "created_by")
    internal var createdBy: UUID = SYSTEM_ACCOUNT_AUDIT_ID

)