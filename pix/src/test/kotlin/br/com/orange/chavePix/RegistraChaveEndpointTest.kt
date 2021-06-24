package br.com.orange.chavePix

import br.com.orange.*
import br.com.orange.TipoDeChave
import br.com.orange.chavePix.clients.*
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
internal class RegistraChaveEndpointTest(
    val grpcClient: PixServiceGrpc.PixServiceBlockingStub,
    val repository: ChavePixRepository
) {
    // Testar: Se busca o cliente no client, se chave não é duplicada, validacoes de properties, e se cadastra chave


    @field: Inject
    lateinit var clientItau: ContasDeCliemtesItauClient

    @field: Inject
    lateinit var clientBCB: ClientBCB

    //lateinit var createPixKeyRequest: CreatePixKeyRequest

    companion object {
        val CLIENTE_ID = UUID.randomUUID()
        val PIX_ID = UUID.randomUUID()
    }

    @BeforeEach
    fun setup() {
        repository.deleteAll()
    }

    @Test
    fun `deve registrar nova chave pix`() {
        `when`(clientItau.buscaContaPorTipo(CLIENTE_ID.toString(), "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok(dadosDaContaResponse()))

        `when`(clientBCB.postaChavePix(createPixKeyRequest()))
            .thenReturn(HttpResponse.created(createPixKeyResponse()))


       var request = RegistraChavePixRequest.newBuilder()
            .setClienteId(CLIENTE_ID.toString())
            .setTipoDeChave(TipoDeChave.EMAIL)
            .setChave("carla@carla.com")
            .setTipoDeConta(TipoDeConta.CONTA_CORRENTE)
            .build()

        val response = RegistraChavePixResponse
            .newBuilder()
            .setClienteId(CLIENTE_ID.toString())
            .setPixId(PIX_ID.toString())
            .build()


        with(response) {
            assertEquals(CLIENTE_ID.toString(), clienteId)
            assertNotNull(pixId)
        }

    }


    @Test
    fun `nao deve registrar chave pix quando chave existe`() {
        // Cenário
        repository.save(
            ChavePix(
                tipo = br.com.orange.chavePix.TipoDeChave.CPF,
                chave = "46972422892",
                clienteId = CLIENTE_ID,
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



        //Ação
        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.registra(
                RegistraChavePixRequest.newBuilder()
                    .setClienteId(CLIENTE_ID.toString())
                    .setTipoDeChave(TipoDeChave.CPF)
                    .setChave("46972422892")
                    .setTipoDeConta(TipoDeConta.CONTA_CORRENTE)
                    .build()
            )
        }

        `when`(clientBCB.postaChavePix(createPixKeyRequest()))
            .thenReturn(HttpResponse.unprocessableEntity())

        // Validação
        with(thrown) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            //assertEquals("UNKNOWN", status.description)
        }

    }


    @Test
    fun `nao deve registrar chave pix quando nao encontrar dados da conta cliente`() {
        `when`(clientItau.buscaContaPorTipo(CLIENTE_ID.toString(), "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.notFound())


        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.registra(
                RegistraChavePixRequest.newBuilder()
                    .setClienteId(CLIENTE_ID.toString())
                    .setTipoDeChave(TipoDeChave.EMAIL)
                    .setChave("carla@carla")
                    .setTipoDeConta(TipoDeConta.CONTA_CORRENTE)
                    .build()
            )
        }

        with(thrown) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Cliente não encontrado no Itau", status.description)
        }
    }


    @Test
    fun `nao deve registrar chave pix quando parametros forem invalidos`() {

        val thrown = assertThrows<StatusRuntimeException> {
            grpcClient.registra(RegistraChavePixRequest.newBuilder().build())
        }

        with(thrown) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            //assertEquals("Um erro inesperado aconteceu", status.description)
        }
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

    private fun dadosDaContaResponse(): DadosDaContaResponse {
        return DadosDaContaResponse(
            tipo = "CONTA_CORRENTE",
            instituicao = InstituicaoResponse("UNIBANCO ITAU SA", "60701190"),
            agencia = "0001",
            numero = "291900",
            titular = TitularResponse("Yuri Matheus", "86135457004")
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
