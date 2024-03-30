package tech.aaregall.lab.petclinic.vet.application.usecase

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import tech.aaregall.lab.petclinic.vet.application.ports.output.VetOutputPort

@ExtendWith(MockKExtension::class)
internal class CountAllVetsUseCaseTest {

    @MockK
    lateinit var vetOutputPort: VetOutputPort

    @InjectMockKs
    lateinit var useCase: CountAllVetsUseCase

    @Nested
    inner class CountAll {

        @ParameterizedTest
        @ValueSource(ints = [0, 1, 2, 3, 5, 8, 13])
        fun `It should call VetOutputPort and directly return the returned value`(totalCount: Int) {
            every { vetOutputPort.countAll() } answers { totalCount }

            val result = useCase.countAllVets()

            assertThat(result).isEqualTo(totalCount)

            verify { vetOutputPort.countAll() }
        }

    }

}