package tech.aaregall.lab.petclinic.common.reactive

import reactor.core.publisher.Mono

class UnitReactive<T>(private val mono: Mono<T>) {

    constructor(value: T & Any): this(Mono.just(value))

    companion object {
        fun <O> error(throwable: Throwable): UnitReactive<O> = UnitReactive(Mono.error(throwable))
    }

    fun toMono() = mono

    fun <O> map(mappingFunction: (T) -> O): UnitReactive<O> =
        UnitReactive(mono.map(mappingFunction))

    fun <O> flatMap(mappingFunction: (T) -> UnitReactive<O>): UnitReactive<O> =
        UnitReactive(mono.flatMap { mappingFunction(it).toMono()})

}