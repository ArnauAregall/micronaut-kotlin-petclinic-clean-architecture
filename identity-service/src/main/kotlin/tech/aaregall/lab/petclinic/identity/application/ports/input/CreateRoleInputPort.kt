package tech.aaregall.lab.petclinic.identity.application.ports.input

import tech.aaregall.lab.petclinic.identity.domain.model.Role

fun interface CreateRoleInputPort {

    fun createRole(createRoleCommand: CreateRoleCommand): Role

}

data class CreateRoleCommand(val name: String)

class CreateRoleCommandException(message: String, cause: Throwable? = null) : IllegalArgumentException("Failed to create Role: $message", cause)