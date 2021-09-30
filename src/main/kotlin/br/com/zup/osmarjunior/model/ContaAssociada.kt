package br.com.zup.osmarjunior.model

import br.com.zup.osmarjunior.clients.request.BankAccountRequest
import br.com.zup.osmarjunior.clients.request.CreatePixKeyRequest
import br.com.zup.osmarjunior.clients.request.OwnerRequest
import br.com.zup.osmarjunior.endpoints.dtos.NovaChavePix
import br.com.zup.osmarjunior.model.enums.KeyType
import br.com.zup.osmarjunior.model.enums.OwnerType
import io.micronaut.core.annotation.Introspected
import java.util.*
import javax.persistence.Embeddable
import javax.validation.constraints.NotBlank

@Introspected
@Embeddable
class ContaAssociada(

    @field:NotBlank
    val instituicao: String,

    @field:NotBlank
    val nomeDoTitular: String,

    @field:NotBlank
    val cpfDoTitular: String,

    @field:NotBlank
    val agencia: String,

    @field:NotBlank
    val numeroDaConta: String
){
    companion object{
        val ITAU_UNIBANCO_ISPB: String = "60701190"
    }

    fun toCreateChavePixRequest(novaChavePix: NovaChavePix): CreatePixKeyRequest {
        val bankAccount = BankAccountRequest(
            participant = ITAU_UNIBANCO_ISPB,
            branch = this.agencia,
            accountNumber = this.numeroDaConta,
            accountType = novaChavePix.tipoDeConta!!.toAccountType()
        )

        val owner = OwnerRequest(
            type = OwnerType.getInstance(this.cpfDoTitular),
            name = this.nomeDoTitular,
            taxIdNumber = this.cpfDoTitular
        )

        return CreatePixKeyRequest(
            keyType = KeyType.by(novaChavePix.tipoDeChave!!),
            key = if(novaChavePix.isRandom()) UUID.randomUUID().toString() else novaChavePix.chave!!,
            bankAccount = bankAccount,
            owner = owner
        )
    }
}
