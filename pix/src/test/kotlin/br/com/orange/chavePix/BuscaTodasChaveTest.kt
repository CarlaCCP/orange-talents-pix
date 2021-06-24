package br.com.orange.chavePix

import br.com.orange.BuscaTodasChavesRequest
import br.com.orange.PixServiceGrpc
import br.com.orange.TipoDeConta
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import java.util.*
import javax.transaction.Transactional

@MicronautTest(transactional = false)
class BuscaTodasChaveTest
    (
    val repository: ChavePixRepository,
    val grpcClient: PixServiceGrpc.PixServiceBlockingStub,
) {

    companion object {
        val CLIENTE_ID = UUID.randomUUID()
        val CHAVE_ALEATORIA = UUID.randomUUID()
    }

    @BeforeEach
    fun setUp() {
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

        val chavePix2 = ChavePix(
            tipo = br.com.orange.chavePix.TipoDeChave.ALEATORIA,
            chave = CHAVE_ALEATORIA.toString(),
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

        repository.save(chavePix2)
        repository.save(chavePix)
    }

    @AfterEach
    fun config() {
        repository.deleteAll()
    }

    @Test
    fun `nao deve buscar chave com cliente nulo`() {
        val throws = assertThrows<StatusRuntimeException> {
            grpcClient.listaChaves(
                BuscaTodasChavesRequest
                    .newBuilder()
                    .setClienteId("")
                    .build()
            )
        }

        with(throws) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Cliente ID não pode ser nulo ou vazio", status.description)
        }
    }


    @Test
    fun `consulta chaves- happy path `() {

        val response = grpcClient.listaChaves(
            BuscaTodasChavesRequest
                .newBuilder()
                .setClienteId("5260263c-a3c1-4727-ae32-3bdb2538841b")
                .build()
        )

        with(response) {
            assertEquals(this.clienteId, "5260263c-a3c1-4727-ae32-3bdb2538841b")
            assertTrue(this.chavesList.size == 2)
        }

    }

    @Test
    fun `traz lista vazia quando nao encontra cliente`(){
        repository.deleteAll()

        val response = grpcClient.listaChaves(
            BuscaTodasChavesRequest
                .newBuilder()
                .setClienteId("5260263c-a3c1-4727-ae32-3bdb2538841b")
                .build()
        )

        with(response) {
            assertEquals(this.clienteId, "5260263c-a3c1-4727-ae32-3bdb2538841b")
            println(chavesList.size)
            assertTrue(this.chavesList.size == 0)
        }


    }

}