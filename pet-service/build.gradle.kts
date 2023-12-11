plugins {
    kotlin("jvm")
    id("io.micronaut.application")
}

version = "0.1"
group = "tech.aaregall.lab.micronaut.petclinic"

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.assertj:assertj-core")
}

tasks.test {
    useJUnitPlatform()
}