package br.com.zup.osmarjunior.clients.response

import br.com.zup.osmarjunior.clients.request.DeletePixKeyRequest
import br.com.zup.osmarjunior.model.ChavePix

data class DadosDoTitularResponse(
    val id: String,
    val nome: String,
    val cpf: String,
    val instituicao: InstituicaoResponse
) {
    fun toDeletePixKeyRequest(chavePix: ChavePix): DeletePixKeyRequest {
        return DeletePixKeyRequest(
            key = chavePix.chave,
            participant = this.instituicao.ispb
        )
    }
}
