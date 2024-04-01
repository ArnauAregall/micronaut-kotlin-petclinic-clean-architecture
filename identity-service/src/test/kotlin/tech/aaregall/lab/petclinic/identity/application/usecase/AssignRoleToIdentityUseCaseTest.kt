package tech.aaregall.lab.petclinic.identity.application.usecase

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import tech.aaregall.lab.petclinic.identity.application.ports.input.AssignRoleToIdentityCommand
import tech.aaregall.lab.petclinic.identity.application.ports.input.AssignRoleToIdentityCommandException
import tech.aaregall.lab.petclinic.identity.application.ports.output.IdentityEventPublisher
import tech.aaregall.lab.petclinic.identity.application.ports.output.IdentityOutputPort
import tech.aaregall.lab.petclinic.identity.application.ports.output.RoleOutputPort
import tech.aaregall.lab.petclinic.identity.application.ports.usecase.AssignRoleToIdentityUseCase
import tech.aaregall.lab.petclinic.identity.domain.event.IdentityUpdatedEvent
import tech.aaregall.lab.petclinic.identity.domain.model.Identity
import tech.aaregall.lab.petclinic.identity.domain.model.IdentityId
import tech.aaregall.lab.petclinic.identity.domain.model.Role
import tech.aaregall.lab.petclinic.identity.domain.model.RoleId

@ExtendWith(MockKExtension::class)
internal class AssignRoleToIdentityUseCaseTest {

    @MockK
    lateinit var identityOutputPort: IdentityOutputPort

    @MockK
    lateinit var roleOutputPort: RoleOutputPort

    @MockK
    lateinit var identityEventPublisher: IdentityEventPublisher

    @InjectMockKs
    lateinit var useCase: AssignRoleToIdentityUseCase

    @Nested
    inner class AssignRoleToIdentity {

        @Test
        fun `Throws IllegalArgumentException when Identity does not exist`() {
            val identityId = IdentityId.create()

            every { identityOutputPort.loadIdentityById(identityId) } answers { null }

            assertThatCode {
                useCase.assignRoleToIdentity(
                    AssignRoleToIdentityCommand(
                        identityId,
                        RoleId.create()
                    )
                )
            }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage("Cannot assign roles to a non existing Identity")

            verify { identityOutputPort.loadIdentityById(identityId) }
            verify (exactly = 0) { roleOutputPort.loadRoleById(any()) }
            verify (exactly = 0) { roleOutputPort.assignRoleToIdentity(any(), any()) }
            verify (exactly = 0) { identityEventPublisher.publishIdentityUpdatedEvent(any()) }
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
                useCase.assignRoleToIdentity(
                    AssignRoleToIdentityCommand(
                        identityId,
                        roleId
                    )
                )
            }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage("Cannot assign a non existing Role to Identity $identityId")

            verify { identityOutputPort.loadIdentityById(identityId) }
            verify { roleOutputPort.loadRoleById(roleId) }
            verify (exactly = 0) { roleOutputPort.assignRoleToIdentity(any(), any()) }
            verify (exactly = 0) { identityEventPublisher.publishIdentityUpdatedEvent(any()) }
        }

        @Test
        fun `Throws AssignRolesToIdentityCommandException when Identity already has the Role assigned`() {
            val identityId = IdentityId.create()
            val roleId = RoleId.create()

            every { identityOutputPort.loadIdentityById(identityId) } answers {
                Identity(id = identityId, firstName = "Foo", lastName = "Bar", roles = listOf(Role(id = roleId, name = "Mage")))
            }

            every { roleOutputPort.loadRoleById(roleId) } answers { Role(id = roleId, name = "Mage") }

            assertThatCode {
                useCase.assignRoleToIdentity(
                    AssignRoleToIdentityCommand(
                        identityId,
                        roleId
                    )
                )
            }
                .isInstanceOf(AssignRoleToIdentityCommandException::class.java)
                .hasMessageContaining("Failed assigning Role to Identity")
                .hasMessageContaining("Identity $identityId already has Role $roleId assigned")

            verify { identityOutputPort.loadIdentityById(identityId) }
            verify { roleOutputPort.loadRoleById(roleId) }
            verify (exactly = 0) { roleOutputPort.assignRoleToIdentity(any(), any()) }
            verify (exactly = 0) { identityEventPublisher.publishIdentityUpdatedEvent(any()) }
        }

        @Test
        fun `Should call RoleOutputPort when both Identity and Role exist and Identity does not have the Role yet`() {
            val identityId = IdentityId.create()
            val roleId = RoleId.create()

            val mockIdentity = Identity(id = identityId, firstName = "Foo", lastName = "Bar", roles = listOf(Role(id = RoleId.create(), name = "Mage")))
            val mockRole = Role(id = roleId, name = "Warrior")

            every { identityOutputPort.loadIdentityById(identityId) } answers { mockIdentity }
            every { roleOutputPort.loadRoleById(roleId) } answers { mockRole  }
            every { roleOutputPort.assignRoleToIdentity(mockIdentity, mockRole) } answers { nothing }
            every { identityEventPublisher.publishIdentityUpdatedEvent(IdentityUpdatedEvent(mockIdentity)) } answers { nothing }

            assertThatCode {
                useCase.assignRoleToIdentity(
                    AssignRoleToIdentityCommand(
                        identityId,
                        roleId
                    )
                )
            }
                .doesNotThrowAnyException()

            verify (exactly = 2) { identityOutputPort.loadIdentityById(identityId) }
            verify { roleOutputPort.loadRoleById(roleId) }
            verify { roleOutputPort.assignRoleToIdentity(mockIdentity, mockRole) }
            verify { identityEventPublisher.publishIdentityUpdatedEvent(IdentityUpdatedEvent(mockIdentity)) }
        }

    }

}