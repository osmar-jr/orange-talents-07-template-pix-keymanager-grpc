package br.com.zup.osmarjunior.endpoints

import br.com.zup.osmarjunior.ConsultaChavePixRequest
import br.com.zup.osmarjunior.ConsultaChavePixResponse
import br.com.zup.osmarjunior.KeyManagerConsultaServiceGrpc
import br.com.zup.osmarjunior.clients.BancoCentralClient
import br.com.zup.osmarjunior.repository.ChavePixRepository
import br.com.zup.osmarjunior.shared.handlers.ErrorAroundHandler
import br.com.zup.osmarjunior.utils.CarregarConsultaChavePixResponse
import br.com.zup.osmarjunior.utils.Instituicoes
import br.com.zup.osmarjunior.utils.toModel
import io.grpc.stub.StreamObserver
import jakarta.inject.Inject
import jakarta.inject.Singleton
import javax.validation.Validator

@ErrorAroundHandler
@Singleton
class ConsultaChavePixEndpoint(
    @Inject val validator: Validator,
    @Inject val repository: ChavePixRepository,
    @Inject val bcbClient: BancoCentralClient
): KeyManagerConsultaServiceGrpc.KeyManagerConsultaServiceImplBase() {

    override fun consultar(
        request: ConsultaChavePixRequest,
        responseObserver: StreamObserver<ConsultaChavePixResponse>?
    ) {
        val filtro = request.toModel(validator)
        val chavePixInfo = filtro.filtra(repository, bcbClient)

        responseObserver?.onNext(
            CarregarConsultaChavePixResponse().convert(chavePixInfo)
        )
        responseObserver?.onCompleted()
    }
}