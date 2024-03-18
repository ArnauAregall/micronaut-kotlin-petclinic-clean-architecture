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
import tech.aaregall.lab.petclinic.vet.application.ports.output.SpecialityOutputPort

@ExtendWith(MockKExtension::class)
internal class CountAllSpecialitiesUseCaseTest {

    @MockK
    lateinit var specialityOutputPort: SpecialityOutputPort

    @InjectMockKs
    lateinit var useCase: CountAllSpecialitiesUseCase

    @Nested
    inner class CountAll {

        @ParameterizedTest
        @ValueSource(ints = [0, 1, 2, 3, 5, 8, 13])
        fun `It should call SpecialityOutputPort and directly return the returned value`(totalCount: Int) {
            every { specialityOutputPort.countAll() } answers { totalCount }

            val result = useCase.countAllSpecialities()

            assertThat(result).isEqualTo(totalCount)

            verify { specialityOutputPort.countAll() }
        }

    }

}