package tech.aaregall.lab.petclinic.common

import jakarta.inject.Singleton

@Singleton
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class UseCase