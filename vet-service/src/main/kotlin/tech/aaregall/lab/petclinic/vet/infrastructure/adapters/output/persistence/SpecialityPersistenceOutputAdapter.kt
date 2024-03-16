package tech.aaregall.lab.petclinic.vet.infrastructure.adapters.output.persistence

import io.micronaut.data.jdbc.runtime.JdbcOperations
import jakarta.inject.Singleton
import tech.aaregall.lab.petclinic.vet.application.ports.output.SpecialityOutputPort
import tech.aaregall.lab.petclinic.vet.domain.model.Speciality
import tech.aaregall.lab.petclinic.vet.domain.model.SpecialityId
import tech.aaregall.lab.petclinic.vet.domain.model.Vet
import java.sql.ResultSet

@Singleton
internal class SpecialityPersistenceOutputAdapter(private val jdbc: JdbcOperations): SpecialityOutputPort {

    override fun specialityExistsByName(name: String): Boolean =
        jdbc.execute { conn ->
            conn.prepareStatement("SELECT EXISTS (SELECT 1 FROM speciality WHERE name ILIKE ?)")
                .use { statement ->
                    statement.setString(1, "%$name%")
                    statement.executeQuery().use {
                        it.next() && it.getBoolean(1)
                    }
                }
        }

    override fun findSpecialities(pageNumber: Int, pageSize: Int): Collection<Speciality>? =
        jdbc.execute { conn ->
            conn.prepareStatement("SELECT * FROM speciality ORDER BY name OFFSET ? LIMIT ?")
                .use { statement ->
                    statement.setInt(1, (maxOf(1, pageNumber) - 1) * pageSize)
                    statement.setInt(2, pageSize)
                    statement.executeQuery().use { generateSequence { if (it.next()) mapRow(it) else null }.toList() }
                }
        }

    override fun createSpeciality(speciality: Speciality): Speciality {
        TODO("Not yet implemented")
    }

    override fun loadSpeciality(specialityId: SpecialityId): Speciality? {
        TODO("Not yet implemented")
    }

    override fun setVetSpecialities(vet: Vet, specialities: Collection<Speciality>): Vet {
        TODO("Not yet implemented")
    }

    private val mapRow: (ResultSet) -> Speciality = { rs ->
        Speciality(
            id = SpecialityId.of(rs.getString("id")),
            name = rs.getString("name"),
            description = rs.getString("description")
        )
    }

}