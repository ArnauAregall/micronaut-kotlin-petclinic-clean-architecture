package tech.aaregall.lab.petclinic.pet.domain

import io.micronaut.runtime.EmbeddedApplication
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import io.micronaut.test.extensions.testresources.annotation.TestResourcesProperties
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import tech.aaregall.lab.petclinic.pet.spec.R2dbcFlywaySpec

@MicronautTest
@TestResourcesProperties(providers = [R2dbcFlywaySpec::class]) // TODO Make the test does not require R2DBC spec
internal class PetAppTest {

    @Test
    fun `Application should be running`(application: EmbeddedApplication<*>) {
        assertThat(application.isRunning).isTrue()
    }

}