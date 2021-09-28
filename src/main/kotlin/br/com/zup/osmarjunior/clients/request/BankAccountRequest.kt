package br.com.zup.osmarjunior.clients.request

import br.com.zup.osmarjunior.model.enums.AccountType
import io.micronaut.core.annotation.Introspected
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Introspected
data class BankAccountRequest(

    @field:NotBlank
    val participant: String,

    @field:NotBlank
    @field:Size(max=4, min = 4)
    val branch: String,

    @field:NotBlank
    @field:Size(max=6, min = 6)
    val accountNumber: String,

    @field:NotNull
    @field:Enumerated(EnumType.STRING)
    val accountType: AccountType,
)