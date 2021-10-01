package br.com.zup.osmarjunior.utils

import br.com.zup.osmarjunior.ChavesPorClienteRequest
import br.com.zup.osmarjunior.endpoints.dtos.IdentificadorCliente


fun ChavesPorClienteRequest.toModel(): IdentificadorCliente {
    return IdentificadorCliente(
        clienteId = clienteId
    )
}