package br.com.orange.chavePix.clients

class PixKeysListResponse(
    var pixKeys: PixKeys
) {

}

class PixKeys(
    var keyType: String,
    var key: String,
    var bankAccount: BankAccountResponse,
    var owner: OwnerBankResponse,
    var createdAt: String
) {

}
