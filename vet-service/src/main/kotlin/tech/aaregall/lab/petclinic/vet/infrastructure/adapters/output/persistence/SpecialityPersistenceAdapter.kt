package tech.aaregall.lab.petclinic.vet.infrastructure.adapters.output.persistence

import io.micronaut.data.jdbc.runtime.JdbcOperations
import jakarta.inject.Singleton
import tech.aaregall.lab.petclinic.vet.application.ports.output.SpecialityOutputPort
import tech.aaregall.lab.petclinic.vet.domain.model.Speciality
import tech.aaregall.lab.petclinic.vet.domain.model.SpecialityId
import java.sql.ResultSet

@Singleton
internal class SpecialityPersistenceAdapter(private val jdbc: JdbcOperations): SpecialityOutputPort {

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

    override fun createSpeciality(speciality: Speciality): Speciality =
        jdbc.execute { conn ->
            conn.prepareStatement("INSERT INTO speciality (id, name, description) VALUES (?::uuid,?,?)", arrayOf("id", "name", "description"))
                .use { statement ->
                    statement.setString(1, speciality.id.toString())
                    statement.setString(2, speciality.name)
                    statement.setString(3, speciality.description)
                    statement.executeUpdate()
                    val rs = statement.generatedKeys
                    if (rs.next()) {
                        return@execute mapRow(rs)
                    } else {
                       error("Failed persisting Speciality, no rows returned [$speciality]")
                    }
                }
        }

    override fun loadSpeciality(specialityId: SpecialityId): Speciality? =
        jdbc.execute { conn ->
            conn.prepareStatement("SELECT * FROM speciality WHERE id = ?::uuid")
                .use { statement ->
                    statement.setString(1, specialityId.toString())
                    statement.executeQuery().use {
                        if (it.next()) mapRow(it) else null
                    }
                }
        }

    private val mapRow: (ResultSet) -> Speciality = { rs ->
        Speciality(
            id = SpecialityId.of(rs.getString("id")),
            name = rs.getString("name"),
            description = rs.getString("description")
        )
    }

}