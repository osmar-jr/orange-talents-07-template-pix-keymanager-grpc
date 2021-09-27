package br.com.zup.osmarjunior.endpoints

import br.com.zup.osmarjunior.ChavePixRequest
import br.com.zup.osmarjunior.KeyManagerGrpcServiceGrpc
import br.com.zup.osmarjunior.TipoDeChave
import br.com.zup.osmarjunior.TipoDeConta
import br.com.zup.osmarjunior.clients.ErpItauClient
import br.com.zup.osmarjunior.clients.response.DadosDaContaResponse
import br.com.zup.osmarjunior.clients.response.InstituicaoResponse
import br.com.zup.osmarjunior.clients.response.TitularResponse
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
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import java.util.*

@MicronautTest(
    transactional = false
)
internal class CadastraChavePixEndpointTest(
    val repository: ChavePixRepository,
    val grpcClient: KeyManagerGrpcServiceGrpc.KeyManagerGrpcServiceBlockingStub
) {
    @Inject
    lateinit var erpItauClient: ErpItauClient;

    companion object {
        val CLIENT_ID = UUID.randomUUID()
    }

    @BeforeEach
    internal fun setUp() {
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

        val response = grpcClient.cadastraChavePix(
            ChavePixRequest.newBuilder()
                .setIdentificadorCliente(CLIENT_ID.toString())
                .setTipoDeChave(TipoDeChave.EMAIL)
                .setChave("carga.intrinseca@zup.com.br")
                .setTipoDeConta(TipoDeConta.CONTA_CORRENTE)
                .build()
        )

        with(response) {
            assertEquals(CLIENT_ID.toString(), clientId)
            assertNotNull(pixId)
        }
    }

    @Test
    fun `nao deve cadastrar chave quando chave pix ja existente`() {
        repository.save(chavePix())

        val chavePixRequest = ChavePixRequest.newBuilder()
            .setIdentificadorCliente(CLIENT_ID.toString())
            .setTipoDeChave(TipoDeChave.CPF)
            .setChave("54618975091")
            .setTipoDeConta(TipoDeConta.CONTA_POUPANCA)
            .build()

        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.cadastraChavePix(
                chavePixRequest
            )
        }

        with(exception){
            assertEquals(Status.ALREADY_EXISTS.code, status.code)
            assertEquals("A chave ${chavePixRequest.chave} já está cadastrada.", status.description)
        }
    }

    @Test
    fun `nao deve cadastrar chave quando dados informados forem invalidos`(){
        val exception = assertThrows<StatusRuntimeException> {
            grpcClient.cadastraChavePix(
                ChavePixRequest.newBuilder().build()
            )
        }

        with(exception){
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
        }
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

    private fun dadosDaContaResponse(): DadosDaContaResponse? {
        return DadosDaContaResponse(
            agencia = "0001",
            instituicao = InstituicaoResponse(
                nome = "ITAÚ UNIBANCO S.A.",
                ispb = "60701190"
            ),
            numero = "212233",
            tipo = TipoConta.CONTA_CORRENTE.name,
            titular = TitularResponse(
                id = CLIENT_ID.toString(),
                nome = "Carga Intrínseca",
                cpf = "54618975091"
            )
        )
    }

    @MockBean(ErpItauClient::class)
    fun erpItauClient(): ErpItauClient? {
        return Mockito.mock(ErpItauClient::class.java)
    }

    @Factory
    class Clients {

        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeyManagerGrpcServiceGrpc.KeyManagerGrpcServiceBlockingStub? {
            return KeyManagerGrpcServiceGrpc.newBlockingStub(channel)
        }
    }

}