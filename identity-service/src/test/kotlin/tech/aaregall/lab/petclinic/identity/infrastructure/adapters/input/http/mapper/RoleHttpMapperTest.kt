package tech.aaregall.lab.petclinic.identity.infrastructure.adapters.input.http.mapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import tech.aaregall.lab.petclinic.identity.application.ports.input.CreateRoleCommand
import tech.aaregall.lab.petclinic.identity.domain.model.Role
import tech.aaregall.lab.petclinic.identity.domain.model.RoleId
import tech.aaregall.lab.petclinic.identity.infrastructure.adapters.input.http.dto.request.CreateRoleRequest
import tech.aaregall.lab.petclinic.identity.infrastructure.adapters.input.http.dto.response.RoleResponse

internal class RoleHttpMapperTest {

    private val roleHttpMapper = RoleHttpMapper()

    @Test
    fun `Maps create request to create command`() {
        val createRoleRequest = CreateRoleRequest(name = "Mock Role")

        val result = roleHttpMapper.mapCreateRequestToCommand(createRoleRequest)

        assertThat(result)
            .isNotNull
            .extracting(CreateRoleCommand::name)
            .isEqualTo(createRoleRequest.name)
    }

    @Test
    fun `Maps domain to response`() {
        val domain = Role(id = RoleId.create(), name = "Mock Role")

        val result = roleHttpMapper.mapToResponse(domain)

        assertThat(result)
            .isNotNull
            .extracting(RoleResponse::id, RoleResponse::name)
            .containsExactly(domain.id.toString(), domain.name)
    }

}