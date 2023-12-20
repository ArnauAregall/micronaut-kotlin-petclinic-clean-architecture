package tech.aaregall.lab.petclinic.pet.spec

import io.micronaut.core.annotation.ReflectiveAccess
import io.micronaut.test.extensions.testresources.TestResourcesPropertyProvider
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

@ReflectiveAccess
class R2dbcFlywaySpec: TestResourcesPropertyProvider {

    companion object {
        private val postgreContainer =
            PostgreSQLContainer(DockerImageName.parse("postgres").withTag("latest"))
                .also { it.start() }
    }

    override fun provide(testProperties: MutableMap<String, Any>?): MutableMap<String, String> {
        return mutableMapOf(
            "r2dbc.datasources.default.url" to "r2dbc:${getPostgreUrl()}",
            "r2dbc.datasources.default.username" to postgreContainer.username,
            "r2dbc.datasources.default.password" to postgreContainer.password,
            "flyway.datasources.default.url" to "jdbc:${getPostgreUrl()}",
            "flyway.datasources.default.username" to postgreContainer.username,
            "flyway.datasources.default.password" to postgreContainer.password,
        )
    }

    private fun getPostgreUrl(): String = "postgresql://${postgreContainer.host}:${postgreContainer.firstMappedPort}/${postgreContainer.databaseName}?currentSchema=pet_service"

}