package br.com.zup.osmarjunior.utils

import br.com.zup.osmarjunior.ConsultaChavePixRequest
import br.com.zup.osmarjunior.ConsultaChavePixRequest.*
import br.com.zup.osmarjunior.model.Filtro
import javax.validation.ConstraintViolationException
import javax.validation.Validator

fun ConsultaChavePixRequest.toModel(validator: Validator): Filtro {

    val filtro = when (filtroCase) {
        FiltroCase.PIXID -> pixId.let {
            Filtro.PorPixId(clienteId = it.clienteId, pixId = it.pixId)
        }
        FiltroCase.CHAVE -> Filtro.PorChave(chave)
        FiltroCase.FILTRO_NOT_SET -> Filtro.Invalido()
    }

    val violations = validator.validate(filtro)
    if (violations.isNotEmpty()) throw ConstraintViolationException(violations)

    return filtro
}