plugins {
    kotlin("jvm")
    id("io.micronaut.library")
    id("io.micronaut.test-resources")
}

group = "tech.aaregall.lab.petclinic"
version = "0.1"

dependencies {
    implementation("io.micronaut.testresources:micronaut-test-resources-extensions-core")
    implementation("org.testcontainers:testcontainers")
    implementation("org.testcontainers:mockserver")
    implementation("io.micronaut.test:micronaut-test-junit5")
    implementation("org.flywaydb:flyway-core")
    implementation("io.rest-assured:kotlin-extensions:${project.properties["restAssuredKotlinExtensionsVersion"]}")
    implementation("com.github.dasniko:testcontainers-keycloak:${project.properties["testcontainersKeycloakVersion"]}")
    api("org.mock-server:mockserver-client-java:${project.properties["testcontainersMockserverVersion"]}")
}

tasks.test {
    useJUnitPlatform()
}