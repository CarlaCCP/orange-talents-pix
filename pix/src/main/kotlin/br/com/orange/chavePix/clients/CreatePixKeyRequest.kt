package br.com.orange.chavePix.clients

import br.com.orange.TipoDeConta
import br.com.orange.chavePix.ChavePix
import br.com.orange.chavePix.TipoDeChave
import java.time.LocalDateTime
import javax.inject.Singleton


class CreatePixKeyRequest(
    var keyType: KeyType,
    var key: String,
    var bankAccount: BankAccount,
    var owner: OwnerBank,
    var createdAt: LocalDateTime = LocalDateTime.now()
) {



}
