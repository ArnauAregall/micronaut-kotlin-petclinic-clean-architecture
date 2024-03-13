import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage
import io.micronaut.gradle.docker.NativeImageDockerfile

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
    runtimeOnly("ch.qos.logback:logback-classic")
    runtimeOnly("com.fasterxml.jackson.module:jackson-module-kotlin")
    testImplementation(project(":test-resources"))
    testImplementation("io.micronaut.testresources:micronaut-test-resources-extensions-core")
    testImplementation("org.junit.jupiter:junit-jupiter:${project.properties["junitJupiterVersion"]}")
    testImplementation("org.assertj:assertj-core:${project.properties["assertjVersion"]}")
    testImplementation("io.mockk:mockk:${project.properties["mockkVersion"]}")
}

application {
    mainClass.set("tech.aaregall.lab.petclinic.vet.VetAppKt")
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

tasks.test {
    useJUnitPlatform()
}

tasks.withType<DockerBuildImage> {
    images.add("${project.group}/${project.name}")
}

tasks.withType<NativeImageDockerfile> {
    jdkVersion.set("21")
}