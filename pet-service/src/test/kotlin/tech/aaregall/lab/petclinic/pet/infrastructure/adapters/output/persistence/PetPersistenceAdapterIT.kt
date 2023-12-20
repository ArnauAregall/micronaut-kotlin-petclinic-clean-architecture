package tech.aaregall.lab.petclinic.pet.infrastructure.adapters.output.persistence

import io.micronaut.data.r2dbc.operations.R2dbcOperations
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.micronaut.test.extensions.testresources.annotation.TestResourcesProperties
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono
import tech.aaregall.lab.petclinic.pet.domain.model.Pet
import tech.aaregall.lab.petclinic.pet.domain.model.PetId
import tech.aaregall.lab.petclinic.pet.domain.model.PetType.DOG
import tech.aaregall.lab.petclinic.pet.spec.R2dbcFlywaySpec
import java.time.LocalDate

@MicronautTest(transactional = false)
@TestResourcesProperties(providers = [R2dbcFlywaySpec::class])
internal class PetPersistenceAdapterIT(
    private val petPersistenceAdapter: PetPersistenceAdapter,
    private val r2dbc: R2dbcOperations) {

    @AfterEach
    fun tearDown() {
        Mono.from(r2dbc.withTransaction { status ->
            status.connection.createStatement("truncate table pet").execute()
        }).block()
    }

    @Nested
    inner class CreatePet {

        @Test
        fun `It should create a Pet`() {
            val petId = PetId.create()

            petPersistenceAdapter.createPet(
                Pet(id = petId, type = DOG, name = "Bimo", birthDate = LocalDate.now(), owner = null)
            ).toMono().block()

            val count: Long = Mono.from(r2dbc.withTransaction { status ->
                Mono.from(status.connection.createStatement("select count(*) from pet where id = '$petId'").execute())
                    .flatMap {result ->
                        Mono.from(result.map { row -> row.get(0) })
                    }
            }).block() as Long

            assertThat(count).isEqualTo(1)
        }

    }


}