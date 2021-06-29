package br.com.orange.chavePix

import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.NotBlank
@Introspected
class RemoveRequest(
    @field: NotBlank val clientId : String,
    @field: NotBlank val pixId: String,
    @field: NotBlank val cpf: String,
    @field: NotBlank val nome: String,
) {

}

