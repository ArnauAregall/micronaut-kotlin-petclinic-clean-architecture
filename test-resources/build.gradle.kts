plugins {
    kotlin("jvm")
    id("io.micronaut.application")
    id("io.micronaut.test-resources")
}

group = "tech.aaregall.lab.petclinic"
version = "0.1"

dependencies {
    implementation("io.micronaut.testresources:micronaut-test-resources-extensions-core")
    implementation("org.testcontainers:testcontainers")
    implementation("io.rest-assured:kotlin-extensions:${project.properties["restAssuredKotlinExtensionsVersion"]}")
    implementation("com.github.dasniko:testcontainers-keycloak:${project.properties["testcontainersKeycloakVersion"]}")
}

tasks.test {
    useJUnitPlatform()
}