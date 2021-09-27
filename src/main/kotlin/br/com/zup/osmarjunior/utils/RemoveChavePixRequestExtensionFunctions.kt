package br.com.zup.osmarjunior.utils

import br.com.zup.osmarjunior.RemoveChavePixRequest
import br.com.zup.osmarjunior.endpoints.dtos.ClienteChave

fun RemoveChavePixRequest.toModel(): ClienteChave {
    return ClienteChave(
        identificadorCliente = clientId,
        chavePixId = pixId
    )
}