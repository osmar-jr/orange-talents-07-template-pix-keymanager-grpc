package br.com.zup.osmarjunior.clients.request

import br.com.zup.osmarjunior.annotations.ValidPixKey
import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@ValidPixKey
@Introspected
data class DeletePixKeyRequest(

    @field:NotBlank
    @field:Size(max=77)
    val key: String,

    @field:NotBlank
    val participant: String
)