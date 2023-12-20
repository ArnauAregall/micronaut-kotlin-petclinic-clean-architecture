package tech.aaregall.lab.petclinic.pet.infrastructure.adapters.output.persistence

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import tech.aaregall.lab.petclinic.pet.domain.model.Pet
import tech.aaregall.lab.petclinic.pet.domain.model.PetId
import tech.aaregall.lab.petclinic.pet.domain.model.PetOwner
import tech.aaregall.lab.petclinic.pet.domain.model.PetType
import java.time.LocalDate
import java.util.UUID

internal class PetPersistenceMapperTest {

    private val petPersistenceMapper = PetPersistenceMapper()

    @Nested
    inner class MapToDomain {

        @ParameterizedTest
        @EnumSource(PetType::class)
        fun `Maps entity to domain without PetOwner`(petType: PetType) {
            val entity = PetPersistenceEntity(
                id = UUID.randomUUID(),
                type = petType.toString(),
                name = "Bimo",
                birthDate = LocalDate.now(),
                ownerIdentityId = null
            )

            val domain = petPersistenceMapper.mapToDomain(entity)

            assertThat(domain)
                .isNotNull
                .extracting(Pet::id, Pet::type, Pet::name, Pet::birthDate, Pet::owner)
                .containsExactly(PetId.of(entity.id), petType, entity.name, entity.birthDate, null)
        }

        @ParameterizedTest
        @EnumSource(PetType::class)
        fun `Maps entity to domain with PetOwner`(petType: PetType) {
            val entity = PetPersistenceEntity(
                id = UUID.randomUUID(),
                type = petType.toString(),
                name = "Bimo",
                birthDate = LocalDate.now(),
                ownerIdentityId = UUID.randomUUID()
            )

            val domain = petPersistenceMapper.mapToDomain(entity)

            assertThat(domain)
                .isNotNull
                .extracting(Pet::id, Pet::type, Pet::name, Pet::birthDate, Pet::owner)
                .containsExactly(
                    PetId.of(entity.id),
                    petType,
                    entity.name,
                    entity.birthDate,
                    PetOwner(entity.ownerIdentityId!!)
                )
        }

    }

    @Nested
    inner class MapToEntity {

        @ParameterizedTest
        @EnumSource(PetType::class)
        fun `Maps domain without PetOwner to entity`(petType: PetType) {
            val domain = Pet(
                id = PetId.create(),
                type = petType,
                name = "Bimo",
                birthDate = LocalDate.now()
            )

            val entity = petPersistenceMapper.mapToEntity(domain)

            assertThat(entity)
                .isNotNull
                .extracting(
                    PetPersistenceEntity::id,
                    PetPersistenceEntity::type,
                    PetPersistenceEntity::name,
                    PetPersistenceEntity::birthDate,
                    PetPersistenceEntity::ownerIdentityId
                )
                .containsExactly(
                    UUID.fromString(domain.id.toString()),
                    domain.type.toString(),
                    domain.name,
                    domain.birthDate,
                    null
                )
        }

        @ParameterizedTest
        @EnumSource(PetType::class)
        fun `Maps domain with PetOwner to entity`(petType: PetType) {
            val domain = Pet(
                id = PetId.create(),
                type = petType,
                name = "Bimo",
                birthDate = LocalDate.now(),
                owner = PetOwner(UUID.randomUUID())
            )

            val entity = petPersistenceMapper.mapToEntity(domain)

            assertThat(entity)
                .isNotNull
                .extracting(
                    PetPersistenceEntity::id,
                    PetPersistenceEntity::type,
                    PetPersistenceEntity::name,
                    PetPersistenceEntity::birthDate,
                    PetPersistenceEntity::ownerIdentityId
                )
                .containsExactly(
                    UUID.fromString(domain.id.toString()),
                    domain.type.toString(),
                    domain.name,
                    domain.birthDate,
                    domain.owner!!.identityId
                )
        }

    }

}