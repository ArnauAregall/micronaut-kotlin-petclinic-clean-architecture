package tech.aaregall.lab.petclinic.pet.application.usecase

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import tech.aaregall.lab.petclinic.common.reactive.UnitReactive
import tech.aaregall.lab.petclinic.pet.application.ports.output.PetOutputPort
import tech.aaregall.lab.petclinic.pet.application.ports.usecase.CountAllPetsUseCaseImpl
import kotlin.random.Random
import kotlin.random.nextLong

@ExtendWith(MockKExtension::class)
internal class CountAllPetsUseCaseImplTest {

    @MockK
    lateinit var petOutputPort: PetOutputPort

    @InjectMockKs
    lateinit var useCase: CountAllPetsUseCaseImpl

    @Nested
    inner class CountAllPets {

        @Test
        fun `Should return a UnitReactive with the exact same value returned by PetOutputPort`() {
            val fakeCount = Random.nextLong(LongRange(10, 1000))

            every { petOutputPort.countAllPets() } answers { UnitReactive(fakeCount) }

            val result = useCase.countAllPets()

            assertThat(result.block()!!)
                .isEqualTo(fakeCount)
        }

    }

}