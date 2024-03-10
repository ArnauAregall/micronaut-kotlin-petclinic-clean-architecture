package tech.aaregall.lab.petclinic.vet

import io.micronaut.runtime.EmbeddedApplication
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

@MicronautTest
internal class VetAppTest {

    @Test
    fun `App should be running`(application: EmbeddedApplication<*>) {
        assertThat(application.isRunning).isTrue()
    }

}