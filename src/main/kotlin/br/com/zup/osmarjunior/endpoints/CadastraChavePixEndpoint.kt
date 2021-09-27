package br.com.zup.osmarjunior.endpoints

import br.com.zup.osmarjunior.ChavePixRequest
import br.com.zup.osmarjunior.ChavePixResponse
import br.com.zup.osmarjunior.KeyManagerGrpcServiceGrpc
import br.com.zup.osmarjunior.exceptions.ChavePixExistenteException
import br.com.zup.osmarjunior.service.NovaChavePixService
import br.com.zup.osmarjunior.shared.handlers.ErrorAroundHandler
import br.com.zup.osmarjunior.utils.toModel
import io.grpc.Status
import io.grpc.stub.StreamObserver
import jakarta.inject.Inject
import jakarta.inject.Singleton
import javax.validation.ConstraintViolationException

@ErrorAroundHandler
@Singleton
class CadastraChavePixEndpoint(
    @Inject val service: NovaChavePixService
) :
    KeyManagerGrpcServiceGrpc.KeyManagerGrpcServiceImplBase() {

    override fun cadastraChavePix(
        request: ChavePixRequest,
        responseObserver: StreamObserver<ChavePixResponse>?
    ) {

        val novaChavePix = request.toModel()
        val chavePixCriada = service.registrar(novaChavePix)

        val response = ChavePixResponse.newBuilder()
            .setClientId(chavePixCriada.identificadorCliente.toString())
            .setPixId(chavePixCriada.id.toString())
            .build()

        responseObserver?.onNext(response)
        responseObserver?.onCompleted()


    }
}