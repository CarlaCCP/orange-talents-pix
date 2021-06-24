package br.com.orange.chavePix

import io.micronaut.data.annotation.Query
import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import java.util.*

@Repository
interface ChavePixRepository : JpaRepository<ChavePix, UUID> {

    fun existsByChave(chave: String?): Boolean
    override fun existsById(pixid: UUID): Boolean
    fun findByChave(chave: String): Optional<ChavePix>


//    @Query("SELECT a FROM ChavePix a WHERE a.clienteId = :clienteId")
//    fun buscaClienteId(clienteId: UUID?) : MutableList<ChavePix>

    fun findAllByClienteId(clienteId: UUID): List<ChavePix>


}
