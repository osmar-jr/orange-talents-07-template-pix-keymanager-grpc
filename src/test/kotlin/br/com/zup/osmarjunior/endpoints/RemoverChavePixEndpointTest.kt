package br.com.zup.osmarjunior.endpoints

import br.com.zup.osmarjunior.KeyManagerRemoveServiceGrpc
import br.com.zup.osmarjunior.RemoveChavePixRequest
import br.com.zup.osmarjunior.clients.BancoCentralClient
import br.com.zup.osmarjunior.clients.ErpItauClient
import br.com.zup.osmarjunior.clients.request.DeletePixKeyRequest
import br.com.zup.osmarjunior.clients.response.DadosDoTitularResponse
import br.com.zup.osmarjunior.clients.response.DeletePixKeyResponse
import br.com.zup.osmarjunior.clients.response.InstituicaoResponse
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
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import java.time.LocalDateTime
import java.util.*


@ExtendWith(MockitoExtension::class)
@MicronautTest(
    transactional = false
)
internal class RemoverChavePixEndpointTest(
    val repository: ChavePixRepository,
    val grpcClient: KeyManagerRemoveServiceGrpc.KeyManagerRemoveServiceBlockingStub
) {
    @Inject
    lateinit var erpItauClient: ErpItauClient

    @Inject
    lateinit var bcbClient: BancoCentralClient


    private val chavePix = chavePix()
    private val deleteChaveRequest = deleteChavePixRequest()
    private val deleteChaveResponse = deleteChaveResponse()

    companion object {
        val CLIENT_ID = UUID.randomUUID()
    }

    @BeforeEach
    internal fun setUp() {
        repository.deleteAll()
    }

    @AfterEach
    internal fun tearDown() {
        repository.deleteAll()
    }

    @Test
    fun `deve remover chave quando chave existir e pertencer ao cliente`() {
        repository.save(chavePix)

        `when`(erpItauClient.consultaPorClienteId(chavePix.identificadorCliente.toString()))
            .thenReturn(HttpResponse.ok(dadosDoTitularResponse()))

        `when`(bcbClient.remove(chavePix.chave, deleteChaveRequest))
            .thenReturn(HttpResponse.ok(deleteChaveResponse))

        val response = grpcClient.removerChavePix(
            RemoveChavePixRequest
                .newBuilder()
                .setClientId(chavePix.identificadorCliente.toString())
                .setPixId(chavePix.id.toString())
                .build()
        )

        with(response) {
            assertNotNull(response)
            assertEquals(CLIENT_ID.toString(), clientId)
            assertEquals(chavePix.id.toString(), pixId)
            assertFalse(repository.existsById(UUID.fromString(pixId)))
        }
    }

    @Test
    fun `nao deve remover chave quando operacao for proibida pelo Banco Central`() {
        repository.save(chavePix)

        `when`(erpItauClient.consultaPorClienteId(chavePix.identificadorCliente.toString()))
            .thenReturn(HttpResponse.ok(dadosDoTitularResponse()))

        `when`(bcbClient.remove(chavePix.chave, deleteChaveRequest))
            .thenReturn(HttpResponse.status(HttpStatus.FORBIDDEN))

        val response = assertThrows<StatusRuntimeException> {
            grpcClient.removerChavePix(
                RemoveChavePixRequest
                    .newBuilder()
                    .setClientId(chavePix.identificadorCliente.toString())
                    .setPixId(chavePix.id.toString())
                    .build()
            )
        }

        with(response) {
            assertEquals(Status.PERMISSION_DENIED.code, status.code)
            assertEquals("Operação não autorizada.", status.description)
        }
    }

    @Test
    fun `nao deve remover chave quando chave nao encontrada pelo Banco Central`() {
        repository.save(chavePix)

        `when`(erpItauClient.consultaPorClienteId(chavePix.identificadorCliente.toString()))
            .thenReturn(HttpResponse.ok(dadosDoTitularResponse()))

        `when`(bcbClient.remove(chavePix.chave, deleteChaveRequest))
            .thenReturn(HttpResponse.notFound())

        val response = assertThrows<StatusRuntimeException> {
            grpcClient.removerChavePix(
                RemoveChavePixRequest
                    .newBuilder()
                    .setClientId(chavePix.identificadorCliente.toString())
                    .setPixId(chavePix.id.toString())
                    .build()
            )
        }

        with(response) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave ${chavePix.id} não encontrada no BCB.", status.description)
        }
    }

    @Test
    fun `nao deve remover chave quando ocorrer algum erro no Banco Central client`() {
        repository.save(chavePix)

        `when`(erpItauClient.consultaPorClienteId(chavePix.identificadorCliente.toString()))
            .thenReturn(HttpResponse.ok(dadosDoTitularResponse()))

        `when`(bcbClient.remove(chavePix.chave, deleteChaveRequest))
            .thenReturn(HttpResponse.status(HttpStatus.BAD_GATEWAY))

        val response = assertThrows<StatusRuntimeException> {
            grpcClient.removerChavePix(
                RemoveChavePixRequest
                    .newBuilder()
                    .setClientId(chavePix.identificadorCliente.toString())
                    .setPixId(chavePix.id.toString())
                    .build()
            )
        }

        with(response) {
            assertEquals(Status.UNKNOWN.code, status.code)
            assertEquals("Erro inesperado ao tentar deletar chave no sistema do Banco Central.", status.description)
        }
    }

    @Test
    fun `deve retornar erro quando chave nao existir no banco de dados`() {

        val chavePixId = UUID.randomUUID().toString()

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.removerChavePix(
                RemoveChavePixRequest
                    .newBuilder()
                    .setPixId(chavePixId)
                    .setClientId(chavePix.identificadorCliente.toString())
                    .build()
            )
        }

        with(exception) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave $chavePixId não encontrada no banco de dados.", status.description)
        }

    }

    @Test
    fun `deve retonar erro quando cliente nao existir no erp itau`() {
        repository.save(chavePix)

        `when`(erpItauClient.consultaPorClienteId(CLIENT_ID.toString()))
            .thenReturn(HttpResponse.notFound())

        val request = RemoveChavePixRequest
            .newBuilder()
            .setPixId(chavePix.id.toString())
            .setClientId(CLIENT_ID.toString())
            .build()

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.removerChavePix(request)
        }

        with(exception) {
            assertEquals(Status.FAILED_PRECONDITION.code, status.code)
            assertEquals("Cliente não encontrado no sistema de contas do banco.", status.description)
        }
    }

    @Test
    fun `deve retonar erro quando chave nao pertencer ao cliente informado`() {
        repository.save(chavePix)
        val randomClientId = UUID.randomUUID()

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.removerChavePix(
                RemoveChavePixRequest
                    .newBuilder()
                    .setClientId(randomClientId.toString())
                    .setPixId(chavePix.id.toString())
                    .build()
            )
        }

        with(exception) {
            assertEquals(Status.FAILED_PRECONDITION.code, status.code)
            assertEquals("Chave ${chavePix.id.toString()} não pertence ao cliente $randomClientId.", status.description)
        }

    }

    @Test
    fun `deve retornar erro quando dados informados forem invalidos`() {

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.removerChavePix(
                RemoveChavePixRequest
                    .newBuilder()
                    .build()
            )
        }

        with(exception) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
        }
    }

    private fun dadosDoTitularResponse(): DadosDoTitularResponse? {
        return DadosDoTitularResponse(
            id = chavePix.identificadorCliente.toString(),
            nome = chavePix.conta.nomeDoTitular,
            cpf = chavePix.conta.cpfDoTitular,
            instituicao = InstituicaoResponse(
                nome = chavePix.conta.instituicao,
                ispb = "60701190"
            )
        )
    }

    private fun chavePix(): ChavePix {
        return ChavePix(
            identificadorCliente = CLIENT_ID,
            tipoChave = TipoChave.CPF,
            chave = "54618975091",
            tipoConta = TipoConta.CONTA_POUPANCA,
            conta = ContaAssociada(
                instituicao = "ITAÚ UNIBANCO S.A.",
                agencia = "0001",
                cpfDoTitular = "54618975091",
                nomeDoTitular = "Carga Intrínseca",
                numeroDaConta = "212233"
            )
        )
    }

    private fun deleteChavePixRequest(): DeletePixKeyRequest {
        return DeletePixKeyRequest(
            key = chavePix.chave,
            participant = ContaAssociada.ITAU_UNIBANCO_ISPB
        )
    }

    private fun deleteChaveResponse(): DeletePixKeyResponse {
        return DeletePixKeyResponse(
            key = chavePix.chave,
            participant = ContaAssociada.ITAU_UNIBANCO_ISPB,
            deletedAt = LocalDateTime.now().toString()
        )
    }

    @MockBean(ErpItauClient::class)
    fun erpItauClient(): ErpItauClient? {
        return Mockito.mock(ErpItauClient::class.java)
    }

    @MockBean(BancoCentralClient::class)
    fun bancoCentralClient(): BancoCentralClient? {
        return Mockito.mock(BancoCentralClient::class.java)
    }

    @Factory
    class Clients {

        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeyManagerRemoveServiceGrpc.KeyManagerRemoveServiceBlockingStub? {
            return KeyManagerRemoveServiceGrpc.newBlockingStub(channel)
        }
    }
}