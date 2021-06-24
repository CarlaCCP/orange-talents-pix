package br.com.orange.chavePix.clients

import br.com.orange.chavePix.DadosDaContaResponse
import br.com.orange.chavePix.remove.DadosDoClienteResponse
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client
import java.util.*


@Client("http://localhost:9091")
interface ContasDeCliemtesItauClient {

    @Get("/api/v1/clientes/{clienteId}/contas{?tipo}")
    fun buscaContaPorTipo(@PathVariable clienteId: String, @QueryValue tipo: String): HttpResponse<DadosDaContaResponse>

    @Get("/api/v1/clientes/{clienteId}")
    fun buscaClientePorId(@PathVariable clienteId: UUID) : HttpResponse<DadosDoClienteResponse>

}
