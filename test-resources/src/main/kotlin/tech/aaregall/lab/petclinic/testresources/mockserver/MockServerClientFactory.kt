package tech.aaregall.lab.petclinic.testresources.mockserver

import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Value
import jakarta.inject.Singleton
import org.mockserver.client.MockServerClient

@Factory
internal class MockServerClientFactory {

    @Singleton
    fun mockServerClient(@Value("\${test.mockserver.host}") mockServerHost: String,
                         @Value("\${test.mockserver.port}") mockServerPort: String): MockServerClient {
        return MockServerClient(mockServerHost, mockServerPort.toInt())
    }

}