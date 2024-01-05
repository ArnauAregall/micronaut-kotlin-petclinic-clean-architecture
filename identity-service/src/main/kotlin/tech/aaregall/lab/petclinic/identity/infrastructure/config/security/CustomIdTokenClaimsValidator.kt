package tech.aaregall.lab.petclinic.identity.infrastructure.config.security

import io.micronaut.context.annotation.Replaces
import io.micronaut.security.oauth2.client.IdTokenClaimsValidator
import io.micronaut.security.oauth2.configuration.OauthClientConfiguration
import io.micronaut.security.token.Claims
import jakarta.inject.Singleton

/**
 * Custom claim validator to not restrict azp to clientId
 * See https://github.com/micronaut-projects/micronaut-security/issues/1543
 */
@Singleton
@Replaces(IdTokenClaimsValidator::class)
class CustomIdTokenClaimsValidator<T>(oauthClientConfigurations: Collection<OauthClientConfiguration>): IdTokenClaimsValidator<T>(oauthClientConfigurations) {

    override fun validateAzp(claims: Claims, clientId: String, audiences: MutableList<String>): Boolean {
        if (audiences.size < 2) {
            return true
        }
        return parseAzpClaim(claims)
            .map { audiences.containsIgnoreCase(it) }
            .orElse(false)
    }

}

private fun List<String>.containsIgnoreCase(element: String): Boolean {
    return this.any { it.equals(element, ignoreCase = true) }
}