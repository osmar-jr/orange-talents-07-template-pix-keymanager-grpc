package br.com.zup.osmarjunior.endpoints.dtos

import br.com.zup.osmarjunior.annotations.ValidPixKey
import br.com.zup.osmarjunior.annotations.ValidUUID
import br.com.zup.osmarjunior.clients.response.CreatePixKeyResponse
import br.com.zup.osmarjunior.model.ChavePix
import br.com.zup.osmarjunior.model.ContaAssociada
import br.com.zup.osmarjunior.model.enums.TipoChave
import br.com.zup.osmarjunior.model.enums.TipoConta
import io.micronaut.core.annotation.Introspected
import java.util.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@ValidPixKey
@Introspected
data class NovaChavePix(
    @field:NotBlank
    @ValidUUID
    val identificadorCliente: String,

    @field:NotNull
    val tipoDeChave: TipoChave?,

    @field:Size(max = 77)
    val chave: String?,

    @field:NotNull
    val tipoDeConta: TipoConta?
) {
    fun toModel(conta: ContaAssociada, bcbKey: CreatePixKeyResponse): ChavePix {
        return ChavePix(
            identificadorCliente = UUID.fromString(this.identificadorCliente),
            tipoChave = TipoChave.valueOf(this.tipoDeChave!!.name),
            chave = if (this.isRandom()) bcbKey.key else this.chave!!,
            tipoConta = TipoConta.valueOf(this.tipoDeConta!!.name),
            conta = conta
        )
    }

    fun isRandom(): Boolean {
        return this.tipoDeChave!! == TipoChave.ALEATORIA
    }

}
