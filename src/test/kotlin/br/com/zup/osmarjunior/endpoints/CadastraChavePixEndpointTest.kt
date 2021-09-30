package br.com.zup.osmarjunior.endpoints

import br.com.zup.osmarjunior.ChavePixRequest
import br.com.zup.osmarjunior.KeyManagerGrpcServiceGrpc
import br.com.zup.osmarjunior.TipoDeChave
import br.com.zup.osmarjunior.TipoDeConta
import br.com.zup.osmarjunior.clients.BancoCentralClient
import br.com.zup.osmarjunior.clients.ErpItauClient
import br.com.zup.osmarjunior.clients.request.BankAccountRequest
import br.com.zup.osmarjunior.clients.request.CreatePixKeyRequest
import br.com.zup.osmarjunior.clients.request.OwnerRequest
import br.com.zup.osmarjunior.clients.response.*
import br.com.zup.osmarjunior.model.ChavePix
import br.com.zup.osmarjunior.model.ContaAssociada
import br.com.zup.osmarjunior.model.enums.*
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
import org.junit.jupiter.api.Assertions.assertNotNull
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
internal class CadastraChavePixEndpointTest(
    val repository: ChavePixRepository,
    val grpcClient: KeyManagerGrpcServiceGrpc.KeyManagerGrpcServiceBlockingStub
) {
    @Inject
    lateinit var erpItauClient: ErpItauClient

    @Inject
    lateinit var bcbClient: BancoCentralClient

    private val chavePix = chavePix()
    private val chavePixResponse = createPixKeyResponse()
    private val chavePixRequest = createPixKeyRequest()

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
    fun `deve registrar uma chave pix`() {

        `when`(
            erpItauClient.consultaClientePorTipoDeConta(
                clienteId = CLIENT_ID.toString(),
                TipoConta.CONTA_CORRENTE.name
            )
        )
            .thenReturn(HttpResponse.ok(dadosDaContaResponse()))

        `when`(bcbClient.registra(chavePixRequest)).thenReturn(
            HttpResponse.created(chavePixResponse)
        )
        val response = grpcClient.cadastraChavePix(
            ChavePixRequest.newBuilder()
                .setIdentificadorCliente(CLIENT_ID.toString())
                .setTipoDeChave(TipoDeChave.valueOf(chavePix.tipoChave.name))
                .setChave(chavePix.chave)
                .setTipoDeConta(TipoDeConta.valueOf(chavePix.tipoConta.name))
                .build()
        )

        with(response) {
            assertEquals(CLIENT_ID.toString(), clientId)
            assertNotNull(pixId)
        }
    }

    @Test
    fun `nao deve registrar uma chave pix quando chave existir no Banco Central`() {

        `when`(
            erpItauClient.consultaClientePorTipoDeConta(
                clienteId = CLIENT_ID.toString(),
                TipoConta.CONTA_CORRENTE.name
            )
        )
            .thenReturn(HttpResponse.ok(dadosDaContaResponse()))

        `when`(bcbClient.registra(chavePixRequest)).thenReturn(
            HttpResponse.unprocessableEntity()
        )

        val response = assertThrows<StatusRuntimeException> {
            grpcClient.cadastraChavePix(
                ChavePixRequest.newBuilder()
                    .setIdentificadorCliente(CLIENT_ID.toString())
                    .setTipoDeChave(TipoDeChave.valueOf(chavePix.tipoChave.name))
                    .setChave(chavePix.chave)
                    .setTipoDeConta(TipoDeConta.valueOf(chavePix.tipoConta.name))
                    .build()
            )
        }

        with(response) {
            assertEquals(Status.ALREADY_EXISTS.code, status.code)
            assertEquals("A chave ${chavePix.chave} já está cadastrada no sistema do BCB.", status.description)
        }
    }

    @Test
    fun `nao deve registrar uma chave pix quando nao for possivel registrar no Banco Central`() {

        `when`(
            erpItauClient.consultaClientePorTipoDeConta(
                clienteId = CLIENT_ID.toString(),
                TipoConta.CONTA_CORRENTE.name
            )
        )
            .thenReturn(HttpResponse.ok(dadosDaContaResponse()))

        `when`(bcbClient.registra(chavePixRequest)).thenReturn(
            HttpResponse.badRequest()
        )

        val response = assertThrows<StatusRuntimeException> {
            grpcClient.cadastraChavePix(
                ChavePixRequest.newBuilder()
                    .setIdentificadorCliente(CLIENT_ID.toString())
                    .setTipoDeChave(TipoDeChave.valueOf(chavePix.tipoChave.name))
                    .setChave(chavePix.chave)
                    .setTipoDeConta(TipoDeConta.valueOf(chavePix.tipoConta.name))
                    .build()
            )
        }

        with(response) {
            assertEquals(Status.UNKNOWN.code, status.code)
            assertEquals("Erro inesperado ao tentar registrar a chave no Banco Central.", status.description)
        }
    }

    @Test
    fun `nao deve cadastrar chave quando chave pix ja existente`() {
        repository.save(chavePix)

        val chavePixRequest = ChavePixRequest.newBuilder()
            .setIdentificadorCliente(CLIENT_ID.toString())
            .setTipoDeChave(TipoDeChave.valueOf(chavePix.tipoChave.name))
            .setChave(chavePix.chave)
            .setTipoDeConta(TipoDeConta.valueOf(chavePix.tipoConta.name))
            .build()

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.cadastraChavePix(
                chavePixRequest
            )
        }

        with(exception) {
            assertEquals(Status.ALREADY_EXISTS.code, status.code)
            assertEquals("A chave ${chavePixRequest.chave} já está cadastrada.", status.description)
        }
    }

    @Test
    fun `nao deve cadastrar chave quando dados informados forem invalidos`() {
        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.cadastraChavePix(
                ChavePixRequest.newBuilder().build()
            )
        }

        with(exception) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
        }
    }

    @Test
    fun `nao deve cadastrar chave quando cliente nao encontrado no sistema de contas do banco`() {
        `when`(
            erpItauClient.consultaClientePorTipoDeConta(
                clienteId = CLIENT_ID.toString(),
                tipo = TipoConta.CONTA_POUPANCA.name
            )
        ).thenReturn(HttpResponse.notFound())

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.cadastraChavePix(
                ChavePixRequest.newBuilder()
                    .setIdentificadorCliente(CLIENT_ID.toString())
                    .setTipoDeChave(TipoDeChave.CPF)
                    .setChave("54618975091")
                    .setTipoDeConta(TipoDeConta.CONTA_POUPANCA)
                    .build()
            )
        }

        with(exception) {
            assertEquals(Status.FAILED_PRECONDITION.code, status.code)
            assertEquals(
                "Cliente não encontrado no sistemas de contas do banco.",
                status.description
            )
        }
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

    private fun dadosDaContaResponse(): DadosDaContaResponse? {
        return DadosDaContaResponse(
            agencia = chavePix.conta.agencia,
            instituicao = InstituicaoResponse(
                nome = chavePix.conta.instituicao,
                ispb = ContaAssociada.ITAU_UNIBANCO_ISPB
            ),
            numero = chavePix.conta.numeroDaConta,
            tipo = TipoConta.CONTA_CORRENTE.name,
            titular = TitularResponse(
                id = CLIENT_ID.toString(),
                nome = chavePix.conta.nomeDoTitular,
                cpf = chavePix.conta.cpfDoTitular
            )
        )
    }

    private fun createPixKeyRequest(): CreatePixKeyRequest {
        return CreatePixKeyRequest(
            keyType = KeyType.by(chavePix.tipoChave),
            key = chavePix.chave,
            bankAccount = BankAccountRequest(
                participant = ContaAssociada.ITAU_UNIBANCO_ISPB,
                branch = chavePix.conta.agencia,
                accountNumber = chavePix.conta.numeroDaConta,
                accountType = chavePix.tipoConta.toAccountType()
            ),
            owner = OwnerRequest(
                type = OwnerType.NATURAL_PERSON,
                name = chavePix.conta.nomeDoTitular,
                taxIdNumber = chavePix.conta.cpfDoTitular
            )
        )
    }

    private fun createPixKeyResponse(): CreatePixKeyResponse {
        return CreatePixKeyResponse(
            keyType = KeyType.CPF.name,
            key = chavePix.chave,
            bankAccount = BankAccountResponse(
                participant = ContaAssociada.ITAU_UNIBANCO_ISPB,
                branch = chavePix.conta.agencia,
                accountNumber = chavePix.conta.numeroDaConta,
                accountType = AccountType.CACC
            ),
            owner = OwnerResponse(
                type = OwnerType.NATURAL_PERSON.name,
                name = chavePix.conta.nomeDoTitular,
                taxIdNumber = chavePix.conta.cpfDoTitular
            ),
            createdAt = LocalDateTime.now().toString()
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
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeyManagerGrpcServiceGrpc.KeyManagerGrpcServiceBlockingStub? {
            return KeyManagerGrpcServiceGrpc.newBlockingStub(channel)
        }
    }

}