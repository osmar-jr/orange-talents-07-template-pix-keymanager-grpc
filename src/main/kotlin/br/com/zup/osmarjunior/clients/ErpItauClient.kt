package br.com.zup.osmarjunior.clients

import br.com.zup.osmarjunior.clients.response.DadosDoClienteResponse
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.client.annotation.Client


@Client(value = "\${kmgrpc.hosts.erp_itau}")
interface ErpItauClient {

    @Get(value = "/api/v1/clientes/{clienteId}")
    fun consultaCliente(@PathVariable("clienteId") clienteId: String): HttpResponse<DadosDoClienteResponse>
}