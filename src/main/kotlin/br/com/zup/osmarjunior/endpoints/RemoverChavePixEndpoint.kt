package br.com.zup.osmarjunior.endpoints

import br.com.zup.osmarjunior.KeyManagerRemoveServiceGrpc
import br.com.zup.osmarjunior.RemoveChavePixRequest
import br.com.zup.osmarjunior.RemoveChavePixResponse
import br.com.zup.osmarjunior.service.RemoveChavePixService
import br.com.zup.osmarjunior.shared.handlers.ErrorAroundHandler
import br.com.zup.osmarjunior.utils.toModel
import io.grpc.stub.StreamObserver
import jakarta.inject.Inject
import jakarta.inject.Singleton

@ErrorAroundHandler
@Singleton
class RemoverChavePixEndpoint(
    @Inject val removeChavePixService: RemoveChavePixService
) : KeyManagerRemoveServiceGrpc.KeyManagerRemoveServiceImplBase() {

    override fun removerChavePix(
        request: RemoveChavePixRequest?,
        responseObserver: StreamObserver<RemoveChavePixResponse>?
    ) {
        val clienteChave = request?.toModel()
        removeChavePixService.remover(clienteChave!!)

        responseObserver?.onNext(
            RemoveChavePixResponse
                .newBuilder()
                .setClientId(clienteChave.identificadorCliente)
                .setPixId(clienteChave.chavePixId)
                .build()
        )
        responseObserver?.onCompleted()
    }
}