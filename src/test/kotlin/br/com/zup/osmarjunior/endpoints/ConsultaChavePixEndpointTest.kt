package br.com.zup.osmarjunior.endpoints

import br.com.zup.osmarjunior.ConsultaChavePixRequest
import br.com.zup.osmarjunior.KeyManagerConsultaServiceGrpc
import br.com.zup.osmarjunior.PixId
import br.com.zup.osmarjunior.clients.BancoCentralClient
import br.com.zup.osmarjunior.clients.response.BankAccountResponse
import br.com.zup.osmarjunior.clients.response.OwnerResponse
import br.com.zup.osmarjunior.clients.response.PixKeyDetailsResponse
import br.com.zup.osmarjunior.model.ChavePix
import br.com.zup.osmarjunior.model.ContaAssociada
import br.com.zup.osmarjunior.model.enums.KeyType
import br.com.zup.osmarjunior.model.enums.OwnerType
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
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.junit.jupiter.MockitoExtension
import java.time.LocalDateTime
import java.util.*

@ExtendWith(MockitoExtension::class)
@MicronautTest(transactional = false)
internal class ConsultaChavePixEndpointTest(
    val repository: ChavePixRepository,
    val grpcClient: KeyManagerConsultaServiceGrpc.KeyManagerConsultaServiceBlockingStub
) {

    @Inject
    lateinit var bcbClient: BancoCentralClient

    private val chavePix = chavePix()

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
    fun `deve retornar uma chave quando existir no banco de dados`() {
        repository.save(chavePix)

        val response = grpcClient.consultar(
            ConsultaChavePixRequest
                .newBuilder()
                .setPixId(
                    PixId.newBuilder()
                        .setPixId(chavePix.id.toString())
                        .setClienteId(chavePix.identificadorCliente.toString())
                        .build()
                )
                .build()
        )

        with(response) {
            assertEquals(chavePix.identificadorCliente.toString(), response.clientId)
            assertEquals(chavePix.id.toString(), response.pixId)
            assertEquals(chavePix.chave, response.chave.chave)
        }

    }

    @Test
    fun `deve retornar uma chave quando nao existir no banco de dados e existir cliente do Banco Central`() {
        val pixKeyDetailsResponse = pixKeyDetailsResponse()

        `when`(bcbClient.consultaPorChave(chavePix.chave)).thenReturn(
            HttpResponse.ok(pixKeyDetailsResponse)
        )

        val response = grpcClient.consultar(
            ConsultaChavePixRequest.newBuilder()
                .setChave(chavePix.chave)
                .build()
        )

        with(response) {
            assertTrue(response.pixId.isNullOrBlank())
            assertTrue(response.clientId.isNullOrBlank())
            assertEquals(chavePix.chave, response.chave.chave)
        }
    }

    @Test
    fun `nao dever retornar uma chave quando filtro invalido`() {

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.consultar(
                ConsultaChavePixRequest.newBuilder().build()
            )
        }

        with(exception) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Chave Pix inválida ou não informada.", status.description)
        }

    }

    @Test
    fun `nao dever retornar uma chave quando chave e cliente id invalidos`() {

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.consultar(
                ConsultaChavePixRequest.newBuilder()
                    .setPixId(
                        PixId.newBuilder()
                            .setPixId("")
                            .setClienteId("")
                            .build()
                    )
                    .build()
            )
        }

        with(exception) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
        }

    }

    @Test
    fun `nao dever retornar uma chave quando por chave invalida`() {

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.consultar(
                ConsultaChavePixRequest.newBuilder().setChave("").build()
            )
        }

        with(exception){
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Chave deve ser informada.", status.description)
        }
    }

    @Test
    fun `nao dever retornar uma chave quando chave e cliente id nao existirem`() {

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.consultar(
                ConsultaChavePixRequest.newBuilder()
                    .setPixId(
                        PixId.newBuilder()
                            .setPixId(UUID.randomUUID().toString())
                            .setClienteId(UUID.randomUUID().toString())
                            .build()
                    )
                    .build()
            )
        }

        with(exception) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave Pix não encontrada.", status.description)
        }

    }

    @Test
    fun `nao dever retornar uma chave quando chave e cliente id nao existirem local e BCB`() {

        val chave = "nao@existe.com.br"

        `when`(bcbClient.consultaPorChave(chave)).thenReturn(
            HttpResponse.notFound()
        )

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.consultar(
                ConsultaChavePixRequest.newBuilder()
                    .setChave(chave)
                    .build()
            )
        }

        with(exception) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave $chave não encontrada no BCB.", status.description)
        }

    }

    private fun pixKeyDetailsResponse(): PixKeyDetailsResponse {
        return PixKeyDetailsResponse(
            keyType = KeyType.by(chavePix.tipoChave),
            key = chavePix.chave,
            bankAccount = BankAccountResponse(
                participant = ContaAssociada.ITAU_UNIBANCO_ISPB,
                branch = chavePix.conta.agencia,
                accountNumber = chavePix.conta.numeroDaConta,
                accountType = chavePix.tipoConta.toAccountType()
            ),
            owner = OwnerResponse(
                type = OwnerType.getInstance(chavePix.conta.cpfDoTitular).name,
                name = chavePix.conta.nomeDoTitular,
                taxIdNumber = chavePix.conta.cpfDoTitular
            ),
            createdAt = LocalDateTime.now()
        )
    }

    private fun chavePix(): ChavePix {
        return ChavePix(
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
        )
    }

    @MockBean(BancoCentralClient::class)
    fun bancoCentralClient(): BancoCentralClient? {
        return mock(BancoCentralClient::class.java)
    }

    @Factory
    class Clients {

        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeyManagerConsultaServiceGrpc.KeyManagerConsultaServiceBlockingStub? {
            return KeyManagerConsultaServiceGrpc.newBlockingStub(channel)
        }
    }

}