package br.com.zup.osmarjunior.model.enums

enum class KeyType(val domainType: TipoChave) {
    CPF(TipoChave.CPF),
    CNPJ(TipoChave.CNPJ),
    EMAIL(TipoChave.EMAIL),
    PHONE(TipoChave.CELULAR),
    RANDOM(TipoChave.ALEATORIA);

    companion object {
        private val mapping = KeyType.values().associateBy(KeyType::domainType)

        fun by(domainType: TipoChave): KeyType {
            return mapping[domainType] ?: throw IllegalArgumentException("KeyType is invalid.")
        }
    }
}
