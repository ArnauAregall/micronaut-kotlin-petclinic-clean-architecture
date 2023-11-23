import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test


class FooTest {

    private val foo: Foo = Foo()

    @Test
    fun `Says hello`() {
        Assertions.assertEquals(foo.sayHello(), "Hello world!")
    }

}