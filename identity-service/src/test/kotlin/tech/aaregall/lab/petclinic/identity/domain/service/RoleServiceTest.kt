package tech.aaregall.lab.petclinic.identity.domain.service

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
import tech.aaregall.lab.petclinic.identity.application.ports.input.AssignRoleToIdentityCommand
import tech.aaregall.lab.petclinic.identity.application.ports.input.AssignRoleToIdentityCommandException
import tech.aaregall.lab.petclinic.identity.application.ports.input.CreateRoleCommand
import tech.aaregall.lab.petclinic.identity.application.ports.input.CreateRoleCommandException
import tech.aaregall.lab.petclinic.identity.application.ports.input.LoadIdentityCommand
import tech.aaregall.lab.petclinic.identity.application.ports.input.LoadIdentityUseCase
import tech.aaregall.lab.petclinic.identity.application.ports.input.RevokeRoleFromIdentityCommand
import tech.aaregall.lab.petclinic.identity.application.ports.input.RevokeRoleFromIdentityCommandException
import tech.aaregall.lab.petclinic.identity.application.ports.output.RoleOutputPort
import tech.aaregall.lab.petclinic.identity.domain.model.Identity
import tech.aaregall.lab.petclinic.identity.domain.model.IdentityId
import tech.aaregall.lab.petclinic.identity.domain.model.Role
import tech.aaregall.lab.petclinic.identity.domain.model.RoleId

@ExtendWith(MockKExtension::class)
internal class RoleServiceTest {

    @MockK
    lateinit var loadIdentityUseCase: LoadIdentityUseCase

    @MockK
    lateinit var roleOutputPort: RoleOutputPort

    @InjectMockKs
    lateinit var roleService: RoleService

    @Nested
    inner class CreateRole {

        @Test
        fun `Throws CreateRoleCommandException when Role with the same name already exists`() {
            val roleName = "Mage"

            every { roleOutputPort.roleExistsByName(roleName) } answers { true }

            assertThatCode { roleService.createRole(CreateRoleCommand(roleName))}
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

            val result = roleService.createRole(CreateRoleCommand(roleName))

            assertThat(result)
                .isNotNull
                .satisfies({ assertThat(it.id).isNotNull })
                .extracting(Role::name)
                .isEqualTo(roleName)

            verify { roleOutputPort.roleExistsByName(roleName) }
            verify { roleOutputPort.createRole(Role(result.id, roleName)) }
        }

    }

    @Nested
    inner class AssignRoleToIdentity {

        @Test
        fun `Throws IllegalArgumentException when Identity does not exist`() {
            val identityId = IdentityId.create()

            every { loadIdentityUseCase.loadIdentity(LoadIdentityCommand(identityId)) } answers { null }

            assertThatCode { roleService.assignRoleToIdentity(AssignRoleToIdentityCommand(identityId, RoleId.create()))}
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage("Cannot assign roles to a non existing Identity")

            verify { loadIdentityUseCase.loadIdentity(LoadIdentityCommand(identityId)) }
            verify (exactly = 0) { roleOutputPort.loadRoleById(any()) }
            verify (exactly = 0) { roleOutputPort.assignRoleToIdentity(any(), any()) }
        }

        @Test
        fun `Throws IllegalArgumentException when Role does not exist`() {
            val identityId = IdentityId.create()
            val roleId = RoleId.create()

            every { loadIdentityUseCase.loadIdentity(LoadIdentityCommand(identityId)) } answers {
                Identity(id = identityId, firstName = "Foo", lastName = "Bar")
            }

            every { roleOutputPort.loadRoleById(roleId) } answers { null }

            assertThatCode { roleService.assignRoleToIdentity(AssignRoleToIdentityCommand(identityId, roleId))}
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage("Cannot assign a non existing Role to Identity $identityId")

            verify { loadIdentityUseCase.loadIdentity(LoadIdentityCommand(identityId)) }
            verify { roleOutputPort.loadRoleById(roleId) }
            verify (exactly = 0) { roleOutputPort.assignRoleToIdentity(any(), any()) }
        }

        @Test
        fun `Throws AssignRolesToIdentityCommandException when Identity already has the Role assigned`() {
            val identityId = IdentityId.create()
            val roleId = RoleId.create()

            every { loadIdentityUseCase.loadIdentity(LoadIdentityCommand(identityId)) } answers {
                Identity(id = identityId, firstName = "Foo", lastName = "Bar", roles = listOf(Role(id = roleId, name = "Mage")))
            }

            every { roleOutputPort.loadRoleById(roleId) } answers { Role(id = roleId, name = "Mage") }

            assertThatCode { roleService.assignRoleToIdentity(AssignRoleToIdentityCommand(identityId, roleId))}
                .isInstanceOf(AssignRoleToIdentityCommandException::class.java)
                .hasMessageContaining("Failed assigning Role to Identity")
                .hasMessageContaining("Identity $identityId already has Role $roleId assigned")

            verify { loadIdentityUseCase.loadIdentity(LoadIdentityCommand(identityId)) }
            verify { roleOutputPort.loadRoleById(roleId) }
            verify (exactly = 0) { roleOutputPort.assignRoleToIdentity(any(), any()) }
        }

        @Test
        fun `Should call RoleOutputPort when both Identity and Role exist and Identity does not have the Role yet`() {
            val identityId = IdentityId.create()
            val roleId = RoleId.create()

            val mockIdentity = Identity(id = identityId, firstName = "Foo", lastName = "Bar", roles = listOf(Role(id = RoleId.create(), name = "Mage")))
            val mockRole = Role(id = roleId, name = "Warrior")

            every { loadIdentityUseCase.loadIdentity(LoadIdentityCommand(identityId)) } answers { mockIdentity }
            every { roleOutputPort.loadRoleById(roleId) } answers { mockRole  }
            every { roleOutputPort.assignRoleToIdentity(mockIdentity, mockRole) } answers { nothing }

            assertThatCode { roleService.assignRoleToIdentity(AssignRoleToIdentityCommand(identityId, roleId))}
                .doesNotThrowAnyException()

            verify { loadIdentityUseCase.loadIdentity(LoadIdentityCommand(identityId)) }
            verify { roleOutputPort.loadRoleById(roleId) }
            verify { roleOutputPort.assignRoleToIdentity(mockIdentity, mockRole) }
        }

    }

    @Nested
    inner class RevokeRoleFromIdentity {

        @Test
        fun `Throws IllegalArgumentException when Identity does not exist`() {
            val identityId = IdentityId.create()

            every { loadIdentityUseCase.loadIdentity(LoadIdentityCommand(identityId)) } answers { null }

            assertThatCode { roleService.revokeRoleFromIdentity(RevokeRoleFromIdentityCommand(identityId, RoleId.create()))}
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage("Cannot revoke a Role from a non existing Identity")

            verify { loadIdentityUseCase.loadIdentity(LoadIdentityCommand(identityId)) }
            verify (exactly = 0) { roleOutputPort.loadRoleById(any()) }
            verify (exactly = 0) { roleOutputPort.revokeRoleFromIdentity(any(), any()) }
        }

        @Test
        fun `Throws IllegalArgumentException when Role does not exist`() {
            val identityId = IdentityId.create()
            val roleId = RoleId.create()

            every { loadIdentityUseCase.loadIdentity(LoadIdentityCommand(identityId)) } answers {
                Identity(id = identityId, firstName = "Foo", lastName = "Bar")
            }

            every { roleOutputPort.loadRoleById(roleId) } answers { null }

            assertThatCode { roleService.revokeRoleFromIdentity(RevokeRoleFromIdentityCommand(identityId, roleId))}
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage("Cannot revoke a non existing Role from Identity $identityId")

            verify { loadIdentityUseCase.loadIdentity(LoadIdentityCommand(identityId)) }
            verify { roleOutputPort.loadRoleById(roleId) }
            verify (exactly = 0) { roleOutputPort.revokeRoleFromIdentity(any(), any()) }
        }

        @Test
        fun `Throws RevokeRoleFromIdentityCommandException when Identity does not have the Role assigned`() {
            val identityId = IdentityId.create()
            val roleId = RoleId.create()

            every { loadIdentityUseCase.loadIdentity(LoadIdentityCommand(identityId)) } answers {
                Identity(id = identityId, firstName = "Foo", lastName = "Bar", roles = listOf(Role(id = RoleId.create(), name = "Paladin")))
            }

            every { roleOutputPort.loadRoleById(roleId) } answers { Role(id = roleId, name = "Mage") }

            assertThatCode { roleService.revokeRoleFromIdentity(RevokeRoleFromIdentityCommand(identityId, roleId))}
                .isInstanceOf(RevokeRoleFromIdentityCommandException::class.java)
                .hasMessageContaining("Failed revoking Role from Identity")
                .hasMessageContaining("Identity $identityId does not have the Role $roleId assigned")

            verify { loadIdentityUseCase.loadIdentity(LoadIdentityCommand(identityId)) }
            verify { roleOutputPort.loadRoleById(roleId) }
            verify (exactly = 0) { roleOutputPort.revokeRoleFromIdentity(any(), any()) }
        }

        @Test
        fun `Should call RoleOutputPort when both Identity and Role exist and Identity has the Role assigned`() {
            val identityId = IdentityId.create()
            val roleId = RoleId.create()

            val mockRole = Role(id = roleId, name = "Warrior")
            val mockIdentity = Identity(id = identityId, firstName = "Foo", lastName = "Bar", roles = listOf(mockRole))

            every { loadIdentityUseCase.loadIdentity(LoadIdentityCommand(identityId)) } answers { mockIdentity }
            every { roleOutputPort.loadRoleById(roleId) } answers { mockRole  }
            every { roleOutputPort.revokeRoleFromIdentity(mockIdentity, mockRole) } answers { nothing }

            assertThatCode { roleService.revokeRoleFromIdentity(RevokeRoleFromIdentityCommand(identityId, roleId))}
                .doesNotThrowAnyException()

            verify { loadIdentityUseCase.loadIdentity(LoadIdentityCommand(identityId)) }
            verify { roleOutputPort.loadRoleById(roleId) }
            verify { roleOutputPort.revokeRoleFromIdentity(mockIdentity, mockRole) }
        }

    }

}