package br.com.orange.chavePix

import br.com.orange.BuscaTodasChavesResponse
import io.micronaut.core.annotation.Introspected
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

@Introspected
class ListaChaveResponse(chavePix: BuscaTodasChavesResponse.ChavePix)
{
    val id = chavePix.pixId
    val chave = chavePix.chave
    val tipo = chavePix.tipoDeChave
    val tipoDeConta = chavePix.tipoDeConta
    val criadaEm = chavePix.criadoEm.let {
        LocalDateTime.ofInstant(Instant.ofEpochSecond(it.seconds, it.nanos.toLong()), ZoneOffset.UTC)
    }
}






