package br.com.zup.osmarjunior.repository

import br.com.zup.osmarjunior.model.ChavePix
import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import java.util.*

@Repository
interface ChavePixRepository: JpaRepository<ChavePix, UUID> {

    fun existsByChave(chave: String): Boolean

    fun findByChave(chave: String): Optional<ChavePix>
}
