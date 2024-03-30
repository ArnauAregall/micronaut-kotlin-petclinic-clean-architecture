package tech.aaregall.lab.petclinic.vet.infrastructure.adapters.output.persistence

import io.micronaut.data.jdbc.runtime.JdbcOperations
import jakarta.inject.Singleton
import tech.aaregall.lab.petclinic.vet.application.ports.output.VetOutputPort
import tech.aaregall.lab.petclinic.vet.domain.model.Speciality
import tech.aaregall.lab.petclinic.vet.domain.model.SpecialityId
import tech.aaregall.lab.petclinic.vet.domain.model.Vet
import tech.aaregall.lab.petclinic.vet.domain.model.VetId

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

                            rs.getString("speciality_id")?.let {
                                Speciality(
                                    id = SpecialityId.of(it),
                                    name = rs.getString("speciality_name"),
                                    description = rs.getString("speciality_description")
                                ).also { speciality ->
                                    (vet.specialities as MutableList<Speciality>).add(speciality)
                                }
                            }
                        }
                        vetMap.values.toList()
                    }
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

}