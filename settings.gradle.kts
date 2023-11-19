plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}
rootProject.name="micronaut-kotlin-kafka-petclinic"
include("pet-service")
include("identity-service")
