package tech.aaregall.lab.petclinic.identity.infrastructure.adapters.output.persistence

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.OneToOne
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
    val firstName: String,

    @NotNull
    @Column(name = "last_name")
    val lastName: String,

    @NotNull
    @Column(name = "created_by")
    internal var createdBy: UUID = SYSTEM_ACCOUNT_AUDIT_ID,

    @OneToOne(mappedBy = "identity", optional = true, cascade = [CascadeType.ALL])
    var contactDetails: ContactDetailsJpaEntity? = null,

    @ManyToMany
    @JoinTable(
        name = "identity_role",
        joinColumns = [JoinColumn(name = "identity_id")],
        inverseJoinColumns = [JoinColumn(name = "role_id")]
    )
    var roles: MutableSet<RoleJpaEntity>? = mutableSetOf()

)