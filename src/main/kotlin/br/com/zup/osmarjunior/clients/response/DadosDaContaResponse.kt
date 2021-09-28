package br.com.zup.osmarjunior.clients.response

import br.com.zup.osmarjunior.clients.request.BankAccountRequest
import br.com.zup.osmarjunior.clients.request.CreatePixKeyRequest
import br.com.zup.osmarjunior.clients.request.OwnerRequest
import br.com.zup.osmarjunior.endpoints.dtos.NovaChavePix
import br.com.zup.osmarjunior.model.ContaAssociada
import br.com.zup.osmarjunior.model.enums.OwnerType
import java.util.*

data class DadosDaContaResponse (
    val tipo: String,
    val instituicao: InstituicaoResponse,
    val agencia: String,
    val numero: String,
    val titular: TitularResponse
) {
    fun toModel(): ContaAssociada {
        return ContaAssociada(
            instituicao = this.instituicao.nome,
            nomeDoTitular = this.titular.nome,
            cpfDoTitular = this.titular.cpf,
            agencia = this.agencia,
            numeroDaConta = this.numero
        )
    }

    fun toCreateChavePixRequest(novaChavePix: NovaChavePix): CreatePixKeyRequest {
        val bankAccount = BankAccountRequest(
            participant = this.instituicao.ispb,
            branch = this.agencia,
            accountNumber = this.numero,
            accountType = novaChavePix.tipoDeConta!!.toAccountType()
        )

        val owner = OwnerRequest(
            type = titular.toOwnerType(),
            name = this.titular.nome,
            taxIdNumber = this.titular.cpf
        )

        return CreatePixKeyRequest(
            keyType = novaChavePix.tipoDeChave!!.toKeyType(),
            key = if(novaChavePix.isRandom()) UUID.randomUUID().toString() else novaChavePix.chave!!,
            bankAccount = bankAccount,
            owner = owner
        )
    }
}
