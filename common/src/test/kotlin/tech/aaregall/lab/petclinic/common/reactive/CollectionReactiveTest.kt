package tech.aaregall.lab.petclinic.common.reactive

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.api.Test
import reactor.core.publisher.Flux

internal class CollectionReactiveTest {

    data class Pizza(val name: String)

    @Test
    fun `vararg single value constructor should build a Flux`() {
        val result = CollectionReactive(Pizza("Margherita"))

        assertThat(result)
            .isInstanceOf(CollectionReactive::class.java)

        assertThat(result.toFlux())
            .isInstanceOf(Flux::class.java)
            .satisfies( {
                assertThat(it.collectList().block())
                    .hasSize(1)
                    .extracting("name")
                    .containsExactly("Margherita")
            })

    }

    @Test
    fun `vararg multiple values constructor should build a Flux`() {
        val result = CollectionReactive(
            Pizza("Hawaiian"), Pizza("Four Cheese"), Pizza("BBQ"), Pizza("Tuna")
        )

        assertThat(result)
            .isInstanceOf(CollectionReactive::class.java)

        assertThat(result.toFlux())
            .isInstanceOf(Flux::class.java)
            .satisfies( {
                assertThat(it.collectList().block())
                    .hasSize(4)
                    .extracting("name")
                    .containsExactly("Hawaiian", "Four Cheese", "BBQ", "Tuna")
            })

    }

    @Test
    fun `Collection value constructor should build a Flux`() {
        val result = CollectionReactive(listOf(Pizza("Margherita"), Pizza("Pepperoni")))

        assertThat(result)
            .isInstanceOf(CollectionReactive::class.java)


        assertThat(result.toFlux())
            .isInstanceOf(Flux::class.java)
            .satisfies( {
                assertThat(it.collectList().block())
                    .hasSize(2)
                    .extracting("name")
                    .containsExactly("Margherita", "Pepperoni")
            })
    }

    @Test
    fun `toFlux should return a reactor Flux`() {
        val pizzaCollectionReactive = CollectionReactive(Pizza("Four Cheese"))

        val result = pizzaCollectionReactive.toFlux()

        assertThat(result)
            .isInstanceOf(Flux::class.java)
            .satisfies({
                assertThat(it.collectMap { pizza -> pizza.name }.block())
                    .hasSize(1)
                    .containsEntry("Four Cheese", Pizza("Four Cheese"))
            })
    }

    @Test
    fun `map should Flux items to any other type`() {
        data class Meal(val name: String)

        val pizzaCollectionReactive = CollectionReactive(Pizza("Margherita"), Pizza("Four Cheese"), Pizza("BBQ"))

        val mealCollectionReactive = pizzaCollectionReactive.map { pizza -> Meal("Pizza ${pizza.name}") }

        assertThat(mealCollectionReactive)
            .isInstanceOf(CollectionReactive::class.java)
            .satisfies({
                assertThat(it.toFlux())
                    .isInstanceOf(Flux::class.java)
                    .satisfies({flux ->
                        assertThat(flux.collectList().block())
                            .hasSize(3)
                            .extracting("name")
                            .containsExactly("Pizza Margherita", "Pizza Four Cheese", "Pizza BBQ")
                    })
            })
    }

    @Test
    fun `error should create a CollectionReactive with a Flux error`() {
        val result = CollectionReactive.error<IllegalStateException>(IllegalStateException("Something wrong happened"))

        assertThat(result)
            .isInstanceOf(CollectionReactive::class.java)
            .satisfies({
                assertThatCode { it.toFlux().collectList().block() }
                    .isInstanceOf(IllegalStateException::class.java)
                    .hasMessage("Something wrong happened")
            })
    }

}