package br.com.zup.osmarjunior.utils

import br.com.zup.osmarjunior.TipoDeConta
import br.com.zup.osmarjunior.model.enums.TipoConta

fun TipoDeConta.toModel(): TipoConta {
    return when(this){
        TipoDeConta.CONTA_CORRENTE -> TipoConta.CONTA_CORRENTE
        else -> TipoConta.CONTA_POUPANCA
    }
}