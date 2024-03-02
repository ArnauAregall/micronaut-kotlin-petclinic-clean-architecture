package tech.aaregall.lab.petclinic.vet.domain.application.usecase

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
import tech.aaregall.lab.petclinic.vet.application.usecase.CreateVetUseCase
import tech.aaregall.lab.petclinic.vet.domain.model.Speciality
import tech.aaregall.lab.petclinic.vet.domain.model.SpecialityId
import tech.aaregall.lab.petclinic.vet.domain.model.Vet

@ExtendWith(MockKExtension::class)
internal class CreateVetUseCaseTest {

    @MockK
    lateinit var specialityOutputPort: SpecialityOutputPort

    @MockK
    lateinit var vetOutputPort: VetOutputPort

    @InjectMockKs
    lateinit var useCase: CreateVetUseCase

    @Test
    fun `Should throw a CreateVetCommandException when Specialities is empty`() {
        assertThatCode { useCase.createVet(CreateVetCommand(emptySet()))}
            .isInstanceOf(CreateVetCommandException::class.java)
            .hasMessageContaining("Failed to create Vet")
            .hasMessageContaining("It must have at least one Speciality")

        verify (exactly = 0) { specialityOutputPort.loadSpeciality(any()) }
        verify (exactly = 0) { vetOutputPort.createVet(any()) }
    }

    @Test
    fun `Should throw a CreateVetCommandException when any of the Specialities does not exist`() {
        val specialityId1 = SpecialityId.create()
        val specialityId2 = SpecialityId.create()

        every { specialityOutputPort.loadSpeciality(specialityId1) } answers {
            Speciality(
                id = args.first() as SpecialityId,
                name = "Mock speciality",
                description = "Mock description"
            )
        }
        every { specialityOutputPort.loadSpeciality(specialityId2) } answers { null }

        assertThatCode { useCase.createVet(CreateVetCommand(setOf(specialityId1, specialityId2)))}
            .isInstanceOf(CreateVetCommandException::class.java)
            .hasMessageContaining("Failed to create Vet")
            .hasMessageContaining("Speciality '$specialityId2' does not exist")

        verify { specialityOutputPort.loadSpeciality(specialityId1) }
        verify { specialityOutputPort.loadSpeciality(specialityId2) }
        verify (exactly = 0) { vetOutputPort.createVet(any()) }
    }

    @Test
    fun `Should return the result of the VetOutputPort when the Vet has at least one Speciality`() {
        val specialityId1 = SpecialityId.create()
        val specialityId2 = SpecialityId.create()

        every { specialityOutputPort.loadSpeciality(any()) } answers {
            val id = args.first() as SpecialityId
            Speciality(
                id = id,
                name = "Mock speciality $id",
                description = "Mock description for $id"
            )
        }

        every { vetOutputPort.createVet(any()) } answers { args.first() as Vet }

        val vet = useCase.createVet(CreateVetCommand(setOf(specialityId1, specialityId2)))

        assertThat(vet)
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

        verify { specialityOutputPort.loadSpeciality(specialityId1) }
        verify { specialityOutputPort.loadSpeciality(specialityId2) }
        verify { vetOutputPort.createVet(any()) }
    }


}