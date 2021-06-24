package br.com.orange.chavePix

import br.com.orange.*
import br.com.orange.chavePix.compartilhado.ErrorAdvice
import br.com.orange.chavePix.consultaPorId.ConsultaPorIdService
import br.com.orange.chavePix.registra.NovaChavePix
import br.com.orange.chavePix.registra.NovaChavePixService
import br.com.orange.chavePix.remove.RemoveChaveService
import com.google.protobuf.Timestamp
import io.grpc.Status

import io.grpc.stub.StreamObserver
import java.lang.IllegalStateException
import java.time.ZoneId
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@ErrorAdvice
class ChavePixEndpoint(
    @Inject private val service: NovaChavePixService,
    @Inject var removeService: RemoveChaveService,
    @Inject var consultaPorIdService: ConsultaPorIdService,
    @Inject var repository: ChavePixRepository
) : PixServiceGrpc.PixServiceImplBase() {

    override fun registra(
        request: RegistraChavePixRequest,
        responseObserver: StreamObserver<RegistraChavePixResponse>
    ) {
        val novaChave = request.toModel()
        val chaveCriada = service.registra(novaChave)


        val response = RegistraChavePixResponse
            .newBuilder()
            .setClienteId(chaveCriada.clienteId.toString())
            .setPixId(chaveCriada.id.toString())
            .build()

        println("PIX id: ${response.pixId}")
        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }


    override fun remove(
        request: RemoveChavePixRequest,
        responseObserver: StreamObserver<RemoveChavePixResponse>
    ) {


        removeService.removeChave(request.clienteId, request.pixId)
        val response = RemoveChavePixResponse.newBuilder()
            .setMensagem("Chave excluída com sucesso")
            .build()

        responseObserver.onNext(response)
        responseObserver.onCompleted()

    }


    override fun consultaPorId(
        request: BuscaPorIdRequest?,
        responseObserver: StreamObserver<BuscaPorIdResponse>?
    ) {
        val chavePix = consultaPorIdService.consultaChave(request?.clienteId, request?.pixId, request?.chave)

        if (request!!.clienteId.isNullOrBlank()) {
            val response = BuscaPorIdResponse.newBuilder()
                .setClienteId("null")
                .setPixId(chavePix.id.toString())
                .setTipoDeChave(chavePix.tipo.name)
                .setChave(chavePix.chave)
                .setTipoDeConta(chavePix.tipoDeConta)
                .setNome(chavePix.conta.nomeDoTitular)
                .setCpf(chavePix.conta.cpfDoTitular)
                .setBanco(chavePix.conta.instituicao)
                .setAgencia(chavePix.conta.agencia)
                .setNumero(chavePix.conta.numeroDaConta)
                .setCriadoEm(chavePix.criada.toString())
                .build()

            responseObserver?.onNext(response)
            responseObserver?.onCompleted()
        } else {
            val response = BuscaPorIdResponse.newBuilder()
                .setClienteId(chavePix.clienteId.toString())
                .setPixId(chavePix.id.toString())
                .setTipoDeChave(chavePix.tipo.name)
                .setChave(chavePix.chave)
                .setTipoDeConta(chavePix.tipoDeConta)
                .setNome(chavePix.conta.nomeDoTitular)
                .setCpf(chavePix.conta.cpfDoTitular)
                .setBanco(chavePix.conta.instituicao)
                .setAgencia(chavePix.conta.agencia)
                .setNumero(chavePix.conta.numeroDaConta)
                .setCriadoEm(chavePix.criada.toString())
                .build()

            responseObserver?.onNext(response)
            responseObserver?.onCompleted()
        }


    }


    override fun listaChaves(
        request: BuscaTodasChavesRequest,
        responseObserver: StreamObserver<BuscaTodasChavesResponse>
    ) {

        if (request.clienteId.isNullOrBlank()) // 1
            throw IllegalArgumentException("Cliente ID não pode ser nulo ou vazio")

        val listaDeChaves = repository.findAllByClienteId(UUID.fromString(request.clienteId)).map {
            BuscaTodasChavesResponse.ChavePix.newBuilder()
                .setPixId(it.id.toString())
                .setTipoDeChave(br.com.orange.TipoDeChave.valueOf(it.tipo.name))
                .setChave(it.chave)
                .setTipoDeConta(TipoDeConta.valueOf(it.tipoDeConta.name))
                .setCriadoEm(it.criada.let {
                    val createdAt = it.atZone(ZoneId.of("UTC")).toInstant()
                    Timestamp.newBuilder()
                        .setSeconds(createdAt.epochSecond)
                        .setNanos(createdAt.nano)
                        .build()
                })
                .build()
        }

        responseObserver.onNext(
            BuscaTodasChavesResponse.newBuilder()
                .setClienteId(request.clienteId.toString())
                .addAllChaves(listaDeChaves)
                .build()
        )

        responseObserver.onCompleted()

    }

}

fun RegistraChavePixRequest.toModel(): NovaChavePix {
    return NovaChavePix(
        clienteId = clienteId,
        tipo = when (tipoDeChave) {
            br.com.orange.TipoDeChave.DESCONHECIDO_CHAVE -> null
            else -> TipoDeChave.valueOf(tipoDeChave.name)
        },
        chave = chave,
        tipoDeConta = when (tipoDeConta) {
            TipoDeConta.NENHUM_TIPO -> null
            else -> TipoDeConta.valueOf(tipoDeConta.name)
        }
    )
}