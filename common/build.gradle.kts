plugins {
    kotlin("jvm")
}

version = "0.1"
group = "tech.aaregall.lab.petclinic"

dependencies {
    implementation("jakarta.inject:jakarta.inject-api:${project.properties["jakartaInjectApiVersion"]}")
}