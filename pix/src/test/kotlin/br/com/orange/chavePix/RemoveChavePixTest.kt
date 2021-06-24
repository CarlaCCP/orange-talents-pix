package br.com.orange.chavePix

import br.com.orange.PixServiceGrpc
import br.com.orange.RemoveChavePixRequest
import br.com.orange.RemoveChavePixResponse
import br.com.orange.TipoDeConta
import br.com.orange.chavePix.clients.*
import br.com.orange.chavePix.remove.DadosDoClienteResponse
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject

@MicronautTest(transactional = false)
internal class RemoveChavePixTest(
    val grpcClient: PixServiceGrpc.PixServiceBlockingStub,
    val repository: ChavePixRepository
) {
    @field: Inject
    lateinit var clientItau: ContasDeCliemtesItauClient

    @field: Inject
    lateinit var clientBCB: ClientBCB

    companion object {
        val CLIENTE_ID = UUID.randomUUID()
    }

    @BeforeEach
    fun setup() {
        repository.deleteAll()
    }

    /*
    * 1. Cliente não autenticado
    * 2. Chave pix não encontrada
    * 3. ConstraintViolationException
    * 4. Cliente excluído com sucesso
    * */


    @Test
    fun `nao deve remover quando cliente não encontrado`() {

        `when`(clientItau.buscaClientePorId(CLIENTE_ID))
            .thenReturn(HttpResponse.notFound())

        val throws = assertThrows<StatusRuntimeException> {
            grpcClient.remove(
                RemoveChavePixRequest
                    .newBuilder()
                    .setClienteId(CLIENTE_ID.toString())
                    .setPixId("3c5c78ae-141b-4154-a485-98cbc3691686")
                    .setCpf("Teste")
                    .setNome("Teste")
                    .build()
            )
        }



        with(throws) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Cliente não encontrado", status.description)
        }


    }

    @Test
    fun `nao deve remover chave nao encontrada`() {

        val request = RemoveChavePixRequest.newBuilder()
            .setClienteId(CLIENTE_ID.toString())
            .setPixId("339449f9-9148-4fe4-925b-fb68aa361803")
            .setNome("Teste")
            .setCpf("Teste")
            .build()

        val clietItau = clientItau.buscaClientePorId(UUID.fromString(request.clienteId))

        with(clietItau) {
            assertFalse(repository.existsById(CLIENTE_ID))
        }
    }


    @Test
    fun `deve remover chave`() {
        // Cenario - cadastra chave
        val chavePix = ChavePix(
            tipo = br.com.orange.chavePix.TipoDeChave.CPF,
            chave = "46972422892",
            clienteId = RegistraChaveEndpointTest.CLIENTE_ID,
            tipoDeConta = TipoDeConta.CONTA_CORRENTE,
            conta = ContaAssociada(
                agencia = "0001",
                cpfDoTitular = "46972422892",
                instituicao = "UNIBANCO ITAU SA",
                nomeDoTitular = "Carla",
                numeroDaConta = "291900"
            )
        )
        repository.save(chavePix)

        `when`(clientBCB.postaChavePix(createPixKeyRequest()))
            .thenReturn(HttpResponse.created(createPixKeyResponse()))


        `when`(clientBCB.deletaChavePixPorId(chavePix.chave, deletePixKeyRequest()))
            .thenReturn(HttpResponse.ok(deletePixKeyResponse()))

        // Acao
        repository.deleteById(chavePix.id)

        RemoveChavePixResponse.newBuilder()
            .setMensagem("Chave excluida com sucesso").build()
        // Validacao
        assertFalse(repository.existsById(chavePix.id!!))


    }

    @Test
    fun `nao deve remover chave quando request invalido`() {

        `when`(clientItau.buscaClientePorId(CLIENTE_ID))
            .thenReturn(HttpResponse.ok(dadosDoClienteResponse()))


        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.remove(
                RemoveChavePixRequest
                    .newBuilder()
                    .build()
            )
        }

        with(thrown) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
        }

    }

    @Test
    fun `nao deve excluir chave antes de excluir no client bcb`() {

        val chavePix = repository.save(
            ChavePix(
                tipo = br.com.orange.chavePix.TipoDeChave.CPF,
                chave = "46972422892",
                clienteId = RegistraChaveEndpointTest.CLIENTE_ID,
                tipoDeConta = TipoDeConta.CONTA_CORRENTE,
                conta = ContaAssociada(
                    agencia = "0001",
                    cpfDoTitular = "46972422892",
                    instituicao = "UNIBANCO ITAU SA",
                    nomeDoTitular = "Carla",
                    numeroDaConta = "291900"
                )
            )
        )

        `when`(clientItau.buscaClientePorId(CLIENTE_ID))
            .thenReturn(HttpResponse.ok(dadosDoClienteResponse()))

        `when`(clientBCB.postaChavePix(createPixKeyRequest()))
            .thenReturn(HttpResponse.created(createPixKeyResponse()))


        `when`(clientBCB.deletaChavePixPorId(chavePix.chave, deletePixKeyRequest()))
            .thenReturn(HttpResponse.notFound())


        assertTrue(repository.existsById(chavePix.id!!))

    }


    @MockBean(ContasDeCliemtesItauClient::class)
    fun clienteMock(): ContasDeCliemtesItauClient {
        return Mockito.mock(ContasDeCliemtesItauClient::class.java)
    }

    @MockBean(ClientBCB::class)
    fun clienteBCBMock(): ClientBCB {
        return Mockito.mock(ClientBCB::class.java)
    }

    @Factory
    class Clients {
        @Bean
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel)
                : PixServiceGrpc.PixServiceBlockingStub {
            return PixServiceGrpc.newBlockingStub(channel)
        }
    }

    private fun deletePixKeyRequest(): DeletePixKeyRequest {
        return DeletePixKeyRequest(
            "teste",
            "Testando-falha"
        )
    }

    private fun deletePixKeyResponse() : DeletePixKeyResponse{
        return DeletePixKeyResponse(
            "46972422892",
            "60701190",
            LocalDateTime.now()
        )
    }

    private fun dadosDoClienteResponse(): DadosDoClienteResponse {
        return DadosDoClienteResponse(
            id = "0d1bb194-3c52-4e67-8c35-a93c0af9284f",
            nome = "Alberto Tavares",
            cpf = "06628726061",
            instituicao = InstituicaoResponse("UNIBANCO ITAU SA", "60701190")
        )
    }

    private fun createPixKeyRequest(): CreatePixKeyRequest {
        return CreatePixKeyRequest(
            keyType = KeyType.EMAIL,
            key = "carla@carla.com",
            bankAccount = BankAccount(
                participant = "60701190",
                branch = "0001",
                accountType = AccountType.CACC,
                accountNumber = "291900"
            ),
            owner = OwnerBank(
                type = TypePerson.NATURAL_PERSON,
                name = "Yuri Matheus",
                taxIdNumber = "86135457004"
            ),
            createdAt = LocalDateTime.now()
        )
    }

    private fun createPixKeyResponse(): CreatePixKeyResponse {
        return CreatePixKeyResponse(
            keyType = KeyTypeResponse.EMAIL,
            key = "carla@carla.com",
            bankAccount = BankAccountResponse(
                participant = "60701190",
                branch = "0001",
                accountType = AccountTypeResponse.CACC,
                accountNumber = "291900"
            ),
            owner = OwnerBankResponse(
                type = TypePersonResponse.NATURAL_PERSON,
                name = "Yuri Matheus",
                taxIdNumber = "86135457004"
            ),
            createdAt = LocalDateTime.now()
        )
    }
}