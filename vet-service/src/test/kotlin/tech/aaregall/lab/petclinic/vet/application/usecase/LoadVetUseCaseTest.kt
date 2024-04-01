package tech.aaregall.lab.petclinic.vet.application.usecase

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import tech.aaregall.lab.petclinic.vet.application.ports.output.VetOutputPort
import tech.aaregall.lab.petclinic.vet.domain.model.Speciality
import tech.aaregall.lab.petclinic.vet.domain.model.SpecialityId
import tech.aaregall.lab.petclinic.vet.domain.model.Vet
import tech.aaregall.lab.petclinic.vet.domain.model.VetId
import java.util.stream.Stream

@ExtendWith(MockKExtension::class)
internal class LoadVetUseCaseTest {

    @MockK
    lateinit var vetOutputPort: VetOutputPort

    @InjectMockKs
    lateinit var useCase: LoadVetUseCase

    @Nested
    @TestInstance(PER_CLASS)
    inner class LoadVet {

        @ParameterizedTest
        @MethodSource("vetArguments")
        fun `Should return VetOutputPort and directly return the returned value`(vet: Vet?) {
            val vetId = VetId.create()

            every { vetOutputPort.loadVet(vetId) } answers { vet }

            val result = useCase.loadVet(vetId)

            assertThat(result).isEqualTo(vet)

            verify { vetOutputPort.loadVet(vetId) }
        }

        private fun vetArguments(): Stream<Arguments> =
            Stream.of(
                arguments(null),
                arguments(Vet(id = VetId.create())),
                arguments(Vet(id = VetId.create(), specialities = setOf(Speciality(id = SpecialityId.create(), name = "Foo"))))
            )
    }

}