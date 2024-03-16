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
import java.util.UUID.randomUUID

@MicronautTest(transactional = false)
internal class SpecialityPersistenceOutputAdapterIT(
    private val jdbc: JdbcOperations,
    private val outputAdapter: SpecialityPersistenceOutputAdapter) {

    @BeforeEach
    fun setUp() {
        jdbc.execute { c -> c.prepareCall("TRUNCATE TABLE speciality CASCADE").execute() }
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
            jdbc.execute { c ->
                c.prepareCall("INSERT INTO speciality VALUES ('${randomUUID()}', 'Surgery'), ('${randomUUID()}', 'Bar')")
                    .execute()
            }

            val result = outputAdapter.specialityExistsByName("Dentistry")

            assertThat(result).isFalse()
        }

        @Test
        fun `Should return true when a name matches exactly`() {
            jdbc.execute { c -> c.prepareCall("INSERT INTO speciality VALUES ('${randomUUID()}', 'Surgery')").execute() }

            val result = outputAdapter.specialityExistsByName("Surgery")

            assertThat(result).isTrue()
        }

        @Test
        fun `Should return true when a name matches ignore case`() {
            jdbc.execute { c -> c.prepareCall("INSERT INTO speciality VALUES ('${randomUUID()}', 'Surgery')").execute() }

            val result = outputAdapter.specialityExistsByName("sUrGeRy")

            assertThat(result).isTrue()
        }

        @Test
        fun `Should return true when a name contains the given name`() {
            jdbc.execute { c -> c.prepareCall("INSERT INTO speciality VALUES ('${randomUUID()}', 'Cardiology Speciality')").execute() }

            val result = outputAdapter.specialityExistsByName("cardiology")

            assertThat(result).isTrue()
        }

    }

    @Nested
    inner class FindSpecialities {

        @Test
        fun `Not yet implemented`() {
            assertThatCode { outputAdapter.findSpecialities(0, 20) }
                .isInstanceOf(NotImplementedError::class.java)
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