package tech.aaregall.lab.petclinic.identity

import io.micronaut.runtime.EmbeddedApplication
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

@MicronautTest
internal class IdentityAppTest {

    @Test
    fun testItWorks(application: EmbeddedApplication<*>) {
        assertThat(application.isRunning).isTrue()
    }

}