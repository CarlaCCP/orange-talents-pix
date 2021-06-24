package br.com.orange.chavePix.remove

import br.com.orange.chavePix.ChavePixRepository
import br.com.orange.chavePix.clients.ClientBCB
import br.com.orange.chavePix.clients.ContasDeCliemtesItauClient
import br.com.orange.chavePix.clients.DeletePixKeyRequest
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.validation.Validated
import java.lang.IllegalStateException
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.constraints.NotBlank

@Validated
@Singleton
class RemoveChaveService(
    @Inject val clienteItau: ContasDeCliemtesItauClient,
    @Inject val repository: ChavePixRepository,
    @Inject val clientBCB: ClientBCB
) {


    @Transactional
    fun removeChave (@NotBlank clienteId: String?, @NotBlank pixId: String?){

        val clienteIdUuid = UUID.fromString(clienteId)
        val idPixUuid = UUID.fromString(pixId)

        val dadosCliente = clienteItau.buscaClientePorId(clienteIdUuid)
        println(dadosCliente.body())

        if(dadosCliente.body() == null){
            throw IllegalStateException("Cliente não encontrado")
        }


        if (!repository.existsById(idPixUuid)){
            println(repository.existsById(idPixUuid))
            throw StatusRuntimeException(Status.NOT_FOUND.withDescription("Chave não encontrada"))
        }
        val chaveCliente = repository.findById(idPixUuid).get()
        val deletePixKeyRequest = DeletePixKeyRequest(key = chaveCliente.chave, participant = "60701190")

        val responseKey = clientBCB.deletaChavePixPorId(chaveCliente.chave, deletePixKeyRequest)
        repository.deleteById(idPixUuid)
    }
}