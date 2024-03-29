package tech.aaregall.lab.petclinic.testresources.keycloak

import dasniko.testcontainers.keycloak.KeycloakContainer
import io.micronaut.core.annotation.ReflectiveAccess
import io.micronaut.test.extensions.testresources.TestResourcesPropertyProvider

@ReflectiveAccess
class KeycloakPropsProvider: TestResourcesPropertyProvider {

    companion object {
        private val keycloakContainer: KeycloakContainer = KeycloakContainer()
            .withRealmImportFiles("keycloak/test-petclinic-realm.json")
            .also {
                it.start()
            }

        private fun getTokenUrl(): String =
            keycloakContainer.authServerUrl + "/realms/petclinic/protocol/openid-connect/token"
    }

    override fun provide(testProperties: MutableMap<String, Any>?): MutableMap<String, String> {
        return mutableMapOf(
            "test.keycloak.openid-connect.token-url" to getTokenUrl(),
            "micronaut.security.oauth2.clients.keycloak.openid.issuer" to "http://localhost:${keycloakContainer.httpPort}/realms/petclinic",
            "micronaut.security.oauth2.clients.keycloak.client-id" to "system-test-client",
            "micronaut.security.oauth2.clients.keycloak.client-secret" to "system-test-client-secret"
        )
    }
}