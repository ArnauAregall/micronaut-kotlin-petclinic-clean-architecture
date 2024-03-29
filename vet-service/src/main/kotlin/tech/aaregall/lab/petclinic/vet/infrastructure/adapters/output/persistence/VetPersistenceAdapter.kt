package tech.aaregall.lab.petclinic.vet.infrastructure.adapters.output.persistence

import io.micronaut.data.jdbc.runtime.JdbcOperations
import jakarta.inject.Singleton
import tech.aaregall.lab.petclinic.vet.application.ports.output.VetOutputPort
import tech.aaregall.lab.petclinic.vet.domain.model.Speciality
import tech.aaregall.lab.petclinic.vet.domain.model.Vet
import tech.aaregall.lab.petclinic.vet.domain.model.VetId
import java.sql.ResultSet

@Singleton
internal class VetPersistenceAdapter(private val jdbc: JdbcOperations): VetOutputPort {

    override fun findVets(pageNumber: Int, pageSize: Int): Collection<Vet>? =
        jdbc.execute { conn ->
            conn.prepareStatement("SELECT * FROM vet OFFSET ? LIMIT ?")
                .use { statement ->
                    statement.setInt(1, (maxOf(1, pageNumber) - 1) * pageSize)
                    statement.setInt(2, pageSize)
                    statement.executeQuery().use { generateSequence { if (it.next()) mapRow(it) else null }.toList() }
                }
        }

    override fun createVet(vet: Vet): Vet {
        TODO("Not yet implemented")
    }

    override fun loadVet(vetId: VetId): Vet? {
        TODO("Not yet implemented")
    }

    override fun deleteVet(vet: Vet) {
        TODO("Not yet implemented")
    }

    override fun setVetSpecialities(vet: Vet, specialities: Collection<Speciality>): Vet {
        TODO("Not yet implemented")
    }

    private val mapRow: (ResultSet) -> Vet = { rs ->
        Vet(id = VetId.of(rs.getString("id")))
    }
}