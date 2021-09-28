package br.com.zup.osmarjunior.clients.response

data class DeletePixKeyResponse(
    val key: String,
    val participant: String,
    val deletedAt: String
)