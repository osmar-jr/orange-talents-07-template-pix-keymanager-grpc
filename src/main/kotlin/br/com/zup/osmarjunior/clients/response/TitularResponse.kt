package br.com.zup.osmarjunior.clients.response

import br.com.zup.osmarjunior.model.enums.OwnerType
import org.hibernate.validator.internal.constraintvalidators.hv.br.CPFValidator

data class TitularResponse(
    val id: String,
    val nome: String,
    val cpf: String
) {
    fun toOwnerType(): OwnerType {

        val isCpf = CPFValidator().run {
            initialize(null)
            isValid(cpf, null)
        }

        if (isCpf) return OwnerType.NATURAL_PERSON

        return OwnerType.LEGAL_PERSON
    }
}
