package br.com.zup.osmarjunior.utils

import br.com.zup.osmarjunior.ChavePorClienteResponse
import br.com.zup.osmarjunior.TipoDeChave
import br.com.zup.osmarjunior.TipoDeConta
import br.com.zup.osmarjunior.model.ChavePix
import com.google.protobuf.Timestamp
import java.time.ZoneId

class CarregaListaChavesPorCliente {

    companion object {

        fun criaListaDeChaveResponse(chavesPorCliente: Collection<ChavePix>): MutableList<ChavePorClienteResponse?> {
            return chavesPorCliente.map { chavePix ->
                chaveResponse(chavePix)
            }.toMutableList()
        }

        private fun chaveResponse(chavePix: ChavePix): ChavePorClienteResponse? {
            return ChavePorClienteResponse
                .newBuilder()
                .setPixId(chavePix.id.toString())
                .setClienteId(chavePix.identificadorCliente.toString())
                .setTipoDeChave(TipoDeChave.valueOf(chavePix.tipoChave.name))
                .setChave(chavePix.chave)
                .setTipoDeConta(TipoDeConta.valueOf(chavePix.tipoConta.name))
                .setCriadaEm(
                    chavePix.criadoEm.let {
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
}