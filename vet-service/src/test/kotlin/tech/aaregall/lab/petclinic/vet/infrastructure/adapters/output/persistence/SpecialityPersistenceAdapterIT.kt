package tech.aaregall.lab.petclinic.vet.infrastructure.adapters.output.persistence

import io.micronaut.data.jdbc.runtime.JdbcOperations
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import tech.aaregall.lab.petclinic.testresources.postgres.PostgresFixture.Companion.runSql
import tech.aaregall.lab.petclinic.vet.domain.model.Speciality
import tech.aaregall.lab.petclinic.vet.domain.model.SpecialityId
import java.util.UUID.nameUUIDFromBytes
import java.util.UUID.randomUUID

@MicronautTest(transactional = false)
internal class SpecialityPersistenceAdapterIT(private val outputAdapter: SpecialityPersistenceAdapter) {

    @BeforeEach
    fun setUp() {
        runSql("TRUNCATE TABLE speciality CASCADE")
    }

    @Nested
    inner class SpecialityExistsByName {

        @Test
        fun `Should return false when no records on the table`() {
            val result = outputAdapter.specialityExistsByName("Surgery")

            assertThat(result).isFalse()
        }

        @Test
        fun `Should return false when no matching names on the table`() {
            runSql("INSERT INTO speciality VALUES ('${randomUUID()}', 'Surgery'), ('${randomUUID()}', 'Bar')")

            val result = outputAdapter.specialityExistsByName("Dentistry")

            assertThat(result).isFalse()
        }

        @Test
        fun `Should return true when a name matches exactly`() {
            runSql("INSERT INTO speciality VALUES ('${randomUUID()}', 'Surgery')")

            val result = outputAdapter.specialityExistsByName("Surgery")

            assertThat(result).isTrue()
        }

        @Test
        fun `Should return true when a name matches ignore case`() {
            runSql("INSERT INTO speciality VALUES ('${randomUUID()}', 'Surgery')")

            val result = outputAdapter.specialityExistsByName("sUrGeRy")

            assertThat(result).isTrue()
        }

        @Test
        fun `Should return true when a name contains the given name`() {
            runSql("INSERT INTO speciality VALUES ('${randomUUID()}', 'Cardiology Speciality')")

            val result = outputAdapter.specialityExistsByName("cardiology")

            assertThat(result).isTrue()
        }

    }

    @Nested
    inner class FindSpecialities {

        @Test
        fun `Should return an empty collection when no records on the table`() {
            val result = outputAdapter.findSpecialities(0, 10)

            assertThat(result)
                .isNotNull
                .isEmpty()
        }

        @Test
        fun `Should return a collection containing the first 10 Specialities sorted by name when there are more than 10 records in the table`() {
            runSql(buildString {
                append("INSERT INTO speciality (id, name, description) VALUES ")
                append((0..15).joinToString(", ") { index ->
                    "('${nameUUIDFromBytes(index.toString().toByteArray())}', 'Speciality $index', 'Description for Speciality $index')"
                })
            })

            val result = outputAdapter.findSpecialities(0, 10)

            assertThat(result)
                .isNotEmpty()
                .hasSize(10)
                .allSatisfy {
                    assertThat(it as Speciality)
                        .extracting(Speciality::id)
                        .isNotNull
                }
                .extracting("name")
                .isEqualTo(IntRange(0, 15).map { index -> "Speciality $index" }.toList().sorted().take(10))
        }

        @Test
        fun `Should return an empty collection when the requested page exceeds the total record count`() {
            runSql(buildString {
                append("INSERT INTO speciality (id, name, description) VALUES ")
                append((0..10).joinToString(", ") { index ->
                    "('${nameUUIDFromBytes(index.toString().toByteArray())}', 'Speciality $index', 'Description for Speciality $index')"
                })
            })

            val result = outputAdapter.findSpecialities(2, 20)

            assertThat(result).isEmpty()
        }
    }

    @Nested
    inner class CreateSpeciality {

        @Test
        fun `Should return the Speciality passed as argument and insert a row in the table`(jdbc: JdbcOperations) {
            val speciality = Speciality(SpecialityId.create(), "Surgery", "Surgery speciality description")

            val result = outputAdapter.createSpeciality(speciality)

            assertThat(result).isNotNull.isEqualTo(result)

            jdbc.execute { c -> c.prepareStatement("""
                SELECT EXISTS (
                    SELECT 1 
                    FROM speciality 
                    WHERE id = '${speciality.id}' 
                    AND name = '${speciality.name}' 
                    AND description = '${speciality.description}'
                )
            """.trimIndent()).executeQuery().use {
                it.next()
                assertThat(it.getBoolean(1)).isTrue()
            }}
        }
    }

    @Nested
    inner class LoadSpeciality {

        @Test
        fun `Should return null when Speciality with the given SpecialityId does not exist`() {
            val result = outputAdapter.loadSpeciality(SpecialityId.create())

            assertThat(result).isNull()
        }

        @Test
        fun `Should return the Speciality when Speciality with the given SpecialityId exists`() {
            val specialityId = SpecialityId.create()
            runSql("INSERT INTO speciality (id, name, description) VALUES ('${specialityId}', 'Behaviorism', 'Description for Behaviorism')")

            val result = outputAdapter.loadSpeciality(specialityId)

            assertThat(result)
                .isNotNull
                .isInstanceOf(Speciality::class.java)
                .extracting("id", "name", "description")
                .containsExactly(specialityId, "Behaviorism", "Description for Behaviorism")
        }

    }

    @Nested
    inner class CountAll {

        @Test
        fun `Should return zero when there are no records on the table`() {
            val result = outputAdapter.countAll()

            assertThat(result).isZero()
        }

        @ParameterizedTest
        @ValueSource(ints = [1, 10, 50])
        fun `Should return the total number of records on the table`(totalRecords: Int) {
            runSql(buildString {
                append("INSERT INTO speciality (id, name, description) VALUES ")
                append((1..totalRecords).joinToString(", ") { index ->
                    "('${nameUUIDFromBytes(index.toString().toByteArray())}', 'Speciality $index', 'Description for Speciality $index')"
                })
            })

            val result = outputAdapter.countAll()

            assertThat(result).isEqualTo(totalRecords)
        }

    }

}