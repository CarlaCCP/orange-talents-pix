package br.com.orange.chavePix.clients

import java.time.LocalDateTime

class DeletePixKeyResponse(
    var key: String,
    var participant: String,
    var deleteAt: LocalDateTime =  LocalDateTime.now()
) {

}
