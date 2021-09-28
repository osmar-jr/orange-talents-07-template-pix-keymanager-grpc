package br.com.zup.osmarjunior.clients.response

data class BankAccountResponse(
    val participant: String,
    val branch: String,
    val accountNumber: String,
    val accountType: String,
)