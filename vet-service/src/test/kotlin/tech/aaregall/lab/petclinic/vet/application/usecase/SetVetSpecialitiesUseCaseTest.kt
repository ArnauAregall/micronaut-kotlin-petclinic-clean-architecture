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
import tech.aaregall.lab.petclinic.vet.application.ports.input.SetVetSpecialitiesCommand
import tech.aaregall.lab.petclinic.vet.application.ports.input.SetVetSpecialitiesCommandException
import tech.aaregall.lab.petclinic.vet.application.ports.output.SpecialityOutputPort
import tech.aaregall.lab.petclinic.vet.application.ports.output.VetOutputPort
import tech.aaregall.lab.petclinic.vet.domain.model.Speciality
import tech.aaregall.lab.petclinic.vet.domain.model.SpecialityId
import tech.aaregall.lab.petclinic.vet.domain.model.Vet
import tech.aaregall.lab.petclinic.vet.domain.model.VetId

@ExtendWith(MockKExtension::class)
internal class SetVetSpecialitiesUseCaseTest {

    @MockK
    lateinit var vetOutputPort: VetOutputPort

    @MockK
    lateinit var specialityOutputPort: SpecialityOutputPort

    @InjectMockKs
    lateinit var useCase: SetVetSpecialitiesUseCase

    @Test
    fun `Should throw a SetVetSpecialitiesCommandException when Vet does not exist`() {
        val vetId = VetId.create()

        every { vetOutputPort.loadVet(vetId) } answers { null }

        assertThatCode { useCase.setVetSpecialities(SetVetSpecialitiesCommand(vetId, emptySet()))}
            .isInstanceOf(SetVetSpecialitiesCommandException::class.java)
            .hasMessageContaining("Failed to set Vet Specialities")
            .hasMessageContaining("Cannot set Vet Specialities for a non existing Vet")

        verify { vetOutputPort.loadVet(vetId) }
        verify (exactly = 0) { specialityOutputPort.loadSpeciality(any()) }
        verify (exactly = 0) { specialityOutputPort.setVetSpecialities(any(), any()) }
    }

    @Test
    fun `Should throw a SetVetSpecialitiesCommandException when Vet exists and Specialities are empty`() {
        every { vetOutputPort.loadVet(any()) } answers { Vet(args.first() as VetId, emptySet()) }

        assertThatCode { useCase.setVetSpecialities(SetVetSpecialitiesCommand(VetId.create(), emptySet()))}
            .isInstanceOf(SetVetSpecialitiesCommandException::class.java)
            .hasMessageContaining("Failed to set Vet Specialities")
            .hasMessageContaining("At least one Speciality is required")

        verify { vetOutputPort.loadVet(any()) }
        verify (exactly = 0) { specialityOutputPort.loadSpeciality(any()) }
        verify (exactly = 0) { specialityOutputPort.setVetSpecialities(any(), any()) }
    }

    @Test
    fun `Should throw a SetVetSpecialitiesCommandException when Vet exists and any of the Specialities does not exist`() {
        val vetId = VetId.create()
        val specialityId1 = SpecialityId.create()
        val specialityId2 = SpecialityId.create()

        every { vetOutputPort.loadVet(vetId) } answers { Vet(args.first() as VetId, emptySet()) }
        every { specialityOutputPort.loadSpeciality(specialityId1) } answers { Speciality(id = specialityId1, name = "Mock speciality") }
        every { specialityOutputPort.loadSpeciality(specialityId2) } answers { null }


        assertThatCode { useCase.setVetSpecialities(SetVetSpecialitiesCommand(vetId, setOf(specialityId1, specialityId2)))}
            .isInstanceOf(SetVetSpecialitiesCommandException::class.java)
            .hasMessageContaining("Failed to set Vet Specialities")
            .hasMessageContaining("Speciality '$specialityId2' does not exist")

        verify { vetOutputPort.loadVet(any()) }
        verify (exactly = 2) { specialityOutputPort.loadSpeciality(any()) }
        verify (exactly = 0) { specialityOutputPort.setVetSpecialities(any(), any()) }
    }

    @Test
    fun `Should return the result of VetOutputPort when Vet exists and Specialities are valid`() {
        val vet = Vet(VetId.create(), emptySet())
        val speciality1 = Speciality(id = SpecialityId.create(), name = "Mock speciality 1")
        val speciality2 = Speciality(id = SpecialityId.create(), name = "Mock speciality 2")


        every { vetOutputPort.loadVet(vet.id) } answers { vet }
        every { specialityOutputPort.loadSpeciality(speciality1.id) } answers { speciality1 }
        every { specialityOutputPort.loadSpeciality(speciality2.id) } answers { speciality2 }
        every { specialityOutputPort.setVetSpecialities(vet, any()) } answers {
            Vet(id =(args.first() as Vet).id, specialities = args.last() as Collection<Speciality>)
        }

        val result = useCase.setVetSpecialities(SetVetSpecialitiesCommand(vet.id, setOf(speciality1.id, speciality2.id)))

        assertThat(result)
            .isNotNull
            .satisfies({ assertThat(it.id).isEqualTo(vet.id) })
            .satisfies({
                assertThat(it.specialities)
                    .asInstanceOf(LIST)
                    .isNotEmpty
                    .hasSize(2)
                    .containsExactly(speciality1, speciality2)
            })

        verify { vetOutputPort.loadVet(any()) }
        verify (exactly = 2) { specialityOutputPort.loadSpeciality(any()) }
        verify { specialityOutputPort.setVetSpecialities(any(), any()) }
    }

}