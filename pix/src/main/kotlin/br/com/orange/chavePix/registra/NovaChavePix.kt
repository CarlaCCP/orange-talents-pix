package br.com.orange.chavePix.registra


import br.com.orange.TipoDeConta
import br.com.orange.chavePix.ChavePix
import br.com.orange.chavePix.ContaAssociada
import br.com.orange.chavePix.TipoDeChave
import br.com.orange.chavePix.ValidPixKey
import br.com.orange.chavePix.clients.*
import br.com.orange.chavePix.compartilhado.ValidUUID
import io.micronaut.core.annotation.Introspected
import java.security.Key
import java.util.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@ValidPixKey
@Introspected
data class NovaChavePix(

    @ValidUUID
    @field: NotBlank val clienteId: String?,
    @field: NotNull val tipo: TipoDeChave?,
    @field: Size (max = 77)var chave: String?,
    @field: NotNull val tipoDeConta: TipoDeConta?

)
{

    fun toModel(conta: ContaAssociada) : ChavePix {
        return ChavePix(
            clienteId = UUID.fromString(this.clienteId),
            tipo = TipoDeChave.valueOf(this.tipo!!.name),
            chave = if (this.tipo == TipoDeChave.ALEATORIA) UUID.randomUUID().toString() else this.chave!!,
            tipoDeConta = TipoDeConta.valueOf(this.tipoDeConta!!.name),
            conta = conta
        )
    }

    fun atualiza (chave: ChavePix, response: CreatePixKeyResponse){
        println("Chave antes: ${chave.chave}")
        chave.chave = response.key
        println("Chave depois: ${chave.chave}")
    }

    fun toRequestPix (conta: ContaAssociada) : CreatePixKeyRequest{
        return CreatePixKeyRequest(
           keyType =
           when (this.tipo) {
               TipoDeChave.CPF -> {
                   KeyType.CPF
               }
               TipoDeChave.EMAIL -> {
                   KeyType.EMAIL
               }
               TipoDeChave.ALEATORIA -> {
                   KeyType.RANDOM
               }

               else -> KeyType.PHONE
           },
            key = this.chave!!,
            bankAccount = BankAccount(
                participant = "60701190",
                branch = conta.agencia,
                accountNumber = conta.numeroDaConta,
                accountType = when (tipoDeConta) {
                    TipoDeConta.CONTA_CORRENTE -> {
                        AccountType.CACC
                    }
                    else -> AccountType.SVGS
                }
            ),
            owner = OwnerBank(
                type = TypePerson.NATURAL_PERSON,
                name = conta.nomeDoTitular,
                taxIdNumber = conta.cpfDoTitular
            )
        )
    }

}
