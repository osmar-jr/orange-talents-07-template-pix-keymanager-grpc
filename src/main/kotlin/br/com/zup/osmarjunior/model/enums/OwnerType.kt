package br.com.zup.osmarjunior.model.enums

import org.hibernate.validator.internal.constraintvalidators.hv.br.CPFValidator

enum class OwnerType {
    NATURAL_PERSON,
    LEGAL_PERSON;

    companion object {
        fun getInstance(identification: String): OwnerType {

            val isCpf = CPFValidator().run {
                initialize(null)
                isValid(identification, null)
            }

            if (isCpf) {
                return OwnerType.NATURAL_PERSON
            }

            return OwnerType.LEGAL_PERSON;
        }
    }
}
