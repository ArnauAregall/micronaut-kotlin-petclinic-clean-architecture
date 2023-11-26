rootProject.name="micronaut-kotlin-kafka-petclinic"

include("pet-service")
include("identity-service")

pluginManagement {
    val kotlinVersion: String by settings
    val kspVersion: String by settings
    val micronautVersion: String by settings

    plugins {
        kotlin("jvm") version kotlinVersion
        kotlin("plugin.jpa") version kotlinVersion
        id("org.jetbrains.kotlin.plugin.allopen") version kotlinVersion
        id("com.google.devtools.ksp") version kspVersion
        id("io.micronaut.application") version micronautVersion
        id("io.micronaut.aot") version micronautVersion
    }

}