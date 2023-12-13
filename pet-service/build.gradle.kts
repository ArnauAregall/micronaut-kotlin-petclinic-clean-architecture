plugins {
    kotlin("jvm")
    id("io.micronaut.application")
}

version = "0.1"
group = "tech.aaregall.lab.petclinic"

dependencies {
    implementation(project(":common"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.assertj:assertj-core")
    testImplementation("io.mockk:mockk:${project.properties["mockkVersion"]}")
}

tasks.test {
    useJUnitPlatform()
}