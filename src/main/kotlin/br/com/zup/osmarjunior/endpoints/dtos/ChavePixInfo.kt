package br.com.zup.osmarjunior.endpoints.dtos

import br.com.zup.osmarjunior.model.ChavePix
import br.com.zup.osmarjunior.model.ContaAssociada
import br.com.zup.osmarjunior.model.enums.TipoChave
import br.com.zup.osmarjunior.model.enums.TipoConta
import java.time.LocalDateTime
import java.util.*

data class ChavePixInfo(
    val pixId: UUID? = null,
    val clienteId: UUID? = null,
    val tipo: TipoChave,
    val chave: String,
    val tipoDeConta: TipoConta,
    val conta: ContaAssociada,
    val registradaEm: LocalDateTime = LocalDateTime.now()
){
    companion object{
        fun of(chavePix: ChavePix): ChavePixInfo {
            return ChavePixInfo(
                pixId = chavePix.id,
                clienteId = chavePix.identificadorCliente,
                tipo = chavePix.tipoChave,
                chave = chavePix.chave,
                tipoDeConta = chavePix.tipoConta,
                conta = chavePix.conta,
                registradaEm = chavePix.criadoEm
            )
        }
    }
}
