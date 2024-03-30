package tech.aaregall.lab.petclinic.vet.infrastructure.adapters.output.persistence

import io.micronaut.data.jdbc.runtime.JdbcOperations
import jakarta.inject.Singleton
import tech.aaregall.lab.petclinic.vet.application.ports.output.VetOutputPort
import tech.aaregall.lab.petclinic.vet.domain.model.Speciality
import tech.aaregall.lab.petclinic.vet.domain.model.SpecialityId
import tech.aaregall.lab.petclinic.vet.domain.model.Vet
import tech.aaregall.lab.petclinic.vet.domain.model.VetId
import java.sql.ResultSet

@Singleton
internal class VetPersistenceAdapter(private val jdbc: JdbcOperations): VetOutputPort {

    override fun findVets(pageNumber: Int, pageSize: Int): Collection<Vet>? =
        jdbc.execute { conn ->
            conn.prepareStatement("""
                WITH paginated_vets AS (
                    SELECT id FROM vet 
                    ORDER BY id OFFSET ? LIMIT ?
                )
                SELECT
                    v.id            AS vet_id,
                    s.id            AS speciality_id,
                    s.name          AS speciality_name,
                    s.description   AS speciality_description
                FROM paginated_vets v
                LEFT JOIN vet_speciality vs on v.id = vs.vet_id
                LEFT JOIN speciality s on s.id = vs.speciality_id
                ORDER BY v.id
            """.trimIndent())
                .use { statement ->
                    statement.setInt(1, (maxOf(1, pageNumber) - 1) * pageSize)
                    statement.setInt(2, pageSize)
                    statement.executeQuery().use { rs ->
                        val vetMap = HashMap<VetId, Vet>()

                        while (rs.next()) {
                            val vetId = VetId.of(rs.getString("vet_id"))
                            val vet = vetMap.getOrPut(vetId) { Vet(id = vetId, specialities = mutableListOf()) }
                            mapVetSpecialities(vet, rs)
                        }
                        vetMap.values.toList()
                    }
                }
        }

    override fun createVet(vet: Vet): Vet =
        jdbc.execute { conn ->
            conn.prepareStatement("INSERT INTO vet (id) VALUES (?::uuid)", arrayOf("id"))
                .use { statement ->
                    statement.setString(1, vet.id.toString())
                    statement.executeUpdate()
                    val rs = statement.generatedKeys
                    if (rs.next()) {
                        return@execute Vet(id = VetId.of(rs.getString("id")))
                    } else {
                        error("Failed persisting Vet, no rows returned [$vet]")
                    }
                }
        }

    override fun loadVet(vetId: VetId): Vet? =
        jdbc.execute { conn ->
            conn.prepareStatement("""
                SELECT
                    v.id            AS vet_id,
                    s.id            AS speciality_id,
                    s.name          AS speciality_name,
                    s.description   AS speciality_description
                FROM vet v
                LEFT JOIN vet_speciality vs on v.id = vs.vet_id
                LEFT JOIN speciality s on s.id = vs.speciality_id
                WHERE v.id = ?::uuid
            """.trimIndent()).use { statement ->
                statement.setString(1, vetId.toString())
                statement.executeQuery().use { rs ->
                    var vet: Vet? = null
                    while (rs.next()) {
                        if (vet == null) {
                            vet = Vet(id = VetId.of(rs.getString("vet_id")), specialities = mutableListOf())
                        }
                        vet = mapVetSpecialities(vet, rs)
                    }
                    vet
                }
            }
        }

    override fun deleteVet(vet: Vet): Boolean =
        jdbc.execute { conn ->
            conn.prepareStatement("DELETE FROM vet v WHERE v.id = ?::uuid").use { statement ->
                statement.setString(1, vet.id.toString())
                statement.executeUpdate() == 1
            }
        }

    override fun setVetSpecialities(vet: Vet, specialities: Collection<Speciality>): Vet {
        TODO("Not yet implemented")
    }

    private val mapVetSpecialities: (Vet, ResultSet) -> Vet = { vet, rs ->
        rs.getString("speciality_id")?.let {
            val speciality = Speciality(
                id = SpecialityId.of(it),
                name = rs.getString("speciality_name"),
                description = rs.getString("speciality_description")
            )
            (vet.specialities as MutableList<Speciality>).add(speciality)
            return@let vet
        } ?: vet
    }

}