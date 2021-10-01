package br.com.zup.osmarjunior.endpoints.dtos

import br.com.zup.osmarjunior.annotations.ValidUUID
import io.micronaut.core.annotation.Introspected
import javax.validation.constraints.NotBlank

@Introspected
data class IdentificadorCliente(
    @field:NotBlank @ValidUUID
    val clienteId: String
)