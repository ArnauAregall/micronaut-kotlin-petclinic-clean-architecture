package tech.aaregall.lab.petclinic.vet.infrastructure.adapters.output.persistence

import io.micronaut.data.jdbc.runtime.JdbcOperations
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.assertj.core.api.InstanceOfAssertFactories.list
import org.assertj.core.groups.Tuple.tuple
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import tech.aaregall.lab.petclinic.vet.domain.model.Speciality
import tech.aaregall.lab.petclinic.vet.domain.model.SpecialityId
import tech.aaregall.lab.petclinic.vet.domain.model.Vet
import tech.aaregall.lab.petclinic.vet.domain.model.VetId
import java.util.UUID.randomUUID

@MicronautTest(transactional = false)
internal class VetPersistenceAdapterIT(private val outputAdapter: VetPersistenceAdapter) {

    @BeforeEach
    fun setUp() {
        runSql("""
            TRUNCATE TABLE vet CASCADE;
            TRUNCATE TABLE speciality CASCADE;
        """.trimIndent())
    }

    @Nested
    inner class FindVets {

        @Test
        fun `Should return an empty collection when no records on the table`() {
            val result = outputAdapter.findVets(0, 10)

            assertThat(result)
                .isNotNull
                .isEmpty()
        }

        @Test
        fun `Should return a collection containing the first 10 Vets when there are more than 10 records in the table`() {
            val specialitiesIds = listOf(SpecialityId.create(), SpecialityId.create(), SpecialityId.create())

            runSql(buildString {
                append("INSERT INTO speciality(id, name, description) VALUES ")
                append(specialitiesIds.joinToString(",") {
                    "('$it', 'Speciality $it', 'Description for $it')"
                })
            })

            runSql(buildString {
                append("INSERT INTO vet (id) VALUES ")
                append((0..15).joinToString(",") { "('${randomUUID()}')" })
            })

            runSql("""
                INSERT INTO vet_speciality (vet_id, speciality_id)
                SELECT v.id, s.id
                FROM vet v, speciality s
            """.trimIndent())

            val result = outputAdapter.findVets(0, 10)

            assertThat(result)
                .isNotEmpty()
                .hasSize(10)
                .allSatisfy { vet ->
                    assertThat(vet as Vet)
                        .satisfies({ assertThat(it.id).isNotNull })
                        .satisfies({
                            assertThat(it.specialities)
                                .isNotEmpty()
                                .asInstanceOf(list(Speciality::class.java))
                                .hasSize(specialitiesIds.size)
                                .extracting(Speciality::id, Speciality::name, Speciality::description)
                                .containsExactlyInAnyOrder(
                                    *specialitiesIds.map { specialityId ->
                                        tuple(specialityId, "Speciality $specialityId", "Description for $specialityId")
                                    }.toTypedArray()
                                )
                        })
                }
        }

        @Test
        fun `Should return an empty collection when the requested page exceeds the total record count`() {
            runSql(buildString {
                append("INSERT INTO vet (id) VALUES ")
                append((0..10).joinToString(", ") { "('${randomUUID()}')" })
            })

            val result = outputAdapter.findVets(2, 20)

            assertThat(result).isEmpty()
        }

    }

    @Nested
    inner class CreateVet {

        @Test
        fun `Should return the Vet passed as argument and insert a row in the table`(jdbc: JdbcOperations) {
            val vet = Vet(id = VetId.create())

            val result = outputAdapter.createVet(vet)

            assertThat(result).isNotNull.isEqualTo(vet)

            jdbc.execute { c ->
                c.prepareStatement("SELECT EXISTS (SELECT 1 FROM vet WHERE id = '${vet.id}')").executeQuery().use {
                    it.next()
                    assertThat(it.getBoolean(1)).isTrue()
                }
            }
        }

    }

    @Nested
    inner class LoadVet {

        @Test
        fun `Should return null when Vet with the given VetId does not exist`() {
            val result = outputAdapter.loadVet(VetId.create())

            assertThat(result).isNull()
        }

        @Test
        fun `Should return the Vet with it's Specialities when Vet with the given VetId exists`() {
            val specialities = listOf(
                Speciality(SpecialityId.create(), "Surgery", "The surgery is..."),
                Speciality(SpecialityId.create(), "Oncology", "The oncology is..."),
                Speciality(SpecialityId.create(), "Dermatology", "The dermatology is...")
            )
            runSql(buildString {
                append("INSERT INTO speciality(id, name, description) VALUES ")
                append(specialities.joinToString(",") {
                    "('${it.id}', '${it.name}', '${it.description}')"
                })
            })

            val vetId = VetId.create()
            runSql("INSERT INTO vet (id) VALUES ('$vetId')")

            runSql("""
                INSERT INTO vet_speciality (vet_id, speciality_id)
                SELECT '$vetId', s.id FROM speciality s
            """.trimIndent())

            val result = outputAdapter.loadVet(vetId)

            assertThat(result)
                .isNotNull
                .satisfies({
                    assertThat(it!!.id).isEqualTo(vetId)
                })
                .satisfies({
                    assertThat(it!!.specialities)
                        .asInstanceOf(list(Speciality::class.java))
                        .hasSize(specialities.size)
                        .usingRecursiveComparison()
                        .isEqualTo(specialities)
                })
        }

    }

    @Nested
    inner class DeleteVet {

        @Test
        fun `Not yet implemented`() {
            assertThatCode { outputAdapter.deleteVet(Vet(VetId.create())) }
                .isInstanceOf(NotImplementedError::class.java)
        }

    }

    @Nested
    inner class SetVetSpecialities {

        @Test
        fun `Not yet implemented`() {
            assertThatCode { outputAdapter.setVetSpecialities(Vet(VetId.create()), emptySet()) }
                .isInstanceOf(NotImplementedError::class.java)
        }

    }

}