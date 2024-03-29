package tech.aaregall.lab.petclinic.identity.infrastructure.adapters.output.persistence

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import tech.aaregall.lab.petclinic.identity.domain.model.Identity
import tech.aaregall.lab.petclinic.identity.domain.model.Role
import java.io.Serializable
import java.util.UUID

@Embeddable
internal data class IdentityRoleId(

    @Column(name = "identity_id") val identityId: UUID,

    @Column(name = "role_id") val roleId: UUID

): Serializable {

    constructor(identity: Identity, role: Role) :
            this(identityId = UUID.fromString(identity.id.toString()), roleId = UUID.fromString(role.id.toString()))

}

@Entity
@Table(name = "identity_role")
internal class IdentityRoleJpaEntity(

    @Id
    val identityRoleId: IdentityRoleId
)