package tech.aaregall.lab.petclinic.common.error

import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.HttpStatus.BAD_REQUEST
import io.micronaut.http.HttpStatus.CONFLICT
import io.micronaut.http.HttpStatus.INTERNAL_SERVER_ERROR
import io.micronaut.http.hateoas.JsonError
import io.micronaut.http.hateoas.Link
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.ThrowingConsumer
import org.junit.jupiter.api.Test

internal class CommonHttpExceptionHandlerTest {

    private val exceptionHandler = CommonHttpExceptionHandler()

    private fun isExpectedHttpResponse(httpStatus: HttpStatus, exception: Exception, request: HttpRequest<*>) =
        ThrowingConsumer<HttpResponse<Any>> {
            assertThat(it)
                .isNotNull
                .isInstanceOf(HttpResponse::class.java)

            assertThat(it.status)
                .isEqualTo(httpStatus)

            assertThat(it.body)
                .isPresent
                .get()
                .isInstanceOf(JsonError::class.java)
                .usingRecursiveComparison()
                .isEqualTo(
                    JsonError(httpStatus.reason)
                        .link(Link.SELF, Link.of(request.uri))
                        .embedded("errors", listOf(JsonError(exception.message)))
                )
        }

    @Test
    fun `Should map IllegalArgumentException to Bad Request`() {
        val request = HttpRequest.GET<Any>("/bad-request")
        val exception = IllegalArgumentException("Bad parameter")

        val result = exceptionHandler.handle(request, exception)

        assertThat(result).satisfies(isExpectedHttpResponse(BAD_REQUEST, exception, request))
    }

    @Test
    fun `Should map IllegalStateException to Conflict`() {
        val request = HttpRequest.DELETE<Any>("/conflict")
        val exception = IllegalStateException("Conflicting resource")

        val result = exceptionHandler.handle(request, exception)

        assertThat(result).satisfies(isExpectedHttpResponse(CONFLICT, exception, request))
    }

    @Test
    fun `Should map RuntimeException to Internal Server Error`() {
        val request = HttpRequest.PATCH<Any>("internal-server-error", """
            {"foo": "bar"}
        """.trimIndent())
        val exception = RuntimeException("Something went wrong")

        val result = exceptionHandler.handle(request, exception)

        assertThat(result).satisfies(isExpectedHttpResponse(INTERNAL_SERVER_ERROR, exception, request))
    }


}