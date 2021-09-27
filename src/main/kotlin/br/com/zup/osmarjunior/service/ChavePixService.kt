package br.com.zup.osmarjunior.service

import br.com.zup.osmarjunior.clients.ErpItauClient
import br.com.zup.osmarjunior.endpoints.dtos.ClienteChave
import br.com.zup.osmarjunior.exceptions.ChavePixNaoEncontradaException
import br.com.zup.osmarjunior.repository.ChavePixRepository
import io.micronaut.validation.Validated
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory
import java.util.*
import javax.transaction.Transactional
import javax.validation.Valid

@Validated
@Singleton
class ChavePixService(
    @Inject val chavePixRepository: ChavePixRepository,
    @Inject val erpItauClient: ErpItauClient
) {
    private val logger = LoggerFactory.getLogger(this::class.java.simpleName)

    @Transactional
    fun remover(@Valid clienteChave: ClienteChave){

        val identificadorCliente = clienteChave.identificadorCliente
        val chavePixId = UUID.fromString(clienteChave.chavePixId)

        val optionalChavePix = chavePixRepository.findById(chavePixId)

        if (optionalChavePix.isEmpty){
            throw ChavePixNaoEncontradaException("Chave $chavePixId não encontrada.")
        }

        val response = erpItauClient.consultaPorClienteId(identificadorCliente)
        response.body() ?: throw IllegalStateException("Cliente não encontrado no sistema de contas do banco.")

        val chavePix = optionalChavePix.get()

        if (!chavePix.pertenceAoCliente(identificadorCliente)) {
            val message = "Chave $chavePixId não pertence ao cliente $identificadorCliente."
            logger.error(message)
            throw IllegalStateException(message)
        }

        chavePixRepository.deleteById(chavePixId)
        logger.info("Chave $chavePixId foi removida com sucesso.")
    }
}