package br.com.orange.chavePix

import br.com.orange.TipoDeChave
import br.com.orange.TipoDeConta
import io.micronaut.core.annotation.Introspected
import java.time.LocalDateTime
import javax.inject.Singleton
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@Introspected
data class RegistraRequest(
     @field: NotBlank val clientId : String,
     @field: NotBlank val tipoDeChave: TipoDeChave,
     @field: Size(max = 77) val chave: String,
     @field: NotBlank val tipoDeConta: TipoDeConta
) {
}