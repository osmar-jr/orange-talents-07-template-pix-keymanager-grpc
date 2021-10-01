package br.com.zup.osmarjunior.endpoints

import br.com.zup.osmarjunior.ChavesPorClienteRequest
import br.com.zup.osmarjunior.KeyManagerConsultaChavesPorClienteServiceGrpc
import br.com.zup.osmarjunior.model.ChavePix
import br.com.zup.osmarjunior.model.ContaAssociada
import br.com.zup.osmarjunior.model.enums.TipoChave
import br.com.zup.osmarjunior.model.enums.TipoConta
import br.com.zup.osmarjunior.repository.ChavePixRepository
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Singleton
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*

@MicronautTest(
    transactional = false
)
internal class ConsultaChavesPorClienteEndpointTest(
    val repository: ChavePixRepository,
    val grpcClient: KeyManagerConsultaChavesPorClienteServiceGrpc.KeyManagerConsultaChavesPorClienteServiceBlockingStub
) {

    companion object {
        val CLIENT_ID = UUID.randomUUID()
    }

    @BeforeEach
    fun setUp() {
        repository.saveAll(chavesPix())
    }

    @AfterEach
    fun tearDown() {
        repository.deleteAll()
    }


    @Test
    fun `deve retornar uma lista quando existirem chaves do cliente informado`() {
        val response = grpcClient.consultarPorCliente(
            ChavesPorClienteRequest.newBuilder()
                .setClienteId(CLIENT_ID.toString())
                .build()
        )

        with(response) {
            assertTrue(response.chavesList.isNotEmpty())
            assertTrue(response.chavesList.size == 2)
            assertEquals(CLIENT_ID.toString(), response.clienteId)
        }
    }

    @Test
    fun `deve retornar uma lista vazia quando nao houver chaves do cliente informado`() {

        val clienteId = UUID.randomUUID().toString()

        val response = grpcClient.consultarPorCliente(
            ChavesPorClienteRequest.newBuilder()
                .setClienteId(clienteId)
                .build()
        )

        with(response) {
            assertTrue(response.chavesList.isEmpty())
            assertEquals(clienteId, response.clienteId)
        }
    }

    @Test
    fun `nao deve retornar uma lista quando cliente id não for informado`() {
        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.consultarPorCliente(
                ChavesPorClienteRequest.newBuilder()
                    .setClienteId("")
                    .build()
            )
        }

        with(exception) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
        }
    }

    @Test
    fun `nao deve retornar uma lista quando dados informados invalidos`() {

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.consultarPorCliente(
                ChavesPorClienteRequest.newBuilder().build()
            )
        }

        with(exception) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
        }
    }

    private fun chavesPix(): List<ChavePix> {
        val chaves = listOf<ChavePix>(
            ChavePix(
                identificadorCliente = CLIENT_ID,
                tipoChave = TipoChave.CPF,
                chave = "49474152071",
                tipoConta = TipoConta.CONTA_CORRENTE,
                conta = ContaAssociada(
                    instituicao = "ITAÚ UNIBANCO S.A.",
                    agencia = "0001",
                    cpfDoTitular = "49474152071",
                    nomeDoTitular = "Carga Intrínseca",
                    numeroDaConta = "212233"
                )
            ),
            ChavePix(
                identificadorCliente = CLIENT_ID,
                tipoChave = TipoChave.EMAIL,
                chave = "carga@intrinseca.com.br",
                tipoConta = TipoConta.CONTA_CORRENTE,
                conta = ContaAssociada(
                    instituicao = "ITAÚ UNIBANCO S.A.",
                    agencia = "0001",
                    cpfDoTitular = "49474152071",
                    nomeDoTitular = "Carga Intrínseca",
                    numeroDaConta = "212233"
                )
            ),
            ChavePix(
                identificadorCliente = UUID.randomUUID(),
                tipoChave = TipoChave.ALEATORIA,
                chave = UUID.randomUUID().toString(),
                tipoConta = TipoConta.CONTA_CORRENTE,
                conta = ContaAssociada(
                    instituicao = "ITAÚ UNIBANCO S.A.",
                    agencia = "0001",
                    cpfDoTitular = "11364969041",
                    nomeDoTitular = "Carga Cognitiva",
                    numeroDaConta = "601660"
                )
            )
        )
        return chaves
    }

    @Factory
    class Clients {

        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeyManagerConsultaChavesPorClienteServiceGrpc.KeyManagerConsultaChavesPorClienteServiceBlockingStub? {
            return KeyManagerConsultaChavesPorClienteServiceGrpc.newBlockingStub(channel)
        }
    }
}