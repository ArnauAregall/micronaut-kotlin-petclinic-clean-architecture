package tech.aaregall.lab.petclinic.pet.spec

import io.micronaut.core.annotation.ReflectiveAccess
import io.micronaut.test.extensions.testresources.TestResourcesPropertyProvider
import org.mockserver.client.MockServerClient
import org.testcontainers.containers.MockServerContainer
import org.testcontainers.utility.DockerImageName

@ReflectiveAccess
class MockServerSpec: TestResourcesPropertyProvider {

    companion object {
        private val mockServerContainer: MockServerContainer =
            MockServerContainer(DockerImageName.parse("mockserver/mockserver")
                .withTag(MockServerClient::class.java.`package`.implementationVersion))
                .also {
                    it.start()
                }

        private val mockServerClient: MockServerClient =
            MockServerClient(mockServerContainer.host, mockServerContainer.serverPort)

        fun getMockServerClient(): MockServerClient = mockServerClient

    }

    override fun provide(testProperties: MutableMap<String, Any>): MutableMap<String, String> {
        val map = mutableMapOf<String, String>()
        testProperties.filterKeys { it.startsWith("micronaut.http") && it.contains("url") }
            .keys.forEach {
                map[it] = mockServerContainer.endpoint
            }
        return map
    }

}