package br.com.zup.osmarjunior.service

import br.com.zup.osmarjunior.clients.ErpItauClient
import br.com.zup.osmarjunior.endpoints.dtos.NovaChavePix
import br.com.zup.osmarjunior.exceptions.ChavePixExistenteException
import br.com.zup.osmarjunior.model.ChavePix
import br.com.zup.osmarjunior.repository.ChavePixRepository
import io.micronaut.validation.Validated
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory
import javax.transaction.Transactional
import javax.validation.Valid
import kotlin.IllegalStateException

@Validated
@Singleton
class NovaChavePixService(
    @Inject val chavePixRepository: ChavePixRepository,
    @Inject val erpItauClient: ErpItauClient
) {
    private val logger = LoggerFactory.getLogger(this::class.java.simpleName)

    @Transactional
    fun registrar(@Valid novaChavePix: NovaChavePix): ChavePix {

        if (chavePixRepository.existsByChave(novaChavePix.chave!!)) {
            logger.error("A chave pix já existe no banco de dados.")
            throw ChavePixExistenteException("A chave ${novaChavePix.chave} já está cadastrada.")
        }

        val response = erpItauClient.consultaClientePorTipoDeConta(
            novaChavePix.identificadorCliente,
            novaChavePix.tipoDeConta!!.name
        )

        val conta = response.body()?.toModel()
            ?: throw IllegalStateException("Cliente não encontrado no sistemas de contas do banco.")

        val chavePix = novaChavePix.toModel(conta)
        chavePixRepository.save(chavePix)

        logger.info("Nova chave pix criada.")

        return chavePix
    }

}
