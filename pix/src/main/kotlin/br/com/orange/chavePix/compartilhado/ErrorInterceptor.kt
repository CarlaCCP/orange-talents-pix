package br.com.orange.chavePix.compartilhado

import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.grpc.stub.StreamObserver
import io.micronaut.aop.InterceptorBean
import io.micronaut.aop.MethodInterceptor
import io.micronaut.aop.MethodInvocationContext
import io.micronaut.http.client.exceptions.HttpClientException
import io.micronaut.http.client.exceptions.HttpClientResponseException
import java.lang.Exception
import java.lang.IllegalStateException
import javax.inject.Singleton
import javax.validation.ConstraintViolationException

@Singleton
@InterceptorBean(ErrorAdvice::class)
class ErrorInterceptor : MethodInterceptor<Any, Any> {


    override fun intercept(context: MethodInvocationContext<Any, Any>): Any {
        return try {
            context.proceed()
        } catch (e: Exception) {
            val responseObserver = context.parameterValues[1] as StreamObserver<*>
            val status = when (e) {


                is IllegalStateException -> Status.INVALID_ARGUMENT
                    .withDescription(e.message)

                is StatusRuntimeException -> Status.NOT_FOUND
                    .withDescription(e.message)

                is ConstraintViolationException -> Status.INVALID_ARGUMENT
                    .withDescription(e.message)

                is HttpClientException -> Status
                    .NOT_FOUND
                    .withDescription(e.message)

                is IllegalArgumentException -> Status.INVALID_ARGUMENT.withDescription(e.message)
                else -> Status.UNKNOWN
                    .withCause(e)
                    .withDescription("Um erro inesperado aconteceu")
            }

            val statusRuntime = StatusRuntimeException(status)
            responseObserver.onError(statusRuntime)
        }
    }
}