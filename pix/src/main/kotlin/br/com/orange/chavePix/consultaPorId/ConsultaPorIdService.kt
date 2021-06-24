package br.com.orange.chavePix.consultaPorId

import br.com.orange.chavePix.ChavePix
import br.com.orange.chavePix.ChavePixRepository
import br.com.orange.chavePix.clients.ClientBCB
import br.com.orange.chavePix.clients.ContasDeCliemtesItauClient
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientException
import io.micronaut.validation.Validated
import java.lang.IllegalStateException
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@Validated
@Singleton
class ConsultaPorIdService(
    @Inject val repository: ChavePixRepository,
    @Inject val itauClient: ContasDeCliemtesItauClient,
    @Inject val clientBCB: ClientBCB,
) {


    @Transactional
    fun consultaChave(clienteId: String?, pixId: String?, @Size(max = 77) chave: String?): ChavePix {

        /*
        * DOIS SISTEMAS, 1° logica abaixo- a chave precisa estar cadastrada no nosso banco
        * 2º Não precisa estar cadastrado no nosso banco, pode ser consultada diretamente no BCB
        *OBS: Ver primeiro se um parametro veio null
        * */
        // Chave deve pertencer ao cliente - consultar no banco se existe tal cliente para tal chave

        if (clienteId.isNullOrBlank() && pixId.isNullOrBlank() && chave.isNullOrBlank()) {
            return throw IllegalStateException("Preencha pelo menos um campo")
        }
        if (chave.isNullOrBlank() && !clienteId!!.isNullOrBlank() && !pixId.isNullOrBlank()) {

            val responseItau = itauClient.buscaClientePorId(UUID.fromString(clienteId))

            val possivelChavePix = repository.findById(UUID.fromString(pixId))

            if (possivelChavePix.isEmpty) {

                return throw StatusRuntimeException(
                    Status.NOT_FOUND
                        .withDescription("Chave não encontrada")
                )
            }

            if (responseItau.body() == null) {
                return throw StatusRuntimeException(
                    Status.NOT_FOUND
                        .withDescription("Cliente não encontrado")
                )
            }
            try {
                val response = clientBCB.buscaChavePixPorId(possivelChavePix.get().chave)

            } catch (e: Exception) {
                return throw HttpClientException("Chave pix não encontrada (BCB)")
            }
            return possivelChavePix.get()
        } else if (!chave.isNullOrBlank() && clienteId.isNullOrBlank() && pixId.isNullOrBlank()) {

            val possivelChavePixBCB = repository.findByChave(chave)

            if (possivelChavePixBCB.isEmpty) {
                try {
                    val response = clientBCB.buscaChavePixPorId(chave.toString())

                    val chavePix = response.body().toModel()
                    return chavePix
                } catch (e: Exception) {
                    return throw HttpClientException("Chave pix não encontrada (BCB)")
                }
            } else {
                return possivelChavePixBCB.get()
            }
        }

        return throw IllegalStateException(" Dados preenchidos incorretamente")
    }
}