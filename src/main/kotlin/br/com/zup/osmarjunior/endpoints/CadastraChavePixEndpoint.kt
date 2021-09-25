package br.com.zup.osmarjunior.endpoints

import br.com.zup.osmarjunior.ChavePixRequest
import br.com.zup.osmarjunior.ChavePixResponse
import br.com.zup.osmarjunior.KeyManagerGrpcServiceGrpc
import br.com.zup.osmarjunior.exceptions.ChavePixExistenteException
import br.com.zup.osmarjunior.service.NovaChavePixService
import br.com.zup.osmarjunior.utils.toModel
import io.grpc.Status
import io.grpc.stub.StreamObserver
import jakarta.inject.Inject
import jakarta.inject.Singleton
import javax.validation.ConstraintViolationException

@Singleton
class CadastraChavePixEndpoint(
    @Inject val service: NovaChavePixService
) :
    KeyManagerGrpcServiceGrpc.KeyManagerGrpcServiceImplBase() {

    override fun cadastraChavePix(
        request: ChavePixRequest,
        responseObserver: StreamObserver<ChavePixResponse>?
    ) {

        try {
            val novaChavePix = request.toModel()
            val chavePixCriada = service.registrar(novaChavePix)

            val response = ChavePixResponse.newBuilder()
                .setClientId(chavePixCriada.identificadorCliente.toString())
                .setPixId(chavePixCriada.id.toString())
                .build()

            responseObserver?.onNext(response)
            responseObserver?.onCompleted()

        } catch (e: ChavePixExistenteException){

            responseObserver?.onError(
                Status.ALREADY_EXISTS
                    .withDescription(e.message)
                    .withCause(e)
                    .asRuntimeException()
            )
            return
        } catch (e: ConstraintViolationException){

            responseObserver?.onError(
                Status.INVALID_ARGUMENT
                    .withDescription("Erro de Validação.")
                    .augmentDescription(e.message)
                    .withCause(e)
                    .asRuntimeException()
            )
            return
        } catch (e: Exception){

            responseObserver?.onError(
                Status.INTERNAL
                    .withDescription("Erro no sistema.")
                    .augmentDescription(e.message)
                    .withCause(e)
                    .asRuntimeException()
            )
            return
        }

    }
}