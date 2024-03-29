package tech.aaregall.lab.petclinic.identity.system

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
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.testcontainers.shaded.org.awaitility.Awaitility.await
import tech.aaregall.lab.petclinic.identity.infrastructure.adapters.output.persistence.SYSTEM_ACCOUNT_AUDIT_ID
import tech.aaregall.lab.petclinic.identity.spec.KafkaConsumerSpec
import tech.aaregall.lab.petclinic.identity.spec.KafkaRecord
import tech.aaregall.lab.petclinic.testresources.keycloak.KeycloakFixture.Companion.getAuthorizationBearer
import tech.aaregall.lab.petclinic.testresources.keycloak.KeycloakPropsProvider
import java.time.Duration

@MicronautTest(transactional = false)
@TestResourcesProperties(providers = [KeycloakPropsProvider::class])
internal class CreateIdentitySystemTest(
    private val embeddedServer: EmbeddedServer,
    private val jdbc: JdbcOperations,
    private val kafkaConsumerSpec: KafkaConsumerSpec) {

    @AfterEach
    fun tearDown() = kafkaConsumerSpec.clear().also { jdbc.execute { it.prepareStatement("truncate table identity")} }

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
                "select first_name, last_name, created_by from identity where id = '$identityId'")
                .executeQuery()
            resultSet.next()
            assertThat(resultSet.getString("first_name")).isEqualTo("Foo")
            assertThat(resultSet.getString("last_name")).isEqualTo("Bar")
            assertThat(resultSet.getString("created_by"))
                .isNotNull
                .isNotEqualTo(SYSTEM_ACCOUNT_AUDIT_ID.toString())
        }

        await().atMost(Duration.ofSeconds(5)).until { kafkaConsumerSpec.hasConsumedRecords() }

        assertThat(kafkaConsumerSpec.get(identityId))
            .isNotNull
            .isNotEmpty()
            .hasSize(1)
            .allSatisfy {
                assertThat(it as KafkaRecord)
                    .satisfies({ record -> assertThat(record.getActionHeader()).isEqualTo("CREATE") })
                    .satisfies({ record ->
                        assertThat(record.body)
                            .extracting("firstName", "lastName")
                            .containsExactly("Foo", "Bar")
                    })
            }
    }

}