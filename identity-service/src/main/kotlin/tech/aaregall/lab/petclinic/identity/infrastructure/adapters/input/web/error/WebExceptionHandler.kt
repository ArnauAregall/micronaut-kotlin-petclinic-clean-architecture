package tech.aaregall.lab.petclinic.identity.infrastructure.adapters.input.web.error

import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpResponse.badRequest
import io.micronaut.http.HttpResponse.serverError
import io.micronaut.http.annotation.Error
import io.micronaut.http.server.exceptions.ExceptionHandler
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory

@Singleton
class WebExceptionHandler: ExceptionHandler<Exception, HttpResponse<Any>> {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @Error(global = true, exception = Exception::class)
    override fun handle(request: HttpRequest<*>?, exception: Exception?): HttpResponse<Any> {
        logger.warn("Handling unexpected exception on web infra adapter: ", exception)
        return when (exception) {
            is IllegalArgumentException -> badRequest()
            else -> serverError()
        }
    }
}