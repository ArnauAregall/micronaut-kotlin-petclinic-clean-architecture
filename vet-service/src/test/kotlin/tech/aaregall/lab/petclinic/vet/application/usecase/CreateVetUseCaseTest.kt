package tech.aaregall.lab.petclinic.vet.application.usecase

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.LIST
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import tech.aaregall.lab.petclinic.vet.application.ports.input.CreateVetCommand
import tech.aaregall.lab.petclinic.vet.application.ports.input.CreateVetCommandException
import tech.aaregall.lab.petclinic.vet.application.ports.output.SpecialityOutputPort
import tech.aaregall.lab.petclinic.vet.application.ports.output.VetOutputPort
import tech.aaregall.lab.petclinic.vet.application.ports.output.VetValidationOutputPort
import tech.aaregall.lab.petclinic.vet.domain.model.Speciality
import tech.aaregall.lab.petclinic.vet.domain.model.SpecialityId
import tech.aaregall.lab.petclinic.vet.domain.model.Vet
import tech.aaregall.lab.petclinic.vet.domain.model.VetId
import java.util.UUID.randomUUID

@ExtendWith(MockKExtension::class)
internal class CreateVetUseCaseTest {

    @MockK
    lateinit var vetOutputPort: VetOutputPort

    @MockK
    lateinit var vetValidationOutputPort: VetValidationOutputPort

    @MockK
    lateinit var specialityOutputPort: SpecialityOutputPort

    @InjectMockKs
    lateinit var useCase: CreateVetUseCase

    @Test
    fun `Should throw a CreateVetCommandException when a Vet with the given Identity ID already exists`() {
        val identityId = randomUUID()

        every { vetOutputPort.loadVet(VetId(identityId)) } answers { Vet(id = VetId(identityId)) }

        assertThatCode { useCase.createVet(CreateVetCommand(identityId = identityId, specialitiesIds = emptySet()))}
            .isInstanceOf(CreateVetCommandException::class.java)
            .hasMessageContaining("Failed to create Vet")
            .hasMessageContaining("Vet with Identity ID '$identityId' already exists")

        verify { vetOutputPort.loadVet(VetId(identityId)) }
        verify (exactly = 0) { vetValidationOutputPort.isValidVetId(VetId(identityId)) }
        verify (exactly = 0) { specialityOutputPort.loadSpeciality(any()) }
        verify (exactly = 0) { vetOutputPort.createVet(any()) }
        verify (exactly = 0) { vetOutputPort.setVetSpecialities(any(), any()) }
    }

    @Test
    fun `Should throw a CreateVetCommandException when Identity ID is not valid`() {
        val identityId = randomUUID()

        every { vetOutputPort.loadVet(VetId(identityId)) } answers { null }
        every { vetValidationOutputPort.isValidVetId(VetId(identityId)) } answers { false }

        assertThatCode { useCase.createVet(CreateVetCommand(identityId = identityId, specialitiesIds = emptySet()))}
            .isInstanceOf(CreateVetCommandException::class.java)
            .hasMessageContaining("Failed to create Vet")
            .hasMessageContaining("Vet ID '$identityId' is not valid")

        verify { vetOutputPort.loadVet(VetId(identityId)) }
        verify { vetValidationOutputPort.isValidVetId(VetId(identityId)) }
        verify (exactly = 0) { specialityOutputPort.loadSpeciality(any()) }
        verify (exactly = 0) { vetOutputPort.createVet(any()) }
        verify (exactly = 0) { vetOutputPort.setVetSpecialities(any(), any()) }
    }

    @Test
    fun `Should throw a CreateVetCommandException when Identity ID is valid and Specialities is empty`() {
        val identityId = randomUUID()

        every { vetOutputPort.loadVet(VetId(identityId)) } answers { null }
        every { vetValidationOutputPort.isValidVetId(any()) } answers { true }

        assertThatCode { useCase.createVet(CreateVetCommand(identityId = identityId, specialitiesIds = emptySet()))}
            .isInstanceOf(CreateVetCommandException::class.java)
            .hasMessageContaining("Failed to create Vet")
            .hasMessageContaining("It must have at least one Speciality")

        verify { vetOutputPort.loadVet(VetId(identityId)) }
        verify { vetValidationOutputPort.isValidVetId(VetId(identityId)) }
        verify (exactly = 0) { specialityOutputPort.loadSpeciality(any()) }
        verify (exactly = 0) { vetOutputPort.createVet(any()) }
        verify (exactly = 0) { vetOutputPort.setVetSpecialities(any(), any()) }
    }

    @Test
    fun `Should throw a CreateVetCommandException when Identity ID is valid and any of the Specialities does not exist`() {
        val identityId = randomUUID()
        val specialityId1 = SpecialityId.create()
        val specialityId2 = SpecialityId.create()

        every { vetOutputPort.loadVet(VetId(identityId)) } answers { null }
        every { vetValidationOutputPort.isValidVetId(any()) } answers { true }
        every { specialityOutputPort.loadSpeciality(specialityId1) } answers {
            Speciality(
                id = args.first() as SpecialityId,
                name = "Mock speciality",
                description = "Mock description"
            )
        }
        every { specialityOutputPort.loadSpeciality(specialityId2) } answers { null }

        assertThatCode { useCase.createVet(CreateVetCommand(identityId = identityId, specialitiesIds = setOf(specialityId1, specialityId2)))}
            .isInstanceOf(CreateVetCommandException::class.java)
            .hasMessageContaining("Failed to create Vet")
            .hasMessageContaining("Speciality '$specialityId2' does not exist")

        verify { vetOutputPort.loadVet(VetId(identityId)) }
        verify { vetValidationOutputPort.isValidVetId(VetId(identityId)) }
        verify { specialityOutputPort.loadSpeciality(specialityId1) }
        verify { specialityOutputPort.loadSpeciality(specialityId2) }
        verify (exactly = 0) { vetOutputPort.createVet(any()) }
        verify (exactly = 0) { vetOutputPort.setVetSpecialities(any(), any()) }
    }

    @Test
    fun `Should return the result of the VetOutputPort when the Vet Identity ID is valid and has at least one Speciality`() {
        val identityId = randomUUID()
        val specialityId1 = SpecialityId.create()
        val specialityId2 = SpecialityId.create()

        every { vetOutputPort.loadVet(VetId(identityId)) } answers { null }
        every { vetValidationOutputPort.isValidVetId(any()) } answers { true }
        every { specialityOutputPort.loadSpeciality(any()) } answers {
            val id = args.first() as SpecialityId
            Speciality(
                id = id,
                name = "Mock speciality $id",
                description = "Mock description for $id"
            )
        }
        every { vetOutputPort.createVet(any()) } answers { args.first() as Vet }
        every { vetOutputPort.setVetSpecialities(any(), any()) } answers {
            Vet(id = (args.first() as Vet).id, specialities = args.last() as Collection<Speciality>)
        }

        val result = useCase.createVet(CreateVetCommand(identityId = identityId, specialitiesIds = setOf(specialityId1, specialityId2)))

        assertThat(result)
            .isNotNull
            .satisfies({ assertThat(it.id).isNotNull })
            .satisfies({
                assertThat(it.specialities)
                    .asInstanceOf(LIST)
                    .isNotEmpty()
                    .hasSize(2)
                    .containsExactly(
                        Speciality(specialityId1, "Mock speciality $specialityId1", "Mock description for $specialityId1"),
                        Speciality(specialityId2, "Mock speciality $specialityId2", "Mock description for $specialityId2")
                    )
            })

        verify { vetOutputPort.loadVet(VetId(identityId)) }
        verify { vetValidationOutputPort.isValidVetId(VetId(identityId)) }
        verify { specialityOutputPort.loadSpeciality(specialityId1) }
        verify { specialityOutputPort.loadSpeciality(specialityId2) }
        verify { vetOutputPort.createVet(any()) }
        verify { vetOutputPort.setVetSpecialities(any(Vet::class), any()) }
    }


}