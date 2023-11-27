val kotlinVersion = project.properties["kotlinVersion"]

repositories {
    mavenCentral()
}

plugins {
    kotlin("jvm")
    id("org.jetbrains.kotlin.plugin.allopen")
    id("com.google.devtools.ksp")
    id("io.micronaut.application") apply false
    id("io.micronaut.aot") apply false
}

kotlin {
    jvmToolchain(21)
}

allprojects {
    apply {
        plugin("com.google.devtools.ksp")
        plugin("org.jetbrains.kotlin.plugin.allopen")
    }

    dependencies {
        subprojects {
            implementation("org.jetbrains.kotlin:kotlin-reflect:${kotlinVersion}")
            implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${kotlinVersion}")
        }

    }

    repositories {
        mavenCentral()
    }

    allOpen {
        annotation("jakarta.persistence.Entity")
    }

}
