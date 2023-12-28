package tech.aaregall.lab.petclinic.test.spec.keycloak

import dasniko.testcontainers.keycloak.KeycloakContainer
import io.micronaut.core.annotation.ReflectiveAccess
import io.micronaut.test.extensions.testresources.TestResourcesPropertyProvider
import io.restassured.http.ContentType
import io.restassured.http.Header
import io.restassured.module.kotlin.extensions.Extract
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When

@ReflectiveAccess
class KeycloakSpec: TestResourcesPropertyProvider {

    companion object {
        private val keycloakContainer: KeycloakContainer = KeycloakContainer()
            .withRealmImportFiles("keycloak/system_test_realm-realm.json")
            .also {
                it.start()
            }

        private fun getTokenUrl(): String =
            keycloakContainer.authServerUrl + "/realms/system_test_realm/protocol/openid-connect/token"

        private fun getJwtToken(): String {
            return Given {
                contentType(ContentType.URLENC)
                formParam("grant_type", "password")
                formParam("client_id", "identity-service")
                formParam("client_secret", "identity-service-secret")
                formParam("username", "system_test_user")
                formParam("password", "system_test_user")
            } When {
                post(getTokenUrl())
            } Then {
                statusCode(200)
            } Extract {
                path("access_token")
            }
        }

        fun getAuthorizationBearer(): Header {
            return Header("Authorization", "Bearer ${getJwtToken()}")
        }
    }

    override fun provide(testProperties: MutableMap<String, Any>?): MutableMap<String, String> {
        return mutableMapOf(
            "micronaut.security.oauth2.clients.keycloak.openid.issuer" to "http://localhost:${keycloakContainer.httpPort}/realms/system_test_realm"
        )
    }
}