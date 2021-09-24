package br.com.zup.osmarjunior.model

import br.com.zup.osmarjunior.model.enums.TipoChave
import br.com.zup.osmarjunior.model.enums.TipoConta
import javax.persistence.*
import javax.validation.constraints.NotBlank

@Entity
class ChavePix(

    @field:NotBlank
    @Column(nullable = false)
    val identificadorCliente: String,

    @field:NotBlank
    @Column(unique = true, nullable = false)
    val chavePix: String,

    @field:Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val tipoChave: TipoChave,

    @field:Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val tipoConta: TipoConta
) {

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false)
    var id: Long? = null

    override fun toString(): String {
        return "ChavePix(identificadorCliente='$identificadorCliente', chavePix='$chavePix', tipoChave=$tipoChave, tipoConta=$tipoConta)"
    }
}

