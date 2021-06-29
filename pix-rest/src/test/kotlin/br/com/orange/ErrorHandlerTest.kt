package br.com.orange

import br.com.orange.compartilhado.ErrorHandler
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.hateoas.JsonError
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class ErrorHandlerTest {

    val requestGenerica = HttpRequest.GET<Any>("/")


    @Test
    fun `deve retornar 404 quando statusException for not found `(){

        val mensagem = "Chave já cadastrada"
        val notFoundException = StatusRuntimeException(Status.NOT_FOUND
            .withDescription(mensagem))

        val resposta = ErrorHandler().handle(requestGenerica,notFoundException)

        assertEquals(HttpStatus.NOT_FOUND, resposta.status)
        assertNotNull(resposta.body())
        assertEquals(mensagem, (resposta.body() as JsonError).message)

    }

}