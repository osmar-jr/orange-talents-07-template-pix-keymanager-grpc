package br.com.zup.osmarjunior.utils

import br.com.zup.osmarjunior.*
import br.com.zup.osmarjunior.endpoints.dtos.ChavePixInfo
import com.google.protobuf.Timestamp
import java.time.ZoneId

class CarregarConsultaChavePixResponse {
    fun convert(chavePixInfo: ChavePixInfo): ConsultaChavePixResponse {
        return ConsultaChavePixResponse
            .newBuilder()
            .setClientId(chavePixInfo.clienteId?.toString() ?: "")
            .setPixId(chavePixInfo.pixId?.toString() ?: "")
            .setChave(
                ChaveConsultaResponse
                    .newBuilder()
                    .setTipo(TipoDeChave.valueOf(chavePixInfo.tipo.name))
                    .setChave(chavePixInfo.chave)
                    .setConta(
                        ContaConsultaResponse
                            .newBuilder()
                            .setTipo(TipoDeConta.valueOf(chavePixInfo.tipoDeConta.name))
                            .setInstituicao(chavePixInfo.conta.instituicao)
                            .setNomeDoTitular(chavePixInfo.conta.nomeDoTitular)
                            .setCpfDoTiTular(chavePixInfo.conta.cpfDoTitular)
                            .setAgencia(chavePixInfo.conta.agencia)
                            .setNumeroDaConta(chavePixInfo.conta.numeroDaConta)
                            .build()
                    )
                    .build()
            )
            .setCriadoEm(
                chavePixInfo.registradaEm.let {
                    val instant = it.atZone(ZoneId.of("UTC")).toInstant()
                    Timestamp
                        .newBuilder()
                        .setNanos(instant.nano)
                        .setSeconds(instant.epochSecond)
                        .build()
                }
            )
            .build()
    }

}
