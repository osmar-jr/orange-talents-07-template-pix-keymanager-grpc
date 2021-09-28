package br.com.zup.osmarjunior.clients.request

import br.com.zup.osmarjunior.annotations.ValidPixKey
import br.com.zup.osmarjunior.model.enums.KeyType
import io.micronaut.core.annotation.Introspected
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@ValidPixKey
@Introspected
data class CreatePixKeyRequest(
    @field:NotNull
    @field:Enumerated(EnumType.STRING)
    val keyType: KeyType,

    @field:NotBlank
    @field:Size(max=77)
    val key: String,

    @field:NotNull
    @field:Valid
    val bankAccount: BankAccountRequest,

    @field:NotNull
    @field:Valid
    val owner: OwnerRequest

)
