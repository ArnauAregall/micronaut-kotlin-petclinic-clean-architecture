package tech.aaregall.lab.petclinic.identity.application.ports.output

import tech.aaregall.lab.petclinic.identity.domain.model.Role
import tech.aaregall.lab.petclinic.identity.domain.model.RoleId

fun interface RoleOutputPort {

    fun loadRoleById(roleId: RoleId): Role?

}