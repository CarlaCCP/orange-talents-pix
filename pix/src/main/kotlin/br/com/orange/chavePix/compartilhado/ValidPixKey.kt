package br.com.orange.chavePix

import br.com.orange.chavePix.registra.NovaChavePix
import javax.inject.Singleton
import javax.validation.Constraint
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext
import javax.validation.Payload
import kotlin.reflect.KClass

@MustBeDocumented
@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [ValidPixKeyValidator::class])
annotation class ValidPixKey(
    val message: String = "Chave pix inválida",
    val groups: Array<KClass<Any>> = [],
    val payload: Array<KClass<Payload>> = []
)


@Singleton
class ValidPixKeyValidator : ConstraintValidator<ValidPixKey, NovaChavePix>{

    override fun isValid(value: NovaChavePix?, context: ConstraintValidatorContext?): Boolean {
        if(value?.tipo == null){
            return false
        }

        return value.tipo.valida(value.chave)
    }

}
