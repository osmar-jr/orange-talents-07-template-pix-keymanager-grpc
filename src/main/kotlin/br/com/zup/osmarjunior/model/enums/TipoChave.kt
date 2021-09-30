package br.com.zup.osmarjunior.model.enums

import org.hibernate.validator.internal.constraintvalidators.hv.EmailValidator
import org.hibernate.validator.internal.constraintvalidators.hv.br.CNPJValidator
import org.hibernate.validator.internal.constraintvalidators.hv.br.CPFValidator

enum class TipoChave {
    CPF {
        override fun valida(chave: String?): Boolean {
            if (chave.isNullOrBlank()) return false

            if (!chave.matches("[0-9]+".toRegex())) return false

            return CPFValidator().run {
                initialize(null)
                isValid(chave, null)
            }
        }
    },

    CNPJ {
        override fun valida(chave: String?): Boolean {
            if (chave.isNullOrBlank()) return false

            if (!chave.matches("[0-9]{14}".toRegex())) return false

            return CNPJValidator().run {
                initialize(null)
                isValid(chave, null)
            }
        }

    },

    EMAIL {
        override fun valida(chave: String?): Boolean {
            if (chave.isNullOrBlank()) return false

            return EmailValidator().run {
                initialize(null)
                isValid(chave, null)
            }
        }
    },

    CELULAR {
        override fun valida(chave: String?): Boolean {

            if (chave.isNullOrBlank()) return false
            return chave.matches("^\\+[1-9][0-9]\\d{1,14}\$".toRegex())
        }
    },

    ALEATORIA {
        override fun valida(chave: String?): Boolean {
            return chave.isNullOrBlank()
        }
    };

    abstract fun valida(chave: String?): Boolean
}
