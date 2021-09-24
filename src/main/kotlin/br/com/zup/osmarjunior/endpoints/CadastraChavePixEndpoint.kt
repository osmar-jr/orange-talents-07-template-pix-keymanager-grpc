package br.com.zup.osmarjunior.endpoints

import br.com.zup.osmarjunior.*
import br.com.zup.osmarjunior.clients.ErpItauClient
import br.com.zup.osmarjunior.model.ChavePix
import br.com.zup.osmarjunior.model.enums.TipoChave
import br.com.zup.osmarjunior.model.enums.TipoConta
import br.com.zup.osmarjunior.repository.ChavePixRepository
import br.com.zup.osmarjunior.utils.toModel
import io.grpc.Status
import io.grpc.stub.StreamObserver
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientResponseException
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory
import java.util.*
import javax.validation.ConstraintViolationException

@Singleton
class CadastraChavePixEndpoint(
    @Inject val erpItauClient: ErpItauClient,
    @Inject val chavePixRepository: ChavePixRepository
) :
    KeyManagerGrpcServiceGrpc.KeyManagerGrpcServiceImplBase() {

    private val logger = LoggerFactory.getLogger(this::class.java.simpleName)

    override fun cadastraChavePix(request: ChavePixRequest?, responseObserver: StreamObserver<ChavePixResponse>?) {
        val identificadorCliente = request?.identificadorCliente
        val tipoDeChave = request?.tipoDeChave
        val tipoDeConta = request?.tipoDeConta
        val chave = request?.chave

        if (identificadorCliente.isNullOrBlank()) {
            responseObserver?.onError(
                Status
                    .INVALID_ARGUMENT
                    .withDescription("Identificador do Cliente deve ser preenchido.")
                    .asRuntimeException()
            )
            return
        }

        if (tipoDeChave == null || tipoDeChave.equals(TipoDeChave.UNRECOGNIZED)) {
            responseObserver?.onError(
                Status
                    .INVALID_ARGUMENT
                    .withDescription("Tipo de Chave vazio ou inválido.")
                    .asRuntimeException()
            )
            return
        }


        if (chave.isNullOrBlank() && !tipoDeChave.equals(TipoDeChave.CHAVE_ALEATORIA)) {
            responseObserver?.onError(
                Status
                    .INVALID_ARGUMENT
                    .withDescription("Chave Pix deve ser preenchido quando não for do tipo Aleatória.")
                    .asRuntimeException()
            )
            return
        }

        if (tipoDeConta == null || tipoDeConta.equals(TipoDeConta.UNRECOGNIZED)) {
            responseObserver?.onError(
                Status
                    .INVALID_ARGUMENT
                    .withDescription("Tipo de Conta vazio ou inválido.")
                    .asRuntimeException()
            )
            return
        }

        if (tipoDeChave.equals(TipoDeChave.CPF) && !chave!!.matches("[0-9]{11}".toRegex())) {
            responseObserver?.onError(
                Status
                    .INVALID_ARGUMENT
                    .withDescription("CPF Inválido.")
                    .augmentDescription("Formato esperado: 99999999999")
                    .asRuntimeException()
            )
            return
        }

        if (tipoDeChave.equals(TipoDeChave.TELEFONE_CELULAR) && !chave!!.matches("\\+[1-9][0-9]\\d{1,14}".toRegex())) {
            responseObserver?.onError(
                Status
                    .INVALID_ARGUMENT
                    .withDescription("Telefone Inválido.")
                    .augmentDescription("Formato esperado: +9999999999999")
                    .asRuntimeException()
            )
            return
        }

        if (chavePixRepository.existsByChavePix(chave!!)) {
            responseObserver?.onError(
                Status.ALREADY_EXISTS
                    .withDescription("A chave informada já existe.")
                    .asRuntimeException()
            )
            return
        }

        try {
            val response = erpItauClient.consultaCliente(identificadorCliente)

            if (response.status.equals(HttpStatus.NOT_FOUND)) {
                logger.error("Cliente não encontrado no sistema de contas do banco.")

                responseObserver?.onError(
                    Status.NOT_FOUND
                        .withDescription("O identificador do cliente não foi encontrado.")
                        .asRuntimeException()
                )
                return
            }

            logger.info("Consulta no ERP Itau realizada: ${response.body.get()}")

        } catch (e: HttpClientResponseException) {
            logger.error("Erro inesperado ao consultar cliente no sistema de contas do banco.", e)

            responseObserver?.onError(
                Status.INTERNAL
                    .withDescription("Problema interno não identidicado.")
                    .withCause(e)
                    .augmentDescription(e.message)
                    .asRuntimeException()
            )
            return

        }

        val chavePix: String =
            if (tipoDeChave.equals(TipoDeChave.CHAVE_ALEATORIA)) UUID.randomUUID().toString() else chave!!

        val tipoChave: TipoChave = tipoDeChave.toModel();
        val tipoConta: TipoConta = tipoDeConta.toModel()

        val chavePixCriada = ChavePix(
            identificadorCliente = identificadorCliente,
            chavePix = chavePix,
            tipoChave = tipoChave,
            tipoConta = tipoConta
        )

        logger.info("Solicitação de Chave Pix realizada: $request")

        try {

            chavePixRepository.save(chavePixCriada)
            logger.info("Chave Pix salva no banco: $chavePixCriada")

        } catch (e: ConstraintViolationException) {

            responseObserver?.onError(
                Status.INVALID_ARGUMENT
                    .withDescription("Erro na validação dos dados ao tentar salvar chave no banco de dados.")
                    .withCause(e)
                    .asRuntimeException()
            )
            return
        }

        val response = ChavePixResponse.newBuilder()
            .setPixId(chavePixCriada.id!!)
            .build()

        responseObserver?.onNext(response)
        responseObserver?.onCompleted()

    }
}