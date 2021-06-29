package br.com.orange.chavePix

import br.com.orange.*
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.*
import io.micronaut.validation.Validated

import javax.inject.Inject
import javax.validation.Valid

@Controller("/pix")
@Validated
class PixClientController(@Inject val grpcClient: PixServiceGrpc.PixServiceBlockingStub) {

    @Post("/registra")
    fun registraPix(@Valid @Body registraRequest: RegistraRequest): HttpResponse<RegistraResponse> {

        try {
            val response = grpcClient.registra(
                RegistraChavePixRequest
                    .newBuilder()
                    .setClienteId(registraRequest.clientId)
                    .setTipoDeChave(registraRequest.tipoDeChave)
                    .setChave(registraRequest.chave)
                    .setTipoDeConta(registraRequest.tipoDeConta)
                    .build()
            )
            return HttpResponse.ok(RegistraResponse(response.clienteId, response.pixId))
        } catch (e: StatusRuntimeException) {

            if (e.status.code == Status.NOT_FOUND.code) {
                return HttpResponse.unprocessableEntity()
            }
        }

        return HttpResponse.badRequest()
    }

    @Delete("/remove")
    fun removePix(@Valid @Body removeRequest: RemoveRequest): HttpResponse<RemoveResponse> {
        val response = grpcClient.remove(
            RemoveChavePixRequest
                .newBuilder()
                .setClienteId(removeRequest.clientId)
                .setPixId(removeRequest.pixId)
                .setNome(removeRequest.nome)
                .setCpf(removeRequest.cpf)
                .build()
        )

        return HttpResponse.ok(RemoveResponse(mensagem = "Chave excluída com sucesso"))

    }

    @Post("/consulta")
    fun consulta(@Valid @Body consultaIdRequest: ConsultaIdRequest): HttpResponse<ConsultaIdResponse> {

        if (consultaIdRequest.chave.isNullOrBlank()) {
            val response = grpcClient.consultaPorId(
                BuscaPorIdRequest
                    .newBuilder()
                    .setPixId(consultaIdRequest.pixId)
                    .setClienteId(consultaIdRequest.clienteId)
                    .setChave("")
                    .build()
            )

            val consultaIdResponse = ConsultaIdResponse(
                clienteId = response.clienteId,
                pixId = response.pixId,
                tipoDeChave = response.tipoDeChave,
                chave = response.chave,
                tipoDeConta = response.tipoDeConta.name,
                nome = response.nome,
                cpf = response.cpf,
                banco = response.banco,
                agencia = response.agencia,
                numero = response.numero,
                criadoEm = response.criadoEm
            )

            return HttpResponse.ok(consultaIdResponse)
        }

        if (consultaIdRequest.clienteId.isNullOrBlank() && consultaIdRequest.pixId.isNullOrBlank()){

            val response = grpcClient.consultaPorId(
                BuscaPorIdRequest
                    .newBuilder()
                    .setPixId("")
                    .setClienteId("")
                    .setChave(consultaIdRequest.chave)
                    .build()
            )

            val consultaIdResponse = ConsultaIdResponse(
                clienteId = response.clienteId,
                pixId = response.pixId,
                tipoDeChave = response.tipoDeChave,
                chave = response.chave,
                tipoDeConta = response.tipoDeConta.name,
                nome = response.nome,
                cpf = response.cpf,
                banco = response.banco,
                agencia = response.agencia,
                numero = response.numero,
                criadoEm = response.criadoEm
            )

            return HttpResponse.ok(consultaIdResponse)

        }

        return HttpResponse.unprocessableEntity()

    }

    @Get("/listaChave/{clienteId}")
    fun listaChaves(@PathVariable clienteId: String) : HttpResponse<Any>{

        val response = grpcClient.listaChaves(
            BuscaTodasChavesRequest
                .newBuilder()
                .setClienteId(clienteId)
                .build()
        )


        val chaves = response.chavesList.map{
            ListaChaveResponse(it)
        }

        return HttpResponse.ok(chaves)

    }

}

class RemoveResponse(
    val mensagem: String
)

