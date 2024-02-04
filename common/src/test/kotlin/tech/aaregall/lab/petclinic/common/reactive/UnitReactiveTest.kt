package tech.aaregall.lab.petclinic.common.reactive

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono

internal class UnitReactiveTest {

    data class Foo(val name: String)

    data class Bar(val name: String)

    @Test
    fun `Value constructor should build a Mono`() {
        val result = UnitReactive(Foo("Alice"))

        assertThat(result)
            .isInstanceOf(UnitReactive::class.java)

        assertThat(result.toMono())
            .isInstanceOf(Mono::class.java)
            .satisfies( {
                assertThat(it.block())
                    .isInstanceOf(Foo::class.java)
                    .extracting("name")
                    .isEqualTo("Alice")
            })
    }

    @Test
    fun `toMono should return a reactor Mono`() {
        val fooUnitReactive = UnitReactive(Mono.just(Foo("Alice")))

        val result = fooUnitReactive.toMono()

        assertThat(result)
            .isInstanceOf(Mono::class.java)
            .satisfies( {
                assertThat(it.block())
                    .isInstanceOf(Foo::class.java)
                    .extracting(Foo::name)
                    .isEqualTo("Alice")
            })
    }

    @Test
    fun `flatMap should map to another UnitReactive of any type`() {
        val fooUnitReactive = UnitReactive(Mono.just(Foo("Bob")))

        val result = fooUnitReactive.flatMap { UnitReactive(Mono.just(Bar(it.name))) }

        assertThat(result)
            .isInstanceOf(UnitReactive::class.java)
            .satisfies( {
                assertThat(it.toMono().block())
                    .isInstanceOf(Bar::class.java)
                    .extracting(Bar::name)
                    .isEqualTo("Bob")
            })
    }

    @Test
    fun `error should create a UnitReactive with a Mono error`() {
        val result = UnitReactive.error<IllegalStateException>(IllegalStateException("Something wrong happened"))

        assertThat(result)
            .isInstanceOf(UnitReactive::class.java)
            .satisfies({

                assertThatCode { result.toMono().block() }
                    .isInstanceOf(IllegalStateException::class.java)
                    .hasMessage("Something wrong happened")
            })
    }

    @Test
    fun `empty should create a UnitReactive with a Mono empty`() {
        val result = UnitReactive.empty<Any>()

        assertThat(result)
            .isInstanceOf(UnitReactive::class.java)
            .satisfies({
                assertThat(result.toMono().block())
                    .isNull()
            })
    }

    @Test
    fun `block should return the Mono value when is not empty`() {
        val result = UnitReactive("Foo").block()

        assertThat(result)
            .isNotNull
            .isEqualTo("Foo")
    }

    @Test
    fun `block should return null when the Mono value empty`() {
        val result = UnitReactive.empty<Any>().block()

        assertThat(result).isNull()
    }

}