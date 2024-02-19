plugins {
    kotlin("jvm")
}

version = "0.1"
group = "tech.aaregall.lab.petclinic"

dependencies {
    implementation(project(":common"))
    testImplementation("org.junit.jupiter:junit-jupiter:${project.properties["junitJupiterVersion"]}")
    testImplementation("org.assertj:assertj-core:${project.properties["assertjVersion"]}")
    testImplementation("io.mockk:mockk:${project.properties["mockkVersion"]}")
}

tasks.test {
    useJUnitPlatform()
}