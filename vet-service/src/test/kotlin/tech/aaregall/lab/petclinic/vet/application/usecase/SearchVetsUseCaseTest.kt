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
import tech.aaregall.lab.petclinic.vet.application.ports.input.SearchVetsCommand
import tech.aaregall.lab.petclinic.vet.application.ports.output.VetOutputPort
import tech.aaregall.lab.petclinic.vet.domain.model.Vet
import tech.aaregall.lab.petclinic.vet.domain.model.VetId
import java.util.stream.Stream

@ExtendWith(MockKExtension::class)
internal class SearchVetsUseCaseTest {

    @MockK
    lateinit var vetOutputPort: VetOutputPort

    @InjectMockKs
    lateinit var useCase: SearchVetsUseCase

    @Nested
    @TestInstance(PER_CLASS)
    inner class SearchVets {

        @Test
        fun `It should return an empty collection when VetOutputPort returns null`() {
            every { vetOutputPort.findVets(any(Int::class), any(Int::class)) } answers { null }

            val result = useCase.searchVets(SearchVetsCommand(0, 20))

            assertThat(result)
                .hasSize(0)
                .isEmpty()
        }

        @MethodSource("findVetsResults")
        @ParameterizedTest(name = INDEX_PLACEHOLDER)
        fun `It should call VetOutputPort with the page arguments and return it's result`(vets: Collection<Vet>) {
            every { vetOutputPort.findVets(any(Int::class), any(Int::class)) } answers { vets }

            val result = useCase.searchVets(SearchVetsCommand(0, 20))

            assertThat(result)
                .hasSize(vets.size)
                .usingRecursiveComparison()
                .isEqualTo(vets)
        }

        private fun findVetsResults(): Stream<Arguments> =
            Stream.of(
                arguments(emptySet<Vet>()),
                arguments(
                    IntRange(start = 1, endInclusive = 19).map { _ ->
                        Vet(id = VetId.create(), specialities = emptySet())
                    }.toSet()
                )
            )
    }

}