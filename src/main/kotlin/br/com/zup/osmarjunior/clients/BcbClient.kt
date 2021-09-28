package br.com.zup.osmarjunior.clients

import br.com.zup.osmarjunior.clients.request.CreatePixKeyRequest
import br.com.zup.osmarjunior.clients.request.DeletePixKeyRequest
import br.com.zup.osmarjunior.clients.response.CreatePixKeyResponse
import br.com.zup.osmarjunior.clients.response.DeletePixKeyResponse
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Post
import io.micronaut.http.client.annotation.Client
import javax.validation.Valid

@Client(value = "\${bcb.host}")
interface BcbClient {

    @Post(value = "/api/v1/pix/keys", consumes = [MediaType.APPLICATION_XML], produces = [MediaType.APPLICATION_XML])
    fun registra(@Valid @Body createPixKeyRequest: CreatePixKeyRequest): HttpResponse<CreatePixKeyResponse>

    @Delete(value = "/api/v1/pix/keys/{key}", consumes = [MediaType.APPLICATION_XML], produces = [MediaType.APPLICATION_XML])
    fun remove(@PathVariable("key") key: String, @Valid @Body deletePixKeyRequest: DeletePixKeyRequest): HttpResponse<DeletePixKeyResponse>
}