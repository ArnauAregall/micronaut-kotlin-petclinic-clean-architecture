package tech.aaregall.lab.petclinic.common.error

import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.hateoas.JsonError
import io.micronaut.http.hateoas.Link
import org.slf4j.LoggerFactory

class CommonHttpExceptionHandler {

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)

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

    fun handle(request: HttpRequest<*>, exception: Exception): HttpResponse<Any> =
        when (exception) {
            is IllegalArgumentException -> badRequestResponse(request, exception)
            is IllegalStateException -> conflictResponse(request, exception)
            else -> internalServerErrorResponse(request, exception)
        }.also {
            logger.warn("Handled HTTP exception [request='${request.uri}', responseStatus='${it.status.code} ${it.status.reason}']", exception)
        }

}