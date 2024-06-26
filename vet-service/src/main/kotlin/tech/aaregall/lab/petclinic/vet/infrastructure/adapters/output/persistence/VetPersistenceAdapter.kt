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
                    ORDER BY created_at OFFSET ? LIMIT ?
                )
                SELECT
                    v.id            AS vet_id,
                    s.id            AS speciality_id,
                    s.name          AS speciality_name,
                    s.description   AS speciality_description
                FROM paginated_vets v
                LEFT JOIN vet_speciality vs on v.id = vs.vet_id
                LEFT JOIN speciality s on s.id = vs.speciality_id
            """.trimIndent())
                .use { statement ->
                    statement.setInt(1, (maxOf(1, pageNumber) - 1) * pageSize)
                    statement.setInt(2, pageSize)
                    statement.executeQuery().use { rs ->
                        val vetMap = LinkedHashMap<VetId, Vet>()

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

    override fun setVetSpecialities(vet: Vet, specialities: Collection<Speciality>): Vet =
        jdbc.execute { conn ->
            conn.autoCommit = false
            try {
                conn.prepareStatement("DELETE FROM vet_speciality WHERE vet_id = ?::uuid").use { statement ->
                    statement.setString(1, vet.id.toString())
                    statement.executeUpdate()
                }

                conn.prepareStatement("INSERT INTO vet_speciality (vet_id, speciality_id) VALUES (?::uuid, ?::uuid)").use { statement ->
                    specialities.forEach { speciality ->
                        statement.setString(1, vet.id.toString())
                        statement.setString(2, speciality.id.toString())
                        statement.addBatch()
                    }
                    statement.executeBatch()
                }

                conn.commit()
            } catch (e: Exception) {
                conn.rollback()
                error("Failed setting Vet specialities: ${e.message} [vet=${vet}, specialities=${specialities}]")
            } finally {
                conn.autoCommit = true
            }
            loadVet(vet.id)!!
        }

    override fun countAll(): Int =
        jdbc.execute { conn ->
            conn.prepareStatement("SELECT COUNT(id) FROM vet")
                .use { statement ->
                    statement.executeQuery().use {
                        if (it.next()) it.getInt(1) else 0
                    }
                }
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