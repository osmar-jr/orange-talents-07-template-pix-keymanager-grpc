package br.com.zup.osmarjunior.clients

import br.com.zup.osmarjunior.clients.response.DadosDaContaResponse
import br.com.zup.osmarjunior.clients.response.DadosDoTitularResponse
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client


@Client(value = "\${erp_itau.host}")
interface ErpItauClient {

    @Get(value = "/api/v1/clientes/{clienteId}/contas{?tipo}")
    fun consultaClientePorTipoDeConta(@PathVariable("clienteId") clienteId: String, @QueryValue tipo: String): HttpResponse<DadosDaContaResponse>

    @Get(value = "/api/v1/clientes/{clienteId}")
    fun consultaPorClienteId(@PathVariable("clienteId") clienteId: String): HttpResponse<DadosDoTitularResponse>
}