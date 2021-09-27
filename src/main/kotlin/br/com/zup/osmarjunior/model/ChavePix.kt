package br.com.zup.osmarjunior.model

import br.com.zup.osmarjunior.TipoDeChave
import br.com.zup.osmarjunior.TipoDeConta
import br.com.zup.osmarjunior.model.enums.TipoChave
import br.com.zup.osmarjunior.model.enums.TipoConta
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Entity
@Table(
    uniqueConstraints = [UniqueConstraint(
        name = "uk_chave_pix",
        columnNames = ["chave"]
    )]
)
class ChavePix(

    @field:NotBlank
    @Column(nullable = false)
    val identificadorCliente: UUID,

    @field:NotNull
    @field:Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val tipoChave: TipoChave,

    @field:NotBlank
    @Column(unique = true, nullable = false)
    val chave: String,

    @field:NotNull
    @field:Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val tipoConta: TipoConta,

    @field:Valid
    @Embedded
    val conta: ContaAssociada
) {

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false)
    var id: UUID? = null

    val now = LocalDateTime.now()

    @Column(nullable = false, updatable = false)
    val criadoEm: LocalDateTime = now

    fun pertenceAoCliente(clienteId: String): Boolean {
        return this.identificadorCliente.equals(UUID.fromString(clienteId))
    }
}

