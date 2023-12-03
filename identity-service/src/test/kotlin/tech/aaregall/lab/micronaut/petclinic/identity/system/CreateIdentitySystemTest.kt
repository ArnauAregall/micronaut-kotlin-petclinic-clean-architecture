package tech.aaregall.lab.micronaut.petclinic.identity.system

import io.micronaut.data.jdbc.runtime.JdbcOperations
import io.micronaut.http.HttpStatus
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.micronaut.test.extensions.testresources.annotation.TestResourcesProperties
import io.restassured.http.ContentType
import io.restassured.module.kotlin.extensions.Extract
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import jakarta.inject.Inject
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.testcontainers.shaded.org.awaitility.Awaitility.await
import tech.aaregall.lab.micronaut.petclinic.identity.config.IdentityKafkaConsumer
import tech.aaregall.lab.micronaut.petclinic.identity.domain.model.IdentityId
import tech.aaregall.lab.micronaut.petclinic.identity.spec.KeycloakSpec
import tech.aaregall.lab.micronaut.petclinic.identity.spec.KeycloakSpec.Companion.getAuthorizationBearer
import java.time.Duration

@MicronautTest(transactional = false)
@TestResourcesProperties(providers = [KeycloakSpec::class])
class CreateIdentitySystemTest {

    @Inject
    lateinit var embeddedServer: EmbeddedServer

    @Inject
    lateinit var jdbc: JdbcOperations

    @Inject
    lateinit var identityKafkaConsumer: IdentityKafkaConsumer

    @AfterEach
    fun tearDown() = identityKafkaConsumer.clear().also { jdbc.execute { it.prepareStatement("truncate table identity")} }

    @Test
    fun `Given an authenticated HTTP POST request to the API, it should persist an Identity in the DB and publish a Kafka event`() {
        val identityId: String =
        Given {
            body("""
                {
                    "first_name": "Foo",
                    "last_name": "Bar"
                }
            """.trimIndent())
            contentType(ContentType.JSON)
            header(getAuthorizationBearer())
        } When {
            port(embeddedServer.port)
            post("/api/identities")
        } Then {
            statusCode(HttpStatus.CREATED.code)
        } Extract {
            path("id")
        }

        jdbc.execute {
            val resultSet = it.prepareStatement(
                "select first_name, last_name from identity where id = '$identityId'")
                .executeQuery()
            resultSet.next()
            assertThat(resultSet.getString("first_name")).isEqualTo("Foo")
            assertThat(resultSet.getString("last_name")).isEqualTo("Bar")
        }

        await().atMost(Duration.ofSeconds(5)).until { identityKafkaConsumer.hasConsumedRecords() }

        assertThat(identityKafkaConsumer.get(IdentityId.of(identityId)))
            .isNotNull
            .extracting("firstName", "lastName")
            .containsExactly("Foo", "Bar")
    }

}