package tech.aaregall.lab.petclinic.identity.domain.usecase

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import tech.aaregall.lab.petclinic.identity.application.ports.input.RevokeRoleFromIdentityCommand
import tech.aaregall.lab.petclinic.identity.application.ports.input.RevokeRoleFromIdentityCommandException
import tech.aaregall.lab.petclinic.identity.application.ports.output.IdentityOutputPort
import tech.aaregall.lab.petclinic.identity.application.ports.output.RoleOutputPort
import tech.aaregall.lab.petclinic.identity.domain.model.Identity
import tech.aaregall.lab.petclinic.identity.domain.model.IdentityId
import tech.aaregall.lab.petclinic.identity.domain.model.Role
import tech.aaregall.lab.petclinic.identity.domain.model.RoleId

@ExtendWith(MockKExtension::class)
internal class RevokeRoleFromIdentityUseCaseImplTest {

    @MockK
    lateinit var identityOutputPort: IdentityOutputPort

    @MockK
    lateinit var roleOutputPort: RoleOutputPort

    @InjectMockKs
    lateinit var useCase: RevokeRoleFromIdentityUseCaseImpl

    @Nested
    inner class RevokeRoleFromIdentity {

        @Test
        fun `Throws IllegalArgumentException when Identity does not exist`() {
            val identityId = IdentityId.create()

            every { identityOutputPort.loadIdentityById(identityId) } answers { null }

            assertThatCode {
                useCase.revokeRoleFromIdentity(
                    RevokeRoleFromIdentityCommand(
                        identityId,
                        RoleId.create()
                    )
                )
            }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage("Cannot revoke a Role from a non existing Identity")

            verify { identityOutputPort.loadIdentityById(identityId) }
            verify (exactly = 0) { roleOutputPort.loadRoleById(any()) }
            verify (exactly = 0) { roleOutputPort.revokeRoleFromIdentity(any(), any()) }
        }

        @Test
        fun `Throws IllegalArgumentException when Role does not exist`() {
            val identityId = IdentityId.create()
            val roleId = RoleId.create()

            every { identityOutputPort.loadIdentityById(identityId) } answers {
                Identity(id = identityId, firstName = "Foo", lastName = "Bar")
            }

            every { roleOutputPort.loadRoleById(roleId) } answers { null }

            assertThatCode {
                useCase.revokeRoleFromIdentity(
                    RevokeRoleFromIdentityCommand(
                        identityId,
                        roleId
                    )
                )
            }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage("Cannot revoke a non existing Role from Identity $identityId")

            verify { identityOutputPort.loadIdentityById(identityId) }
            verify { roleOutputPort.loadRoleById(roleId) }
            verify (exactly = 0) { roleOutputPort.revokeRoleFromIdentity(any(), any()) }
        }

        @Test
        fun `Throws RevokeRoleFromIdentityCommandException when Identity does not have the Role assigned`() {
            val identityId = IdentityId.create()
            val roleId = RoleId.create()

            every { identityOutputPort.loadIdentityById(identityId) } answers {
                Identity(id = identityId, firstName = "Foo", lastName = "Bar", roles = listOf(Role(id = RoleId.create(), name = "Paladin")))
            }

            every { roleOutputPort.loadRoleById(roleId) } answers { Role(id = roleId, name = "Mage") }

            assertThatCode {
                useCase.revokeRoleFromIdentity(
                    RevokeRoleFromIdentityCommand(
                        identityId,
                        roleId
                    )
                )
            }
                .isInstanceOf(RevokeRoleFromIdentityCommandException::class.java)
                .hasMessageContaining("Failed revoking Role from Identity")
                .hasMessageContaining("Identity $identityId does not have the Role $roleId assigned")

            verify { identityOutputPort.loadIdentityById(identityId) }
            verify { roleOutputPort.loadRoleById(roleId) }
            verify (exactly = 0) { roleOutputPort.revokeRoleFromIdentity(any(), any()) }
        }

        @Test
        fun `Should call RoleOutputPort when both Identity and Role exist and Identity has the Role assigned`() {
            val identityId = IdentityId.create()
            val roleId = RoleId.create()

            val mockRole = Role(id = roleId, name = "Warrior")
            val mockIdentity = Identity(id = identityId, firstName = "Foo", lastName = "Bar", roles = listOf(mockRole))

            every { identityOutputPort.loadIdentityById(identityId)} answers { mockIdentity }
            every { roleOutputPort.loadRoleById(roleId) } answers { mockRole  }
            every { roleOutputPort.revokeRoleFromIdentity(mockIdentity, mockRole) } answers { nothing }

            assertThatCode {
                useCase.revokeRoleFromIdentity(
                    RevokeRoleFromIdentityCommand(
                        identityId,
                        roleId
                    )
                )
            }
                .doesNotThrowAnyException()

            verify { identityOutputPort.loadIdentityById(identityId) }
            verify { roleOutputPort.loadRoleById(roleId) }
            verify { roleOutputPort.revokeRoleFromIdentity(mockIdentity, mockRole) }
        }

    }

}