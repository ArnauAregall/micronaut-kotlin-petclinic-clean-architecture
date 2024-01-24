package tech.aaregall.lab.petclinic.pet.infrastructure.adapters.output.persistence

import io.micronaut.data.r2dbc.operations.R2dbcOperations
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
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

    @BeforeEach
    fun setUp() {
        Mono.from(r2dbc.withTransaction { status ->
            status.connection.createStatement("truncate table pet").execute()
        }).block()
    }

    @Nested
    inner class FindPets {

        @Test
        fun `It should return an empty Flux when there are not Pet table records present`() {
            val result = petPersistenceAdapter.findPets(0, 20)

            assertThat(result.toFlux().collectList().block())
                .isEmpty()
        }

        @Test
        fun `It should return Pets based on pagination arguments when Pet table records are present`() {
            Mono.from(
                r2dbc.withTransaction { status ->
                    Mono.from(
                        status.connection.createStatement(buildString {
                            append("insert into pet (id, type, name, birth_date) values ")
                            append(IntRange(start = 1, endInclusive = 50).joinToString(", ") { index ->
                                "('${UUID.nameUUIDFromBytes(index.toString().toByteArray())}', 'DOG', 'Puppy #$index', '2024-01-01')"
                            })
                            append(";")
                        }
                        ).execute()
                    )
                }
            ).block()

            fun expectedPetNames(start: Int, endInclusive: Int) = IntRange(start, endInclusive).map { index -> "Puppy #$index" }.toList()

            val firstTwentyPets = petPersistenceAdapter.findPets(0, 20)
            assertThat(firstTwentyPets.toFlux().collectList().block())
                .isNotEmpty
                .hasSize(20)
                .extracting("name")
                .containsAll(expectedPetNames(1, 20))

            val lastFivePets = petPersistenceAdapter.findPets(9, 5)
            assertThat(lastFivePets.toFlux().collectList().block())
                .isNotEmpty
                .hasSize(5)
                .extracting("name")
                .containsAll(expectedPetNames(46, 50))
        }

    }

    @Nested
    inner class CountAllPets {

        @Test
        fun `Should return a UnitReactive with 0 when there are no Pet records on the database`() {
            val result = petPersistenceAdapter.countAllPets()

            assertThat(result.toMono().block()).isZero()
        }

        @Test
        fun `Should return a UnitReactive with the total number of Pets when Pet records are present on the database`() {
            Mono.from(
                r2dbc.withTransaction { status ->
                    Mono.from(
                        status.connection.createStatement(buildString {
                            append("insert into pet (id, type, name, birth_date) values ")
                            append(IntRange(start = 1, endInclusive = 50).joinToString(", ") { index ->
                                "('${UUID.nameUUIDFromBytes(index.toString().toByteArray())}', 'CAT', '$index', '2024-01-01')"
                            })
                            append(";")
                        }
                        ).execute()
                    )
                }
            ).block()

            val result = petPersistenceAdapter.countAllPets()

            assertThat(result.toMono().block()).isEqualTo(50)
        }

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

    @Nested
    inner class DeletePetsByPetOwner {

        @Test
        fun `It should delete all Pets by PetOwner when Pets exist`() {
            val petOwner = PetOwner(UUID.randomUUID())

            Mono.from(
                r2dbc.withTransaction { status ->
                    Mono.from(
                        status.connection.createStatement("""
                        insert into pet (id, type, name, birth_date, owner_identity_id) values 
                        ('${UUID.randomUUID()}', 'DOG', 'Snoopy', '1950-10-04', '${petOwner.identityId}'),
                        ('${UUID.randomUUID()}', 'CAT', 'Garfield', '1988-09-17', '${petOwner.identityId}'),
                        ('${UUID.randomUUID()}', 'BIRD', 'Woodstock', '1950-10-04', '${petOwner.identityId}');
                        """.trimIndent()
                        ).execute()
                    )
                }
            ).block()

            petPersistenceAdapter.deletePetsByPetOwner(petOwner)

            val countQueryPublisher: Publisher<Long> = r2dbc.withTransaction { status ->
                Mono.from(
                    status.connection.createStatement("""
                    select count(*) from pet where owner_identity_id = '${petOwner.identityId}'
                    """.trimIndent()
                    ).execute()
                )
                    .flatMap { result ->
                        Mono.from(result.map { row -> row.get(0) as Long })
                    }
            }

            val count: Long = Mono.from(countQueryPublisher).block() as Long

            assertThat(count).isZero()
        }

    }


}