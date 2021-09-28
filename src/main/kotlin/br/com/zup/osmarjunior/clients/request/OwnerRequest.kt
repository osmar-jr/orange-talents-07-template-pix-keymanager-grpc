package br.com.zup.osmarjunior.clients.request

import br.com.zup.osmarjunior.model.enums.OwnerType
import io.micronaut.core.annotation.Introspected
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Introspected
data class OwnerRequest(
    @field:NotNull
    @field:Enumerated(EnumType.STRING)
    val type: OwnerType,

    @field:NotBlank
    val name: String,

    @field:NotBlank
    val taxIdNumber: String
)
