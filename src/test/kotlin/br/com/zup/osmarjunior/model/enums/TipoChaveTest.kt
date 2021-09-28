package br.com.zup.osmarjunior.model.enums

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@MicronautTest
internal class TipoChaveTest{

    @Nested
    inner class CPF {

        @Test
        fun `deve retornar valido quando informado um numero valido`(){
            with(TipoChave.CPF){
                assertTrue(valida("79844265037"))
            }
        }

        @Test
        fun `deve retornar invalido quando cpf nao for informado`(){
            with(TipoChave.CPF){
                assertFalse(valida(null))
                assertFalse(valida(""))
                assertFalse(valida("  "))
            }
        }

        @Test
        fun `deve retornar invalido quando informado um numero invalido`(){
            with(TipoChave.CPF){
                assertFalse(valida("384020248102"))
            }
        }

        @Test
        fun `deve retornar invalido quando informado um valor com letras`(){
            with(TipoChave.CPF){
                assertFalse(valida("7984426503a"))
            }
        }
    }

    @Nested
    inner class TELEFONE_CELULAR {

        @Test
        fun `deve retornar valido quando informado um numero de telefone correto`(){
            with(TipoChave.TELEFONE_CELULAR){
                assertTrue(valida("+5585988714077"))
            }
        }

        @Test
        fun `deve retornar invalido quando numero de telefone nao for informado`(){
            with(TipoChave.TELEFONE_CELULAR){
                assertFalse(valida(null))
                assertFalse(valida(""))
                assertFalse(valida("   "))
            }
        }

        @Test
        fun `deve retornar invalido quando numero de telefone mal formatado`(){
            with(TipoChave.TELEFONE_CELULAR){
                assertFalse(valida("5585988714077"))
                assertFalse(valida("+55 (85) 9 8871-4077"))
            }
        }
    }

    @Nested
    inner class EMAIL {

        @Test
        fun `deve retornar valido quando informado um email correto`(){
            with(TipoChave.EMAIL){
                assertTrue(valida("email@validos.com.br"))
            }
        }

        @Test
        fun `deve retornar invalido quando email nao for informado`(){
            with(TipoChave.EMAIL){
                assertFalse(valida(null))
                assertFalse(valida(""))
                assertFalse(valida("   "))
            }
        }

        @Test
        fun `deve retornar invalido quando informado email mal formatado`(){
            with(TipoChave.EMAIL){
                assertFalse(valida("invalido"))
                assertFalse(valida("invalido@"))
                assertFalse(valida("@invalido.com"))
            }
        }
    }

    @Nested
    inner class CHAVE_ALEATORIA {

        @Test
        fun `deve retornar valido quando chave aleatoria nao informada`(){
            with(TipoChave.CHAVE_ALEATORIA){
                assertTrue(valida(null))
                assertTrue(valida(""))
                assertTrue(valida("   "))
            }
        }

        @Test
        fun `deve retornar invalido quando chave aleatoria for informada`(){
            with(TipoChave.CHAVE_ALEATORIA){
                assertFalse(valida("QUALQUER VALOR"))
            }
        }

    }
}