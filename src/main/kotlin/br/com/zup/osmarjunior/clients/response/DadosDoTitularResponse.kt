package br.com.zup.osmarjunior.clients.response

data class DadosDoTitularResponse(
    val id: String,
    val nome: String,
    val cpf: String,
    val instituicao: InstituicaoResponse
)
