package tech.aaregall.lab.petclinic.identity.application.ports.output

import tech.aaregall.lab.petclinic.identity.domain.model.Identity
import tech.aaregall.lab.petclinic.identity.domain.model.Role
import tech.aaregall.lab.petclinic.identity.domain.model.RoleId

interface RoleOutputPort {

    fun createRole(role: Role): Role

    fun roleExistsByName(name: String): Boolean

    fun loadRoleById(roleId: RoleId): Role?

    fun assignRoleToIdentity(identity: Identity, role: Role)

}