package br.com.orange.chavePix

import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.Size

@Introspected
class ConsultaIdRequest(
    val clienteId: String?,
    val pixId: String?,
    @Size(max = 77) val chave: String?

) {
}