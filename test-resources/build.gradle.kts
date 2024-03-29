plugins {
    kotlin("jvm")
    id("io.micronaut.library")
    id("io.micronaut.test-resources")
}

group = "tech.aaregall.lab.petclinic"
version = "0.1"

dependencies {
    implementation("io.micronaut.testresources:micronaut-test-resources-extensions-core")
    implementation("io.micronaut.data:micronaut-data-jdbc")
    implementation("org.testcontainers:testcontainers")
    implementation("org.testcontainers:mockserver")
    implementation("io.rest-assured:kotlin-extensions:${project.properties["restAssuredKotlinExtensionsVersion"]}")
    implementation("com.github.dasniko:testcontainers-keycloak:${project.properties["testcontainersKeycloakVersion"]}")
    api("org.mock-server:mockserver-client-java:${project.properties["testcontainersMockserverVersion"]}")
}

tasks.test {
    useJUnitPlatform()
}