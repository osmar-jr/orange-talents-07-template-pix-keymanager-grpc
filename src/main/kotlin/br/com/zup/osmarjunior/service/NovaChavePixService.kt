package br.com.zup.osmarjunior.service

import br.com.zup.osmarjunior.clients.BancoCentralClient
import br.com.zup.osmarjunior.clients.ErpItauClient
import br.com.zup.osmarjunior.endpoints.dtos.NovaChavePix
import br.com.zup.osmarjunior.exceptions.ChavePixExistenteException
import br.com.zup.osmarjunior.model.ChavePix
import br.com.zup.osmarjunior.repository.ChavePixRepository
import io.micronaut.http.HttpStatus
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
    @Inject val erpItauClient: ErpItauClient,
    @Inject val bancoCentralClient: BancoCentralClient
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

        if (response.body.isEmpty) {
            logger.error("Cliente inexistente no sistema ERP Iti.")
            throw IllegalStateException("Cliente não encontrado no sistemas de contas do banco.")
        }

        val dadosDaContaResponse = response.body.get()

        val conta = dadosDaContaResponse.toModel()
        val createChavePixRequest = conta.toCreateChavePixRequest(novaChavePix)

        val bcbResponse = bancoCentralClient.registra(createChavePixRequest)

        val createPixKeyResponse = when (bcbResponse.status) {
            HttpStatus.CREATED -> {
                bcbResponse.body()
            }
            HttpStatus.UNPROCESSABLE_ENTITY -> {
                logger.error("ERRO: Chave ja existente no BCB client.")
                throw ChavePixExistenteException("A chave ${novaChavePix.chave} já está cadastrada no sistema do BCB.")
            }
            else -> {
                logger.error("Erro desconhecido no BCB client.")
                throw Exception("Erro inesperado ao tentar registrar a chave no Banco Central.")
            }
        }

        val chavePix = novaChavePix.toModel(conta, createPixKeyResponse)
        chavePixRepository.save(chavePix)

        logger.info("Nova chave pix criada.")

        return chavePix
    }

}
