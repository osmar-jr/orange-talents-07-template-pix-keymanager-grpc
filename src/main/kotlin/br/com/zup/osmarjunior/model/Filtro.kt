package br.com.zup.osmarjunior.model

import br.com.zup.osmarjunior.annotations.ValidUUID
import br.com.zup.osmarjunior.clients.BancoCentralClient
import br.com.zup.osmarjunior.endpoints.dtos.ChavePixInfo
import br.com.zup.osmarjunior.exceptions.ChavePixNaoEncontradaException
import br.com.zup.osmarjunior.repository.ChavePixRepository
import io.micronaut.core.annotation.Introspected
import io.micronaut.http.HttpStatus
import org.slf4j.LoggerFactory
import java.util.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@Introspected
sealed class Filtro {

    abstract fun filtra(repository: ChavePixRepository, bcbClient: BancoCentralClient): ChavePixInfo

    @Introspected
    data class PorPixId(
        @field:NotBlank
        @field:ValidUUID
        val clienteId: String,

        @field:NotBlank
        @field:ValidUUID
        val pixId: String,
    ) : Filtro() {

        fun pixIdAsUuid() = UUID.fromString(pixId)

        override fun filtra(repository: ChavePixRepository, bcbClient: BancoCentralClient): ChavePixInfo {
            return repository.findById(pixIdAsUuid())
                .filter { it.pertenceAoCliente(clienteId) }
                .map(ChavePixInfo::of)
                .orElseThrow { ChavePixNaoEncontradaException("Chave Pix não encontrada.") }
        }
    }

    @Introspected
    data class PorChave(
        @field:Size(min = 1, max = 77)
        @field:NotBlank
        val chave: String
    ) : Filtro() {

        private val logger = LoggerFactory.getLogger(this::class.java.simpleName)

        override fun filtra(repository: ChavePixRepository, bcbClient: BancoCentralClient): ChavePixInfo {

            // Reforco na validacao da chave que passou pelo NotBlank
            if (chave.isNullOrBlank()) throw IllegalArgumentException("Chave deve ser informada.")

            return repository.findByChave(chave)
                .map(ChavePixInfo::of)
                .orElseGet {
                    logger.info("Consultando chave $chave no BCB")

                    val response = bcbClient.consultaPorChave(chave)

                    when (response.status) {
                        HttpStatus.OK -> response.body()?.toModel()
                        else -> throw ChavePixNaoEncontradaException("Chave $chave não encontrada no BCB.")
                    }
                }
        }
    }

    @Introspected
    class Invalido() : Filtro() {
        override fun filtra(repository: ChavePixRepository, bcbClient: BancoCentralClient): ChavePixInfo {
            throw IllegalArgumentException("Chave Pix inválida ou não informada.")
        }

    }
}
