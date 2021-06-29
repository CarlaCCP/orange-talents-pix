package br.com.orange.chavePix

class ConsultaIdResponse(
    val clienteId: String?,
    val pixId: String?,
    val tipoDeChave: String,
    val chave: String,
    val tipoDeConta: String,
    val nome: String,
    val cpf: String,
    val banco: String,
    val agencia: String,
    val numero: String,
    val criadoEm: String
) {
}