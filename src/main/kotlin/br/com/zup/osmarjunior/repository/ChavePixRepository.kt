package br.com.zup.osmarjunior.repository

import br.com.zup.osmarjunior.model.ChavePix
import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository

@Repository
interface ChavePixRepository: JpaRepository<ChavePix, Long> {

    fun existsByChavePix(chavePix: String): Boolean
}
