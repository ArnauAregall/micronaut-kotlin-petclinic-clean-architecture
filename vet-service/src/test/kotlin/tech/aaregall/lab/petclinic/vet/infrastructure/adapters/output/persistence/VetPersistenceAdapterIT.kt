package tech.aaregall.lab.petclinic.vet.infrastructure.adapters.output.persistence

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import tech.aaregall.lab.petclinic.vet.domain.model.Vet
import tech.aaregall.lab.petclinic.vet.domain.model.VetId
import java.util.UUID.nameUUIDFromBytes

@MicronautTest(transactional = false)
internal class VetPersistenceAdapterIT(private val outputAdapter: VetPersistenceAdapter) {

    @BeforeEach
    fun setUp() {
        runSql("TRUNCATE TABLE vet CASCADE")
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
            runSql(buildString {
                append("INSERT INTO vet (id) VALUES ")
                append((0..15).joinToString(", ") { index ->
                    "('${nameUUIDFromBytes(index.toString().toByteArray())}')"
                })
            })

            val result = outputAdapter.findVets(0, 10)

            assertThat(result)
                .isNotEmpty()
                .hasSize(10)
                .allSatisfy {
                    assertThat(it as Vet)
                        .extracting(Vet::id)
                        .isNotNull
                }
        }

        @Test
        fun `Should return an empty collection when the requested page exceeds the total record count`() {
            runSql(buildString {
                append("INSERT INTO vet (id) VALUES ")
                append((0..10).joinToString(", ") { index ->
                    "('${nameUUIDFromBytes(index.toString().toByteArray())}')"
                })
            })

            val result = outputAdapter.findVets(2, 20)

            assertThat(result).isEmpty()
        }

    }

    @Nested
    inner class CreateVet {

        @Test
        fun `Not yet implemented`() {
            assertThatCode { outputAdapter.createVet(Vet(id = VetId.create())) }
                .isInstanceOf(NotImplementedError::class.java)
        }

    }

    @Nested
    inner class LoadVet {

        @Test
        fun `Not yet implemented`() {
            assertThatCode { outputAdapter.loadVet(VetId.create()) }
                .isInstanceOf(NotImplementedError::class.java)
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