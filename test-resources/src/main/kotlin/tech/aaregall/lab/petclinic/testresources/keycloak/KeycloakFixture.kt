package tech.aaregall.lab.petclinic.testresources.keycloak

import io.micronaut.context.annotation.Context
import io.micronaut.context.annotation.Value
import io.restassured.http.ContentType
import io.restassured.http.Header
import io.restassured.module.kotlin.extensions.Extract
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import tech.aaregall.lab.petclinic.testresources.keycloak.KeycloakUser.SYSTEM_TEST_USER

@Context
class KeycloakFixture(
    @Value("\${test.keycloak.openid-connect.token-url:unknown-keycloak-token-url}") private val tokenUrl: String,
    @Value("\${micronaut.security.oauth2.clients.keycloak.client-id}") private val clientId: String,
    @Value("\${micronaut.security.oauth2.clients.keycloak.client-secret}") private val clientSecret: String) {

    private fun getJwtToken(keycloakUser: KeycloakUser): String =
        Given {
            contentType(ContentType.URLENC)
            formParam("grant_type", "password")
            formParam("client_id", clientId)
            formParam("client_secret", clientSecret)
            formParam("username", keycloakUser.username)
            formParam("password", keycloakUser.password)
        } When {
            post(tokenUrl)
        } Then {
            log().ifError()
            statusCode(200)
        } Extract {
            path("access_token")
        }

    companion object {
        @Volatile
        private lateinit var instance: KeycloakFixture

        fun getAuthorizationBearer(keycloakUser: KeycloakUser = SYSTEM_TEST_USER): Header =
            Header("Authorization", "Bearer ${instance.getJwtToken(keycloakUser)}")
    }

    init {
        instance = this
    }

}