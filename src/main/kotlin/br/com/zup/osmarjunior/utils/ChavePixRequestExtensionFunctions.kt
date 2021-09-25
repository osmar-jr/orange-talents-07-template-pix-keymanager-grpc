package br.com.zup.osmarjunior.utils

import br.com.zup.osmarjunior.ChavePixRequest
import br.com.zup.osmarjunior.TipoDeChave
import br.com.zup.osmarjunior.TipoDeConta
import br.com.zup.osmarjunior.endpoints.dtos.NovaChavePix
import br.com.zup.osmarjunior.model.enums.TipoChave
import br.com.zup.osmarjunior.model.enums.TipoConta

fun ChavePixRequest.toModel(): NovaChavePix {
    return NovaChavePix(
        identificadorCliente = identificadorCliente,
        tipoDeChave = when(tipoDeChave){
            TipoDeChave.UNKNOWN_CHAVE -> TipoChave.CHAVE_ALEATORIA
            else -> TipoChave.valueOf(tipoDeChave.name)
        },
        chave = chave,
        tipoDeConta = when(tipoDeConta){
            TipoDeConta.UNKNOWN_CONTA -> null
            else -> TipoConta.valueOf(tipoDeConta.name)
        }
    )
}