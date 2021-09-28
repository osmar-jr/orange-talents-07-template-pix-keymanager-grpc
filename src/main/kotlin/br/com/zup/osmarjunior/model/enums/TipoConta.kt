package br.com.zup.osmarjunior.model.enums

enum class TipoConta {
    CONTA_CORRENTE,
    CONTA_POUPANCA;

    fun toAccountType(): AccountType {
        return when(this){
            CONTA_CORRENTE -> AccountType.CACC
            else -> AccountType.SVGS
        }
    }
}
