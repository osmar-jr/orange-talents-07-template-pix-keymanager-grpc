package br.com.zup.osmarjunior.endpoints

import br.com.zup.osmarjunior.ChavesPorClienteRequest
import br.com.zup.osmarjunior.ChavesPorClienteResponse
import br.com.zup.osmarjunior.KeyManagerConsultaChavesPorClienteServiceGrpc
import br.com.zup.osmarjunior.service.ConsultaChavesPorClienteService
import br.com.zup.osmarjunior.shared.handlers.ErrorAroundHandler
import br.com.zup.osmarjunior.utils.toModel
import io.grpc.stub.StreamObserver
import jakarta.inject.Inject
import jakarta.inject.Singleton

@ErrorAroundHandler
@Singleton
class ConsultaChavesPorClienteEndpoint(
    @Inject val serviceConsulta: ConsultaChavesPorClienteService
) : KeyManagerConsultaChavesPorClienteServiceGrpc.KeyManagerConsultaChavesPorClienteServiceImplBase() {

    override fun consultarPorCliente(
        request: ChavesPorClienteRequest?,
        responseObserver: StreamObserver<ChavesPorClienteResponse>?
    ) {
        val identificadorCliente = request?.toModel()
        val chavesPorCliente = serviceConsulta.consultaChavesPorCliente(identificadorCliente!!)

        responseObserver?.onNext(
            ChavesPorClienteResponse
                .newBuilder()
                .addAllChaves(chavesPorCliente)
                .build()
        )
        responseObserver?.onCompleted()
    }
}