package tech.aaregall.lab.micronaut.petclinic.identity.common

import jakarta.inject.Singleton
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.CLASS

@Singleton
@Retention(RUNTIME)
@Target(CLASS)
annotation class UseCase
