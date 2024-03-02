package tech.aaregall.lab.petclinic.identity.application.usecase

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import tech.aaregall.lab.petclinic.identity.application.ports.input.CreateRoleCommand
import tech.aaregall.lab.petclinic.identity.application.ports.input.CreateRoleCommandException
import tech.aaregall.lab.petclinic.identity.application.ports.output.RoleOutputPort
import tech.aaregall.lab.petclinic.identity.application.ports.usecase.CreateRoleUseCase
import tech.aaregall.lab.petclinic.identity.domain.model.Role

@ExtendWith(MockKExtension::class)
internal class CreateRoleUseCaseTest {

    @MockK
    lateinit var roleOutputPort: RoleOutputPort

    @InjectMockKs
    lateinit var useCase: CreateRoleUseCase

    @Nested
    inner class CreateRole {

        @Test
        fun `Throws CreateRoleCommandException when Role with the same name already exists`() {
            val roleName = "Mage"

            every { roleOutputPort.roleExistsByName(roleName) } answers { true }

            assertThatCode { useCase.createRole(CreateRoleCommand(roleName)) }
                .isInstanceOf(CreateRoleCommandException::class.java)
                .hasMessageContaining("Failed to create Role")
                .hasMessageContaining("Role with name '$roleName' already exists")

            verify { roleOutputPort.roleExistsByName(roleName) }
            verify (exactly = 0) { roleOutputPort.createRole(any()) }
        }

        @Test
        fun `Should call RoleOutputPort and return the new Role when no other Role with the same name exists yet`() {
            val roleName = "Mage"

            every { roleOutputPort.roleExistsByName(roleName) } answers { false }
            every { roleOutputPort.createRole(any()) } answers { args.first() as Role }

            val result = useCase.createRole(CreateRoleCommand(roleName))

            assertThat(result)
                .isNotNull
                .satisfies({ assertThat(it.id).isNotNull })
                .extracting(Role::name)
                .isEqualTo(roleName)

            verify { roleOutputPort.roleExistsByName(roleName) }
            verify { roleOutputPort.createRole(Role(result.id, roleName)) }
        }

    }

}