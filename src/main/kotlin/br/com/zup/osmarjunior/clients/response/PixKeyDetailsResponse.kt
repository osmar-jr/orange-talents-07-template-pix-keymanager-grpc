package br.com.zup.osmarjunior.clients.response

import br.com.zup.osmarjunior.endpoints.dtos.ChavePixInfo
import br.com.zup.osmarjunior.model.ContaAssociada
import br.com.zup.osmarjunior.model.enums.KeyType
import java.time.LocalDateTime

data class PixKeyDetailsResponse(
    val keyType: KeyType,
    val key: String,
    val bankAccount: BankAccountResponse,
    val owner: OwnerResponse,
    val createdAt: LocalDateTime
) {
    fun toModel(): ChavePixInfo {
        return ChavePixInfo(
            tipo = keyType.domainType,
            chave = this.key,
            tipoDeConta = this.bankAccount.accountType.toTipoConta(),
            conta = ContaAssociada(
                instituicao = "NOT_FOUND",
                nomeDoTitular = this.owner.name,
                cpfDoTitular = this.owner.taxIdNumber,
                agencia = this.bankAccount.branch,
                numeroDaConta = this.bankAccount.accountNumber
            ),
            registradaEm = createdAt
        )
    }
}