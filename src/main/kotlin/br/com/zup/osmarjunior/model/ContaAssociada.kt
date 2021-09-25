package br.com.zup.osmarjunior.model

import io.micronaut.core.annotation.Introspected
import javax.persistence.Embeddable
import javax.validation.constraints.NotBlank

@Introspected
@Embeddable
class ContaAssociada(

    @field:NotBlank
    val instituicao: String,

    @field:NotBlank
    val nomeDoTitular: String,

    @field:NotBlank
    val cpfDoTitular: String,

    @field:NotBlank
    val agencia: String,

    @field:NotBlank
    val numeroDaConta: String
)
