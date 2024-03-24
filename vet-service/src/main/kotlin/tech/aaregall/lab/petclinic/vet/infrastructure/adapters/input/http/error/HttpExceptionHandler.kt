package tech.aaregall.lab.petclinic.vet.infrastructure.adapters.input.http.error

import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Error
import io.micronaut.http.hateoas.JsonError
import io.micronaut.http.hateoas.Link
import io.micronaut.http.server.exceptions.ExceptionHandler
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory

/**
 * TODO: move to common
 */
@Singleton
internal class HttpExceptionHandler: ExceptionHandler<Exception, HttpResponse<Any>> {

    private val logger = LoggerFactory.getLogger(this::class.java)

    companion object {

        private fun badRequestResponse(request: HttpRequest<*>, exception: Exception): HttpResponse<Any> =
            HttpResponse.badRequest<JsonError>().body(exceptionToJsonError(HttpStatus.BAD_REQUEST.reason, request, exception))

        private fun conflictResponse(request: HttpRequest<*>, exception: Exception): HttpResponse<Any> =
            HttpResponse.status<JsonError>(HttpStatus.CONFLICT).body(exceptionToJsonError(HttpStatus.CONFLICT.reason, request, exception))

        private fun internalServerErrorResponse(request: HttpRequest<*>, exception: Exception): HttpResponse<Any> =
            HttpResponse.serverError<JsonError>().body(exceptionToJsonError(HttpStatus.INTERNAL_SERVER_ERROR.reason, request, exception))

        private fun exceptionToJsonError(message: String, request: HttpRequest<*>, exception: Exception): JsonError =
            JsonError(message)
                .link(Link.SELF, Link.of(request.uri))
                .embedded("errors", listOf(JsonError(exception.message)))
    }

    @Error(global = true, exception = Exception::class)
    override fun handle(request: HttpRequest<*>, exception: Exception): HttpResponse<Any> {
        logger.warn("Handling unexpected exception on http infra adapter: ", exception)
        return when (exception) {
            is IllegalArgumentException -> badRequestResponse(request, exception)
            is IllegalStateException -> conflictResponse(request, exception)
            else -> internalServerErrorResponse(request, exception)
        }
    }

}