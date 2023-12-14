package tech.aaregall.lab.petclinic.identity.infrastructure.adapters.input.http.mapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import tech.aaregall.lab.petclinic.identity.domain.model.Identity
import tech.aaregall.lab.petclinic.identity.domain.model.IdentityId

internal class IdentityHttpMapperTest {

    private val identityHttpMapper = IdentityHttpMapper()

    @Test
    fun `Maps domain to response`() {
        val domain = Identity(id = IdentityId.create(), firstName = "John", lastName = "Doe")

        val response = identityHttpMapper.mapToResponse(domain)

        assertThat(response)
            .isNotNull
            .extracting("id", "firstName", "lastName")
            .containsExactly(domain.id.toString(), domain.firstName, domain.lastName)
    }

}