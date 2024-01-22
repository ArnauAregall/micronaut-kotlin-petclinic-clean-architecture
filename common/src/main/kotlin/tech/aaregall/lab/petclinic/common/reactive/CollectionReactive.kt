package tech.aaregall.lab.petclinic.common.reactive

import reactor.core.publisher.Flux

class CollectionReactive<T>(private val flux: Flux<T>) {

    constructor(vararg values: T & Any): this(Flux.fromIterable(values.toList()))

    constructor(collection: Collection<T>): this(Flux.fromIterable(collection))

    companion object {
        fun <O> error(throwable: Throwable): CollectionReactive<O> = CollectionReactive(Flux.error(throwable))
    }

    fun toFlux() = flux

    fun <O> map(mappingFunction: (T) -> O): CollectionReactive<O> = CollectionReactive(flux.map(mappingFunction))

}