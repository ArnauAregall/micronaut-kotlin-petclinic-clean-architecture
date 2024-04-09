package tech.aaregall.lab.petclinic.pet.infrastructure.adapters.output.persistence

import io.micronaut.data.r2dbc.operations.R2dbcOperations
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.reactivestreams.Publisher
import reactor.core.publisher.Mono
import tech.aaregall.lab.petclinic.pet.domain.model.Pet
import tech.aaregall.lab.petclinic.pet.domain.model.PetId
import tech.aaregall.lab.petclinic.pet.domain.model.PetOwner
import tech.aaregall.lab.petclinic.pet.domain.model.PetType
import tech.aaregall.lab.petclinic.pet.domain.model.PetType.BIRD
import tech.aaregall.lab.petclinic.pet.domain.model.PetType.DOG
import tech.aaregall.lab.petclinic.testresources.flyway.CleanDatabase
import java.time.LocalDate
import java.util.UUID

@MicronautTest(transactional = false)
@CleanDatabase
internal class PetPersistenceAdapterIT(
    private val petPersistenceAdapter: PetPersistenceAdapter,
    private val r2dbc: R2dbcOperations) {

    @Nested
    inner class FindPets {

        @Test
        fun `It should return an empty Flux when there are not Pet table records present`() {
            val result = petPersistenceAdapter.findPets(0, 20)

            assertThat(result.blockList())
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
            assertThat(firstTwentyPets.blockList())
                .isNotEmpty
                .hasSize(20)
                .extracting("name")
                .containsAll(expectedPetNames(1, 20))

            val lastFivePets = petPersistenceAdapter.findPets(9, 5)
            assertThat(lastFivePets.blockList())
                .isNotEmpty
                .hasSize(5)
                .extracting("name")
                .containsAll(expectedPetNames(46, 50))
        }

    }

    @Nested
    inner class LoadPetById {

        @Test
        fun `It should return a UnitReactive of empty Mono when Pet does not exist in the database`() {
            val result = petPersistenceAdapter.loadPetById(PetId.of(UUID.randomUUID()))

            assertThat(result.block())
                .isNull()
        }

        @Test
        fun `Should return a UnitReactive with a Mono containing the Pet exists in the database`() {
            val petId = UUID.randomUUID()
            val ownerIdentityId = UUID.randomUUID()

            Mono.from(
                r2dbc.withTransaction { status ->
                    Mono.from(
                        status.connection.createStatement("""
                            insert into pet (id, type, name, birth_date, owner_identity_id) values 
                            ('$petId', 'CAT', 'Silvester', '2024-02-01', '$ownerIdentityId')
                        """.trimIndent()).execute()
                    )
                }
            ).block()

            val result = petPersistenceAdapter.loadPetById(PetId.of(petId))

            assertThat(result.block()!!)
                .isNotNull
                .isInstanceOf(Pet::class.java)
                .extracting(Pet::id, Pet::type, Pet::name, Pet::birthDate, Pet::owner)
                .containsExactly(
                    PetId.of(petId), PetType.CAT, "Silvester", LocalDate.of(2024, 2, 1), PetOwner(ownerIdentityId)
                )
        }

    }

    @Nested
    inner class CountAllPets {

        @Test
        fun `Should return a UnitReactive with 0 when there are no Pet records on the database`() {
            val result = petPersistenceAdapter.countAllPets()

            assertThat(result.block()!!).isZero()
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

            assertThat(result.block()!!).isEqualTo(50)
        }

    }

    @Nested
    inner class CreatePet {

        @Test
        fun `It should create a Pet without PetOwner`() {
            val petId = PetId.create()

            petPersistenceAdapter.createPet(
                Pet(id = petId, type = DOG, name = "Bimo", birthDate = LocalDate.now(), owner = null)
            ).block()!!

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
            ).block()!!

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
    inner class UpdatePet {

        @Test
        fun `It should update the PetOwner from an existing Pet without PetOwner`() {
            val pet = Pet(PetId.create(), DOG, "Scooby Doo", LocalDate.now())

            Mono.from(
                r2dbc.withTransaction { status ->
                    Mono.from(
                        status.connection.createStatement("""
                            insert into pet (id, type, name, birth_date) values 
                            ('${pet.id}', '${pet.type.name}', '${pet.name}', '${pet.birthDate}')
                        """.trimIndent()).execute()
                    )
                }
            ).block()

            val petOwner = PetOwner(UUID.randomUUID())
            pet.owner = petOwner

            val result = petPersistenceAdapter.updatePet(pet).block()!!

            assertThat(result.owner)
                .isNotNull
                .isEqualTo(petOwner)

            val countQueryPublisher: Publisher<Long> = r2dbc.withTransaction { status ->
                Mono.from(status.connection.createStatement("select count(*) from pet where owner_identity_id = '${petOwner.identityId}'").execute())
                    .flatMap { result ->
                        Mono.from(result.map { row -> row.get(0) as Long })
                    }
            }

            val count: Long? = Mono.from(countQueryPublisher).block()

            assertThat(count).isEqualTo(1)
        }

        @Test
        fun `It should update the PetOwner from an existing Pet with PetOwner`() {
            val pet = Pet(PetId.create(), DOG, "Scooby Doo", LocalDate.now(), PetOwner(UUID.randomUUID()))

            Mono.from(
                r2dbc.withTransaction { status ->
                    Mono.from(
                        status.connection.createStatement("""
                            insert into pet (id, type, name, birth_date, owner_identity_id) values 
                            ('${pet.id}', '${pet.type.name}', '${pet.name}', '${pet.birthDate}', '${pet.owner!!.identityId}')
                        """.trimIndent()).execute()
                    )
                }
            ).block()

            val newPetOwner = PetOwner(UUID.randomUUID())
            pet.owner = newPetOwner

            val result = petPersistenceAdapter.updatePet(pet).block()!!

            assertThat(result.owner)
                .isNotNull
                .isEqualTo(newPetOwner)

            val countQueryPublisher: Publisher<Long> = r2dbc.withTransaction { status ->
                Mono.from(status.connection.createStatement("select count(*) from pet where owner_identity_id = '${newPetOwner.identityId}'").execute())
                    .flatMap { result ->
                        Mono.from(result.map { row -> row.get(0) as Long })
                    }
            }

            val count: Long? = Mono.from(countQueryPublisher).block()

            assertThat(count).isEqualTo(1)
        }

    }

    @Nested
    inner class DeletePet {

        @Test
        fun `It should return a UnitReactive containing false when the Pet is not deleted from the database`() {
            val pet = Pet(PetId.create(), DOG, "Non existing dog", LocalDate.now())

            val result = petPersistenceAdapter.deletePet(pet)

            assertThat(result.block()!!).isFalse()
        }

        @Test
        fun `It should return a UnitReactive containing true when the Pet is deleted from the database`() {
            val pet = Pet(PetId.create(), DOG, "Scooby Doo", LocalDate.now())

            Mono.from(
                r2dbc.withTransaction { status ->
                    Mono.from(
                        status.connection.createStatement("""
                            insert into pet (id, type, name, birth_date) values 
                            ('${pet.id}', '${pet.type.name}', '${pet.name}', '${pet.birthDate}')
                        """.trimIndent()).execute()
                    )
                }
            ).block()

            val result = petPersistenceAdapter.deletePet(pet)

            assertThat(result.block()!!).isTrue()

            val countQueryPublisher: Publisher<Long> = r2dbc.withTransaction { status ->
                Mono.from(status.connection.createStatement("select count(*) from pet where id = '${pet.id}'").execute())
                    .flatMap { result ->
                        Mono.from(result.map { row -> row.get(0) as Long })
                    }
            }

            val count: Long? = Mono.from(countQueryPublisher).block()

            assertThat(count).isZero()
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