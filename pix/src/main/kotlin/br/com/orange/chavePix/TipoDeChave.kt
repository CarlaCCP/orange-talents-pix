package br.com.orange.chavePix

import io.micronaut.validation.validator.constraints.EmailValidator

enum class TipoDeChave {

    CPF {
        override fun valida(chave: String?): Boolean {
            if(chave.isNullOrBlank()){
                return false
            }

            if(!chave.matches("[0-9]+".toRegex())){
                return false
            }

         return true
        }
    },

    CELULAR {
        override fun valida(chave: String?): Boolean {
            if(chave.isNullOrBlank()){
                return false
            }
            return chave.matches("^\\+[1-9][0-9]\\d{1,14}\$".toRegex())
        }
    },
    EMAIL {
        override fun valida(chave: String?): Boolean {
            if( chave.isNullOrBlank()){
                return false
            }

            return true
        }
    },
    ALEATORIA {

        override fun valida(chave: String?) = chave.isNullOrBlank()
    };

    abstract fun valida(chave: String?) : Boolean
}

