package br.com.orange.chavePix

import br.com.orange.BuscaPorIdRequest
import br.com.orange.PixServiceGrpc
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
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import javax.inject.Inject
import org.junit.jupiter.api.Assertions.*
import org.mockito.Mockito.`when`
import java.time.LocalDateTime
import java.util.*

@MicronautTest
class ConsultaPorIdTest(
    val repository: ChavePixRepository,
    val grpcClient: PixServiceGrpc.PixServiceBlockingStub,

    ) {

    @field: Inject
    lateinit var clientItau: ContasDeCliemtesItauClient

    @field: Inject
    lateinit var clientBCB: ClientBCB

    companion object {
        val CLIENTE_ID = UUID.randomUUID()
        val PIX_ID = UUID.randomUUID()
    }

    @BeforeEach
    fun setUp() {
        repository.deleteAll()
    }


    @Test
    fun `nao consulta dados com parametros nulos `() {
        val throws = assertThrows<StatusRuntimeException> {
            grpcClient.consultaPorId(
                BuscaPorIdRequest
                    .newBuilder()
                    .setChave("")
                    .setClienteId("")
                    .setPixId("")
                    .build()
            )
        }

        with(throws) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Preencha pelo menos um campo", status.description)
        }
    }

    @Test
    fun `nao cunsulta quando chave nao encontrada`() {

        val chavePix = ChavePix(
            tipo = br.com.orange.chavePix.TipoDeChave.EMAIL,
            chave = "carla@carla.com",
            clienteId = UUID.fromString("5260263c-a3c1-4727-ae32-3bdb2538841b"),
            tipoDeConta = TipoDeConta.CONTA_CORRENTE,
            conta = ContaAssociada(
                agencia = "0001",
                cpfDoTitular = "86135457004",
                instituicao = "UNIBANCO ITAU SA",
                nomeDoTitular = "Yuri Matheus",
                numeroDaConta = "60701190"
            )
        )

        repository.save(chavePix)


        val throws = assertThrows<StatusRuntimeException> {
            grpcClient.consultaPorId(
                BuscaPorIdRequest
                    .newBuilder()
                    .setChave("")
                    .setClienteId("5260263c-a3c1-4727-ae32-3bdb2538841b")
                    .setPixId(CLIENTE_ID.toString())
                    .build()
            )
        }

        `when`(clientItau.buscaClientePorId(UUID.fromString("5260263c-a3c1-4727-ae32-3bdb2538841b")))
            .thenReturn(HttpResponse.ok(dadosDoClienteResponse()))

        with(throws) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("NOT_FOUND: Chave não encontrada", status.description)
        }
    }

    @Test
    fun `nao consulta quando cliente nao encontrado`() {

        val chavePix = ChavePix(
            tipo = br.com.orange.chavePix.TipoDeChave.EMAIL,
            chave = "carla@carla.com",
            clienteId = UUID.fromString("5260263c-a3c1-4727-ae32-3bdb2538841b"),
            tipoDeConta = TipoDeConta.CONTA_CORRENTE,
            conta = ContaAssociada(
                agencia = "0001",
                cpfDoTitular = "86135457004",
                instituicao = "UNIBANCO ITAU SA",
                nomeDoTitular = "Yuri Matheus",
                numeroDaConta = "60701190"
            )
        )

        repository.save(chavePix)

        println(chavePix.id)

        val throws = assertThrows<StatusRuntimeException> {
            grpcClient.consultaPorId(
                BuscaPorIdRequest
                    .newBuilder()
                    .setChave("")
                    .setPixId(chavePix.id.toString())
                    .setClienteId(CLIENTE_ID.toString())
                    .build()
            )
        }

        `when`(clientItau.buscaClientePorId(CLIENTE_ID))
            .thenReturn(HttpResponse.notFound(null))


        with(throws) {
            assertEquals(io.grpc.Status.NOT_FOUND.code, status.code)
            //assertEquals("NOT_FOUND: Cliente não encontrado", status.description)
        }

    }

    @Test
    fun `nao consulta quando a chave nao esta no BCB`() {
        val chavePix = ChavePix(
            tipo = br.com.orange.chavePix.TipoDeChave.EMAIL,
            chave = "carla@carla.com",
            clienteId = UUID.fromString("5260263c-a3c1-4727-ae32-3bdb2538841b"),
            tipoDeConta = TipoDeConta.CONTA_CORRENTE,
            conta = ContaAssociada(
                agencia = "0001",
                cpfDoTitular = "86135457004",
                instituicao = "UNIBANCO ITAU SA",
                nomeDoTitular = "Yuri Matheus",
                numeroDaConta = "60701190"
            )
        )

        repository.save(chavePix)

        `when`(clientBCB.postaChavePix(createPixKeyRequest()))
            .thenReturn(HttpResponse.created(createPixKeyResponse()))


        val throws = assertThrows<StatusRuntimeException> {
            grpcClient.consultaPorId(
                BuscaPorIdRequest
                    .newBuilder()
                    .setChave("")
                    .setPixId(chavePix.id.toString())
                    .setClienteId("5260263c-a3c1-4727-ae32-3bdb2538841b")
                    .build()
            )
        }


        `when`(clientBCB.buscaChavePixPorId("5260263c-a3c1-4727-ae32-3bdb2538841b"))
            .thenReturn(HttpResponse.notFound())

        with(throws){
            assertEquals(Status.NOT_FOUND.code, status.code)
            //assertEquals("Cliente não encontrado", status.description)
        }

    }

    @Test
    fun `nao consulta dados preenchidos incorretamente` (){
        val throws = assertThrows<StatusRuntimeException> {
            grpcClient.consultaPorId(
                BuscaPorIdRequest
                    .newBuilder()
                    .setChave("")
                    .setPixId("")
                    .setClienteId("5260263c-a3c1-4727-ae32-3bdb2538841b")
                    .build()
            )
        }

        with(throws){
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals(" Dados preenchidos incorretamente", status.description)
        }
    }

    @Test
    fun `consulta no BCB quando nao existe chave no banco, retorna erro quando nao encontrada `(){

        val throws = assertThrows<StatusRuntimeException> {
            grpcClient.consultaPorId(
                BuscaPorIdRequest
                    .newBuilder()
                    .setChave("carla@carla")
                    .setPixId("")
                    .setClienteId("")
                    .build()
            )
        }



        with(throws){
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave pix não encontrada (BCB)", status.description)
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

    private fun dadosDoClienteResponse(): DadosDoClienteResponse {
        return DadosDoClienteResponse(
            id = "5260263c-a3c1-4727-ae32-3bdb2538841b",
            nome = "Yuri Matheus",
            cpf = "86135457004",
            instituicao = InstituicaoResponse("UNIBANCO ITAU SA", "60701190")
        )
    }
}