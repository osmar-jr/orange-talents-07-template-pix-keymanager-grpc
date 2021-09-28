package br.com.zup.osmarjunior.service

import br.com.zup.osmarjunior.clients.BcbClient
import br.com.zup.osmarjunior.clients.ErpItauClient
import br.com.zup.osmarjunior.endpoints.dtos.ClienteChave
import br.com.zup.osmarjunior.exceptions.ChavePixNaoEncontradaException
import br.com.zup.osmarjunior.exceptions.OperacaoNaoPermitidaException
import br.com.zup.osmarjunior.repository.ChavePixRepository
import io.grpc.Status
import io.micronaut.http.HttpStatus
import io.micronaut.validation.Validated
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory
import java.util.*
import javax.transaction.Transactional
import javax.validation.Valid

@Validated
@Singleton
class RemoveChavePixService(
    @Inject val chavePixRepository: ChavePixRepository,
    @Inject val erpItauClient: ErpItauClient,
    @Inject val bcbClient: BcbClient
) {
    private val logger = LoggerFactory.getLogger(this::class.java.simpleName)

    @Transactional
    fun remover(@Valid clienteChave: ClienteChave){

        val identificadorCliente = clienteChave.identificadorCliente
        val chavePixId = UUID.fromString(clienteChave.chavePixId)

        val optionalChavePix = chavePixRepository.findById(chavePixId)

        if (optionalChavePix.isEmpty){
            throw ChavePixNaoEncontradaException("Chave $chavePixId não encontrada no banco de dados.")
        }

        val chavePix = optionalChavePix.get()

        if (!chavePix.pertenceAoCliente(identificadorCliente)) {
            val message = "Chave $chavePixId não pertence ao cliente $identificadorCliente."
            logger.error(message)
            throw IllegalStateException(message)
        }

        val erpItauResponse = erpItauClient.consultaPorClienteId(identificadorCliente)
        val dadosDoTitularResponse = erpItauResponse.body()
            ?: throw IllegalStateException("Cliente não encontrado no sistema de contas do banco.")

        val deletePixKeyRequest = dadosDoTitularResponse.toDeletePixKeyRequest(chavePix)

        val bcbResponse = bcbClient.remove(chavePix.chave, deletePixKeyRequest)
        when(bcbResponse.status){
            HttpStatus.OK -> bcbResponse.body()
            HttpStatus.FORBIDDEN -> throw OperacaoNaoPermitidaException("Operação não autorizada.")
            HttpStatus.NOT_FOUND -> throw ChavePixNaoEncontradaException("Chave $chavePixId não encontrada no BCB.")
            else -> throw Exception("Erro inesperado ao tentar deletar chave no sistema do Banco Central.")
        }

        chavePixRepository.deleteById(chavePixId)
        logger.info("Chave $chavePixId foi removida com sucesso.")
    }
}