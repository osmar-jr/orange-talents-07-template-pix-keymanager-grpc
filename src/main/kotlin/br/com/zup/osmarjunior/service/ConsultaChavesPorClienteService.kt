package br.com.zup.osmarjunior.service

import br.com.zup.osmarjunior.ChavePorClienteResponse
import br.com.zup.osmarjunior.clients.ErpItauClient
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
    @Inject val erpItauClient: ErpItauClient,
    @Inject val repository: ChavePixRepository
) {
    fun consultaChavesPorCliente(@Valid identificadorCliente: IdentificadorCliente): MutableList<ChavePorClienteResponse?> {

        val clienteId = UUID.fromString(identificadorCliente.clienteId)

        val response = erpItauClient.consultaPorClienteId(clienteId.toString())
        response.body() ?: throw IllegalStateException("Cliente não encontrado no sistema de contas do banco.")

        val chavesPix = repository.findByIdentificadorCliente(clienteId)

        return CarregaListaChavesPorCliente.criaListaDeChaveResponse(chavesPix)

    }

}
