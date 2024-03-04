import io.micronaut.gradle.docker.NativeImageDockerfile

plugins {
    kotlin("jvm")
    kotlin("plugin.jpa")
    id("io.micronaut.application")
    id("io.micronaut.aot")
    id("io.micronaut.test-resources")
}

version = "0.1"
group = "tech.aaregall.lab.petclinic"

dependencies {
    ksp("io.micronaut:micronaut-http-validation")
    ksp("io.micronaut.serde:micronaut-serde-processor")
    annotationProcessor("io.micronaut.validation:micronaut-validation-processor")
    implementation(project(":common"))
    implementation("io.micronaut.validation:micronaut-validation")
    implementation("io.micronaut.data:micronaut-data-hibernate-jpa")
    implementation("io.micronaut.sql:micronaut-hibernate-jpa")
    implementation("io.micronaut.sql:micronaut-jdbc-hikari")
    implementation("io.micronaut.flyway:micronaut-flyway")
    implementation("io.micronaut.kafka:micronaut-kafka")
    implementation("io.micronaut.kotlin:micronaut-kotlin-runtime")
    implementation("io.micronaut.serde:micronaut-serde-jackson")
    implementation("io.micronaut.security:micronaut-security-oauth2")
    implementation("io.micronaut.security:micronaut-security-jwt")
    implementation("io.micronaut:micronaut-http-client")
    runtimeOnly("ch.qos.logback:logback-classic")
    runtimeOnly("org.postgresql:postgresql")
    runtimeOnly("org.flywaydb:flyway-database-postgresql")
    runtimeOnly("org.yaml:snakeyaml")
    runtimeOnly("com.fasterxml.jackson.module:jackson-module-kotlin")
    testImplementation(project(":test-resources"))
    testImplementation("io.mockk:mockk:${project.properties["mockkVersion"]}")
    testImplementation("io.micronaut:micronaut-http-client")
    testImplementation("io.micronaut.data:micronaut-data-jdbc")
    testImplementation("io.micronaut.test:micronaut-test-rest-assured")
    testImplementation("io.micronaut.testresources:micronaut-test-resources-extensions-core")
    testImplementation("io.rest-assured:kotlin-extensions:${project.properties["restAssuredKotlinExtensionsVersion"]}")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testImplementation("org.assertj:assertj-core")
    testImplementation("org.testcontainers:testcontainers")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.testcontainers:kafka")
}

application {
    mainClass.set("tech.aaregall.lab.petclinic.identity.IdentityAppKt")
}

graalvmNative {
    toolchainDetection = false
}

micronaut {
    runtime("netty")
    testRuntime("junit5")
    processing {
        incremental(true)
        annotations("tech.aaregall.lab.*")
    }
    aot {
        // Please review carefully the optimizations enabled below
        // Check https://micronaut-projects.github.io/micronaut-aot/latest/guide/ for more details
        optimizeServiceLoading.set(false)
        convertYamlToJava.set(false)
        precomputeOperations.set(true)
        cacheEnvironment.set(true)
        optimizeClassLoading.set(true)
        deduceEnvironment.set(true)
        optimizeNetty.set(true)
    }
}

tasks.withType<NativeImageDockerfile> {
    jdkVersion.set("21")
}

