package tech.aaregall.lab.petclinic.pet.infrastructure.adapters.output.persistence

import io.micronaut.data.r2dbc.operations.R2dbcOperations
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.reactivestreams.Publisher
import reactor.core.publisher.Mono
import tech.aaregall.lab.petclinic.pet.domain.model.Pet
import tech.aaregall.lab.petclinic.pet.domain.model.PetId
import tech.aaregall.lab.petclinic.pet.domain.model.PetOwner
import tech.aaregall.lab.petclinic.pet.domain.model.PetType.BIRD
import tech.aaregall.lab.petclinic.pet.domain.model.PetType.DOG
import java.time.LocalDate
import java.util.UUID

@MicronautTest(transactional = false)
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
        fun `It should create a Pet without PetOwner`() {
            val petId = PetId.create()

            petPersistenceAdapter.createPet(
                Pet(id = petId, type = DOG, name = "Bimo", birthDate = LocalDate.now(), owner = null)
            ).toMono().block()

            val countQueryPublisher: Publisher<Long> = r2dbc.withTransaction { status ->
                Mono.from(status.connection.createStatement("select count(*) from pet where id = '$petId'").execute())
                    .flatMap { result ->
                        Mono.from(result.map { row -> row.get(0) as Long })
                    }
            }

            val count: Long? = Mono.from(countQueryPublisher).block()

            assertThat(count).isEqualTo(1)
        }

        @Test
        fun `It should create a Pet with PetOwner`() {
            val petId = PetId.create()
            val petOwner = PetOwner(UUID.randomUUID())

            petPersistenceAdapter.createPet(
                Pet(id = petId, type = BIRD, name = "Marujito", birthDate = LocalDate.now(), owner = petOwner)
            ).toMono().block()

            val countQueryPublisher: Publisher<Long> = r2dbc.withTransaction { status ->
                Mono.from(
                    status.connection.createStatement(
                        """
                    select count(*) from pet 
                    where id = '$petId' 
                    and owner_identity_id = '${petOwner.identityId}'
                    """.trimIndent()
                    ).execute()
                )
                    .flatMap { result ->
                        Mono.from(result.map { row -> row.get(0) as Long })
                    }
            }

            val count: Long = Mono.from(countQueryPublisher).block() as Long

            assertThat(count).isEqualTo(1)
        }

    }


}