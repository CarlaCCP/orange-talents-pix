package br.com.orange.chavePix.remove

import br.com.orange.chavePix.InstituicaoResponse

data class DadosDoClienteResponse(
    val id: String,
    val nome: String,
    val cpf: String,
    val instituicao: InstituicaoResponse
) {

}
