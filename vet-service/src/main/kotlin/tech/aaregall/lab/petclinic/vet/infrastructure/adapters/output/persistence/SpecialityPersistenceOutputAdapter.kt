package tech.aaregall.lab.petclinic.vet.infrastructure.adapters.output.persistence

import io.micronaut.data.jdbc.runtime.JdbcOperations
import jakarta.inject.Singleton
import tech.aaregall.lab.petclinic.vet.application.ports.output.SpecialityOutputPort
import tech.aaregall.lab.petclinic.vet.domain.model.Speciality
import tech.aaregall.lab.petclinic.vet.domain.model.SpecialityId
import tech.aaregall.lab.petclinic.vet.domain.model.Vet

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

    override fun findSpecialities(pageNumber: Int, pageSize: Int): Collection<Speciality>? {
        TODO("Not yet implemented")
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
}