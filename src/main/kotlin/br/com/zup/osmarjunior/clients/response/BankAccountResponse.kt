package br.com.zup.osmarjunior.clients.response

import br.com.zup.osmarjunior.model.enums.AccountType

data class BankAccountResponse(
    val participant: String,
    val branch: String,
    val accountNumber: String,
    val accountType: AccountType,
)