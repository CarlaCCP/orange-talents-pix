package br.com.orange.chavePix.clients

import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.codec.MediaTypeCodec


@Client("http://localhost:8082")
interface ClientBCB {

    @Post(value = "/api/v1/pix/keys", produces = [MediaType.APPLICATION_XML], consumes = [MediaType.APPLICATION_XML])
    fun postaChavePix(@Body request: CreatePixKeyRequest) : HttpResponse<CreatePixKeyResponse>

    @Delete(value = "/api/v1/pix/keys/{key}", produces = [MediaType.APPLICATION_XML], consumes = [MediaType.APPLICATION_XML])
    fun deletaChavePixPorId(@PathVariable key: String, @Body request: DeletePixKeyRequest) : HttpResponse<DeletePixKeyResponse>

    @Get(value = "/api/v1/pix/keys/{key}", consumes = [MediaType.APPLICATION_XML], produces = [MediaType.APPLICATION_XML])
    fun buscaChavePixPorId(@PathVariable key: String) : HttpResponse<CreatePixKeyResponse>

    @Get(value = "/api/v1/pix/keys", consumes = [MediaType.APPLICATION_XML])
    fun buscaTodos() : HttpResponse<PixKeysListResponse>


}