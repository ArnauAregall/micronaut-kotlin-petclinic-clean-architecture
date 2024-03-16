package tech.aaregall.lab.petclinic.vet.infrastructure.adapters.output.persistence

import io.micronaut.data.jdbc.runtime.JdbcOperations
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import tech.aaregall.lab.petclinic.vet.domain.model.Speciality
import tech.aaregall.lab.petclinic.vet.domain.model.SpecialityId
import tech.aaregall.lab.petclinic.vet.domain.model.Vet
import tech.aaregall.lab.petclinic.vet.domain.model.VetId
import java.util.UUID.nameUUIDFromBytes
import java.util.UUID.randomUUID

@MicronautTest(transactional = false)
internal class SpecialityPersistenceOutputAdapterIT(
    private val jdbc: JdbcOperations,
    private val outputAdapter: SpecialityPersistenceOutputAdapter) {

    private fun runSql(sql: String) = jdbc.execute { c -> c.prepareCall(sql).execute() }

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
        fun `Not yet implemented`() {
            assertThatCode { outputAdapter.createSpeciality(Speciality(SpecialityId.create(), "Foo")) }
                .isInstanceOf(NotImplementedError::class.java)
        }
    }

    @Nested
    inner class LoadSpeciality {

        @Test
        fun `Not yet implemented`() {
            assertThatCode { outputAdapter.loadSpeciality(SpecialityId.create()) }
                .isInstanceOf(NotImplementedError::class.java)
        }
    }

    @Nested
    inner class SetVetSpecialities {

        @Test
        fun `Not yet implemented`() {
            assertThatCode { outputAdapter.setVetSpecialities(Vet(VetId.create(), emptySet()), emptySet()) }
                .isInstanceOf(NotImplementedError::class.java)
        }
    }

}