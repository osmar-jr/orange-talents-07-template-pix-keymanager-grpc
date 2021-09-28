package br.com.zup.osmarjunior.clients.response

data class CreatePixKeyResponse(
    val keyType: String,
    val key: String,
    val bankAccount: BankAccountResponse,
    val owner: OwnerResponse,
    val createdAt: String
)
