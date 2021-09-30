package br.com.zup.osmarjunior.model.enums

enum class AccountType {
    CACC,
    SVGS;

    fun toTipoConta(): TipoConta {
        return when(this){
            CACC -> TipoConta.CONTA_CORRENTE
            else -> TipoConta.CONTA_CORRENTE
        }
    }
}
