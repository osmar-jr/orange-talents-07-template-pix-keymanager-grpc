package br.com.zup.osmarjunior.utils

import br.com.zup.osmarjunior.TipoDeChave
import br.com.zup.osmarjunior.model.enums.TipoChave


fun TipoDeChave.toModel(): TipoChave {
    return when (this) {
        TipoDeChave.CPF -> TipoChave.CPF
        TipoDeChave.EMAIL -> TipoChave.EMAIL
        TipoDeChave.TELEFONE_CELULAR -> TipoChave.TELEFONE_CELULAR
        else -> TipoChave.CHAVE_ALEATORIA
    }
}