plugins {
    kotlin("jvm")
}

version = "0.1"
group = "tech.aaregall.lab.petclinic"

dependencies {
    implementation("jakarta.inject:jakarta.inject-api:${project.properties["jakartaInjectApiVersion"]}")
    implementation("io.projectreactor:reactor-core:${project.properties["reactorVersion"]}")
    implementation("io.micronaut:micronaut-http:${project.properties["micronautVersion"]}")
    testImplementation("org.junit.jupiter:junit-jupiter:${project.properties["junitJupiterVersion"]}")
    testImplementation("org.assertj:assertj-core:${project.properties["assertjVersion"]}")
}

tasks.test {
    useJUnitPlatform()
}