package tech.aaregall.lab.petclinic.vet.application.usecase

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.ParameterizedTest.INDEX_PLACEHOLDER
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import tech.aaregall.lab.petclinic.vet.application.ports.input.SearchSpecialitiesCommand
import tech.aaregall.lab.petclinic.vet.application.ports.output.SpecialityOutputPort
import tech.aaregall.lab.petclinic.vet.domain.model.Speciality
import tech.aaregall.lab.petclinic.vet.domain.model.SpecialityId
import java.util.stream.Stream

@ExtendWith(MockKExtension::class)
internal class SearchSpecialitiesUseCaseTest {

    @MockK
    lateinit var specialityOutputPort: SpecialityOutputPort

    @InjectMockKs
    lateinit var useCase: SearchSpecialitiesUseCase

    @Nested
    @TestInstance(PER_CLASS)
    inner class SearchSpecialities {

        @Test
        fun `It should return an empty collection when SpecialityOutputPort returns null`() {
            every { specialityOutputPort.findSpecialities(any(Int::class), any(Int::class)) } answers { null }

            val result = useCase.searchSpecialities(SearchSpecialitiesCommand(0, 20))

            assertThat(result)
                .hasSize(0)
                .isEmpty()
        }

        @ParameterizedTest(name = INDEX_PLACEHOLDER)
        @MethodSource("findSpecialitiesResults")
        fun `It should call SpecialityOutputPort with the page arguments and return it's result`(specialities: Set<Speciality>) {
            every { specialityOutputPort.findSpecialities(any(Int::class), any(Int::class)) } answers { specialities }

            val result = useCase.searchSpecialities(SearchSpecialitiesCommand(0, 20))

            assertThat(result)
                .hasSize(specialities.size)
                .usingRecursiveComparison()
                .isEqualTo(specialities)
        }

        private fun findSpecialitiesResults(): Stream<Arguments> =
            Stream.of(
                arguments(emptySet<Speciality>()),
                arguments(
                    IntRange(start = 1, endInclusive = 50).map { index ->
                        Speciality(id = SpecialityId.create(), name = "Speciality $index")
                    }.toSet()
                )
            )

    }

}