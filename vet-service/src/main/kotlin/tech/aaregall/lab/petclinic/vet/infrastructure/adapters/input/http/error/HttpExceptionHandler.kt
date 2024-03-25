package tech.aaregall.lab.petclinic.vet.infrastructure.adapters.input.http.error

import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Error
import io.micronaut.http.server.exceptions.ExceptionHandler
import jakarta.inject.Singleton
import tech.aaregall.lab.petclinic.common.error.CommonHttpExceptionHandler

@Singleton
internal class HttpExceptionHandler: ExceptionHandler<Exception, HttpResponse<Any>> {

    private val delegate = CommonHttpExceptionHandler()

    @Error(global = true, exception = Exception::class)
    override fun handle(request: HttpRequest<*>, exception: Exception): HttpResponse<Any> =
        delegate.handle(request, exception)

}