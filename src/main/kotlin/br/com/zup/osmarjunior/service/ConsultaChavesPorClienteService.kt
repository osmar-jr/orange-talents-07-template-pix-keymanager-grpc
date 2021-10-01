package br.com.zup.osmarjunior.service

import br.com.zup.osmarjunior.ChavePorClienteResponse
import br.com.zup.osmarjunior.endpoints.dtos.IdentificadorCliente
import br.com.zup.osmarjunior.repository.ChavePixRepository
import br.com.zup.osmarjunior.utils.CarregaListaChavesPorCliente
import io.micronaut.validation.Validated
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.util.*
import javax.validation.Valid

@Validated
@Singleton
class ConsultaChavesPorClienteService(
    @Inject val repository: ChavePixRepository
) {
    fun consultaChavesPorCliente(@Valid identificadorCliente: IdentificadorCliente): MutableList<ChavePorClienteResponse?> {
        val clienteId = UUID.fromString(identificadorCliente.clienteId)

        val chavesPix = repository.findByIdentificadorCliente(clienteId)

        return CarregaListaChavesPorCliente.criaListaDeChaveResponse(chavesPix)

    }

}
