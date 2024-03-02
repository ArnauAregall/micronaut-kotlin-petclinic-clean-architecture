package tech.aaregall.lab.petclinic.vet.domain.application.usecase

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
import tech.aaregall.lab.petclinic.vet.application.ports.input.CreateSpecialityCommand
import tech.aaregall.lab.petclinic.vet.application.ports.input.CreateSpecialityCommandException
import tech.aaregall.lab.petclinic.vet.application.ports.output.SpecialityOutputPort
import tech.aaregall.lab.petclinic.vet.application.usecase.CreateSpecialityUseCase
import tech.aaregall.lab.petclinic.vet.domain.model.Speciality

@ExtendWith(MockKExtension::class)
internal class CreateSpecialityUseCaseTest {

    @MockK
    lateinit var specialityOutputPort: SpecialityOutputPort

    @InjectMockKs
    lateinit var createSpecialityUseCase: CreateSpecialityUseCase

    @Nested
    inner class CreateSpeciality {

        @Test
        fun `It should throw a CreateSpecialityCommandException when a Speciality with the given name already exists`() {
            val name = "Anesthesia"

            every { specialityOutputPort.specialityExistsByName(name) } answers { true }

            assertThatCode { createSpecialityUseCase.createSpeciality(CreateSpecialityCommand(name)) }
                .isInstanceOf(CreateSpecialityCommandException::class.java)
                .hasMessageContaining("Failed to create Speciality")
                .hasMessageContaining("Speciality with name '$name' already exists")

            verify { specialityOutputPort.specialityExistsByName(name) }
            verify (exactly = 0) { specialityOutputPort.createSpeciality(any()) }
        }

        @Test
        fun `It should return the Speciality returned by the output port when the given name does not exist, without description`() {
            val name = "Surgery"

            every { specialityOutputPort.specialityExistsByName(name) } answers { false }
            every { specialityOutputPort.createSpeciality(any()) } answers { args.first() as Speciality }

            val result = createSpecialityUseCase.createSpeciality(CreateSpecialityCommand(name))

            assertThat(result)
                .isNotNull
                .satisfies(
                    { assertThat(it.id).isNotNull },
                    { assertThat(it.name).isNotNull.isEqualTo(name) },
                    { assertThat(it.description).isNull() }
                )

            verify { specialityOutputPort.specialityExistsByName(name) }
            verify { specialityOutputPort.createSpeciality(result) }
        }

        @Test
        fun `It should return the Speciality returned by the output port when the given name does not exist, with description`() {
            val name = "Virology"
            val description = "study of viruses and virus-like agents, including, but not limited to, their taxonomy, disease-producing properties, cultivation, and genetics"

            every { specialityOutputPort.specialityExistsByName(name) } answers { false }
            every { specialityOutputPort.createSpeciality(any()) } answers { args.first() as Speciality }

            val result = createSpecialityUseCase.createSpeciality(CreateSpecialityCommand(name, description))

            assertThat(result)
                .isNotNull
                .satisfies(
                    { assertThat(it.id).isNotNull },
                    { assertThat(it.name).isNotNull.isEqualTo(name) },
                    { assertThat(it.description).isNotNull.isEqualTo(description) }
                )

            verify { specialityOutputPort.specialityExistsByName(name) }
            verify { specialityOutputPort.createSpeciality(result) }
        }

    }

}