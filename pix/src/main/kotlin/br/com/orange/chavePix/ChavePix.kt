package br.com.orange.chavePix


import br.com.orange.TipoDeConta
import org.hibernate.annotations.GenericGenerator
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Entity
@Table(uniqueConstraints = [UniqueConstraint(name = "uk_chave_pix", columnNames = ["chave"])])
class ChavePix(

    @field: NotNull
    @Column(nullable = false, length = 16)
    val clienteId: UUID,

    @Enumerated(EnumType.STRING)
    @field: NotNull
    @Column(nullable = false)
    val tipo: TipoDeChave,

    @field: NotBlank
    @Column(nullable = false, unique = true)
    var chave : String,

    @Enumerated(EnumType.STRING)
    @field: NotNull
    @Column(nullable = false)
    val tipoDeConta: TipoDeConta,


    @field: Valid
    @Embedded
    val conta : ContaAssociada,
) {

   


    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", columnDefinition = "BINARY(16)")
    val id: UUID? = null

    @Column(nullable = false)
    val criada :LocalDateTime = LocalDateTime.now()

}
