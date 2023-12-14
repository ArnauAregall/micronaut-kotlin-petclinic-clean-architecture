plugins {
    kotlin("jvm")
    id("io.micronaut.application")
    id("io.micronaut.aot")
}

version = "0.1"
group = "tech.aaregall.lab.petclinic"

dependencies {
    implementation(project(":common"))
    implementation("io.micronaut.kotlin:micronaut-kotlin-runtime")
    implementation("io.micronaut.serde:micronaut-serde-jackson")
    implementation("io.micronaut:micronaut-http-client")
    runtimeOnly("org.yaml:snakeyaml")
    testImplementation("io.micronaut.testresources:micronaut-test-resources-extensions-core")
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.assertj:assertj-core")
    testImplementation("io.mockk:mockk:${project.properties["mockkVersion"]}")
    testImplementation("org.mock-server:mockserver-client-java:${project.properties["testcontainersMockserverVersion"]}")
    testImplementation("org.testcontainers:mockserver")
}

application {
    mainClass.set("tech.aaregall.lab.petclinic.pet.PetApp")
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