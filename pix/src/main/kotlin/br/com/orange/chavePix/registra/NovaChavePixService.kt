package br.com.orange.chavePix.registra

import br.com.orange.TipoDeConta
import br.com.orange.chavePix.ChavePix
import br.com.orange.chavePix.ChavePixRepository
import br.com.orange.chavePix.TipoDeChave
import br.com.orange.chavePix.clients.*
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Body
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.validation.Validated
import java.lang.Exception
import java.lang.IllegalStateException
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Valid

@Validated
@Singleton
class NovaChavePixService(
    @Inject val repository: ChavePixRepository,
    @Inject val itauClient: ContasDeCliemtesItauClient,
    @Inject val clientBCB: ClientBCB,

    ) {

    @Transactional
    fun registra(@Valid novaChave: NovaChavePix): ChavePix {


        //Verifica se a chave já existe
        if (repository.existsByChave(novaChave.chave)) {
            throw StatusRuntimeException(Status.ALREADY_EXISTS.withDescription("Chave já cadastrada"))
        }

        // busca dados da conta
        val response = itauClient.buscaContaPorTipo(novaChave.clienteId!!, novaChave.tipoDeConta!!.name)
        val conta = response.body()?.toModel() ?: throw IllegalStateException("Cliente não encontrado no Itau")

        val chave = novaChave.toModel(conta)
        //Converte uma novaChave na request para o banco BCB
        val createPixKeyRequest = novaChave.toRequestPix(conta)

        val responseClient = clientBCB.postaChavePix(createPixKeyRequest)
        novaChave.atualiza(chave, responseClient.body()!!)


        // Atualizar a chave que retornará do BCB
        repository.save(chave)
        return chave
    }

}
