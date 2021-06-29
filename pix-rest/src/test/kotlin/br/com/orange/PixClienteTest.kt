package br.com.orange

import br.com.orange.chavePix.*
import com.google.protobuf.Timestamp
import io.micronaut.context.annotation.Replaces
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import org.junit.jupiter.api.Assertions.*
import java.time.LocalDateTime
import java.time.ZoneId

@MicronautTest
internal class PixClienteTest {


    companion object {
        val PIX_ID = UUID.randomUUID()
        val CLIENTE_ID = UUID.randomUUID()
    }

    @field: Inject
    lateinit var registraStub: PixServiceGrpc.PixServiceBlockingStub

    @field:Inject
    @field:Client("/")
    lateinit var client: HttpClient

    @Test
    fun `deve registrar uma nova chave pix`() {


        val registraRequest = RegistraRequest(
            clientId = "c56dfef4-7901-44fb-84e2-a2cefb157890",
            tipoDeChave = TipoDeChave.CPF,
            chave = "86135457004",
            tipoDeConta = TipoDeConta.CONTA_CORRENTE
        )

        val requestRegistra = RegistraChavePixRequest
            .newBuilder()
            .setChave(registraRequest.chave)
            .setTipoDeConta(registraRequest.tipoDeConta)
            .setClienteId(registraRequest.clientId)
            .setTipoDeChave(registraRequest.tipoDeChave)
            .build()


        val responseRegistra = RegistraChavePixResponse
            .newBuilder()
            .setClienteId(registraRequest.clientId)
            .setPixId(PIX_ID.toString())
            .build()

        // No lugar do requestRegistra - pode usar Mockito.any(), para ter mais controle
        //pois a classe requestRegistra é criada do grpc
        given(registraStub.registra(requestRegistra)).willReturn(responseRegistra)

        val request = HttpRequest.POST("/pix/registra", registraRequest)
        val response = client.toBlocking().exchange(request, RegistraResponse::class.java)


        assertEquals(HttpStatus.OK, response.status)
        assertEquals(response.body().clienteId, registraRequest.clientId)
    }

    @Test
    fun `deve remover uma chave`() {
        val removeRequest = RemoveRequest(
            clientId = "c56dfef4-7901-44fb-84e2-a2cefb157890",
            pixId = PIX_ID.toString(),
            cpf = "Teste",
            nome = "Teste"
        )

        val remove = RemoveChavePixRequest.newBuilder()
            .setClienteId(removeRequest.clientId)
            .setPixId(PIX_ID.toString())
            .setCpf("Teste")
            .setNome("Teste")
            .build()

        val removeResponse = RemoveChavePixResponse.newBuilder().setMensagem("Chave excluída com sucesso").build()
        given(registraStub.remove(remove)).willReturn(removeResponse)

        val request = HttpRequest.DELETE("/pix/remove", removeRequest)
        val response = client.toBlocking().exchange(request, RemoveResponse::class.java)

        assertEquals(HttpStatus.OK, response.status)
        assertEquals(response.body().mensagem, removeResponse.mensagem)
    }


    @Test
    fun `deve consultar chave por clienteId e PixId`() {


        val consultaRequestId = ConsultaIdRequest(
            clienteId = CLIENTE_ID.toString(),
            pixId = PIX_ID.toString(),
            chave = ""
        )


        val consultaRequestGrpc = BuscaPorIdRequest.newBuilder()
            .setClienteId(consultaRequestId.clienteId)
            .setPixId(consultaRequestId.pixId)
            .setChave("")
            .build()

        val consultaResponseGrpc = BuscaPorIdResponse.newBuilder()
            .setChave("12345678989")
            .setClienteId(CLIENTE_ID.toString())
            .setPixId(PIX_ID.toString())
            .setAgencia("0001")
            .setBanco("Itau")
            .setTipoDeChave(TipoDeChave.CPF.name)
            .setTipoDeConta(TipoDeConta.CONTA_CORRENTE)
            .setCpf("12345678989")
            .setNumero("7898656")
            .setNome("Carla")
            .setCriadoEm("19/01/2021")
            .build()

        val consultaIdResponse = ConsultaIdResponse(
            chave = consultaResponseGrpc.chave,
            clienteId = consultaResponseGrpc.clienteId,
            pixId = consultaResponseGrpc.pixId,
            agencia = consultaResponseGrpc.agencia,
            banco = consultaResponseGrpc.banco,
            tipoDeChave = consultaResponseGrpc.tipoDeChave,
            tipoDeConta = consultaResponseGrpc.tipoDeConta.name,
            cpf = consultaResponseGrpc.cpf,
            numero = consultaResponseGrpc.numero,
            nome = consultaResponseGrpc.nome,
            criadoEm = consultaResponseGrpc.criadoEm
        )

        given(registraStub.consultaPorId(consultaRequestGrpc)).willReturn(consultaResponseGrpc)

        val request = HttpRequest.POST("/pix/consulta", consultaRequestId)
        val response = client.toBlocking().exchange(request, ConsultaIdResponse::class.java)

        assertEquals(HttpStatus.OK, response.status)
    }


    @Test
    fun `deve consultar chave com a chave`() {

        val consultaRequestId = ConsultaIdRequest(
            clienteId = "",
            pixId = "",
            chave = "12345678989"
        )

        val consultaRequestGrpc = BuscaPorIdRequest.newBuilder()
            .setClienteId("")
            .setPixId("")
            .setChave(consultaRequestId.chave)
            .build()

        val consultaResponseGrpc = BuscaPorIdResponse.newBuilder()
            .setChave("12345678989")
            .setClienteId("")
            .setPixId("")
            .setAgencia("0001")
            .setBanco("Itau")
            .setTipoDeChave(TipoDeChave.CPF.name)
            .setTipoDeConta(TipoDeConta.CONTA_CORRENTE)
            .setCpf("12345678989")
            .setNumero("7898656")
            .setNome("Carla")
            .setCriadoEm("19/01/2021")
            .build()

        given(registraStub.consultaPorId(consultaRequestGrpc)).willReturn(consultaResponseGrpc)

        val request = HttpRequest.POST("/pix/consulta", consultaRequestId)
        val response = client.toBlocking().exchange(request, ConsultaIdResponse::class.java)

        assertEquals(HttpStatus.OK, response.status)

    }

    @Test
    fun `deve listar chaves`() {
        val clienteId = CLIENTE_ID.toString()

        val listaRequest = BuscaTodasChavesRequest.newBuilder().setClienteId(clienteId).build()

        val respostaGrpc = listaChavePixResponse(CLIENTE_ID.toString())

        given(registraStub.listaChaves(Mockito.any())).willReturn(respostaGrpc)

        val requestURI = HttpRequest.GET<Any>("/pix/listaChave/$clienteId")
        val responseURI = client.toBlocking().exchange(requestURI, Any::class.java)

        assertEquals(HttpStatus.OK, responseURI.status)

    }


    private fun listaChavePixResponse(clienteId: String) : BuscaTodasChavesResponse {
        val chaveUm = BuscaTodasChavesResponse.ChavePix.newBuilder()
            .setPixId(UUID.randomUUID().toString())
            .setTipoDeChave(TipoDeChave.CPF)
            .setChave("12345678998")
            .setTipoDeConta(TipoDeConta.CONTA_CORRENTE)
            .setCriadoEm(LocalDateTime.now().let {
                val createdAt = it.atZone(ZoneId.of("UTC")).toInstant()
                Timestamp.newBuilder()
                    .setSeconds(createdAt.epochSecond)
                    .setNanos(createdAt.nano)
                    .build()
            })
            .build()


        val chaveDois = BuscaTodasChavesResponse.ChavePix.newBuilder()
            .setPixId(UUID.randomUUID().toString())
            .setTipoDeChave(TipoDeChave.ALEATORIA)
            .setChave("")
            .setTipoDeConta(TipoDeConta.CONTA_CORRENTE)
            .setCriadoEm(LocalDateTime.now().let {
                val createdAt = it.atZone(ZoneId.of("UTC")).toInstant()
                Timestamp.newBuilder()
                    .setSeconds(createdAt.epochSecond)
                    .setNanos(createdAt.nano)
                    .build()
            })
            .build()

        return BuscaTodasChavesResponse.newBuilder()
            .setClienteId(clienteId)
            .addAllChaves(listOf(chaveUm, chaveDois))
            .build()
    }


    @Replaces(bean = PixServiceGrpc.PixServiceBlockingStub::class)
    @Singleton
    fun stubMock() = Mockito.mock(PixServiceGrpc.PixServiceBlockingStub::class.java)


}