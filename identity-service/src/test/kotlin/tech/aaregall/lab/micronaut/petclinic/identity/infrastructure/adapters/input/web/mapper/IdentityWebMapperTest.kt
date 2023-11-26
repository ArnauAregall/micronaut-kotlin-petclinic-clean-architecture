package tech.aaregall.lab.micronaut.petclinic.identity.infrastructure.adapters.input.web.mapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import tech.aaregall.lab.micronaut.petclinic.identity.domain.model.Identity
import tech.aaregall.lab.micronaut.petclinic.identity.domain.model.IdentityId

internal class IdentityWebMapperTest {

    private val identityWebMapper = IdentityWebMapper()

    @Test
    fun `Maps domain to response`() {
        val domain = Identity(id = IdentityId.create(), firstName = "John", lastName = "Doe")

        val response = identityWebMapper.mapToResponse(domain)

        assertThat(response)
            .isNotNull
            .extracting("id", "firstName", "lastName")
            .containsExactly(domain.id.toString(), domain.firstName, domain.lastName)
    }

}