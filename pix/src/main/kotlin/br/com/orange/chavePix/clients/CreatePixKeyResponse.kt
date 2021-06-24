package br.com.orange.chavePix.clients

import br.com.orange.TipoDeConta
import br.com.orange.chavePix.ChavePix
import br.com.orange.chavePix.ContaAssociada
import br.com.orange.chavePix.TipoDeChave
import java.time.LocalDateTime
import java.util.*

// Repensar essa classe
class CreatePixKeyResponse(
    var keyType: KeyTypeResponse,
    var key: String,
    var bankAccount: BankAccountResponse,
    var owner: OwnerBankResponse,
    var createdAt: LocalDateTime,

) {

    fun toModel() : ChavePix{
        return ChavePix(
            clienteId = UUID.randomUUID(),
            tipo = when (this.keyType) {
                KeyTypeResponse.CPF-> {
                    TipoDeChave.CPF
                }
                KeyTypeResponse.EMAIL-> {
                     TipoDeChave.EMAIL
                }
                KeyTypeResponse.RANDOM -> {
                     TipoDeChave.ALEATORIA
                }

                else -> TipoDeChave.CELULAR
            },
            chave = this.key,
            tipoDeConta = when (this.bankAccount.accountType) {
                AccountTypeResponse.CACC -> {
                     TipoDeConta.CONTA_CORRENTE
                }
                else -> TipoDeConta.CONTA_POUPANCA
            },

            conta = ContaAssociada(
                agencia = bankAccount.branch,
                numeroDaConta = bankAccount.accountNumber,
                nomeDoTitular = owner.name,
                cpfDoTitular = owner.taxIdNumber,
                instituicao = "ITAU UNIBANCO"
            )
        )
    }
}

class OwnerBankResponse(
    var type: TypePersonResponse,
    var name: String,
    var taxIdNumber: String
) {

}

enum class TypePersonResponse {
    NATURAL_PERSON, LEGAL_PERSON
}

class BankAccountResponse(
    var participant: String,
    var branch: String,
    var accountNumber: String,
    var accountType: AccountTypeResponse

) {

}

enum class AccountTypeResponse {
    CACC, SVGS
}

enum class KeyTypeResponse {
    CPF, CNPJ, PHONE, EMAIL, RANDOM
}
