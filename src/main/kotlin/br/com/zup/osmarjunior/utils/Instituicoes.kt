package br.com.zup.osmarjunior.utils

import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileWriter


class Instituicoes {

    val logger = LoggerFactory.getLogger(this::class.java.simpleName)

    init {
        carregaInstituicoes()
    }

    companion object {
        private var bancos: MutableMap<String, String> = mutableMapOf()

        fun name(ispb: String): String? {

            if(this.bancos.isEmpty()){
                Instituicoes()
            }

            return bancos.get(ispb) ?: ""
        }
    }

    private fun carregaInstituicoes() {
        if (bancos.isEmpty()){
            bancos = carregaBancos()
        }
    }

    private fun carregaBancos(): MutableMap<String, String> {
        try {
            val bancos_file = File("bancos_pix.txt").readLines()
            val bancos_pix: MutableMap<String, String> = mutableMapOf()

            if (bancos_file.isNotEmpty()){

                bancos_file.map {
                    it.split("=")
                }.forEach {
                    bancos_pix.put(it[0], it[1])
                }

            } else {
                val lines = File("bancos.csv").readLines()
                lines.map { line ->
                    line.split(",")

                }.forEach{
                    bancos_pix.put(it[0].trim(), it[1].trim())
                }

                writeToFile(bancos_pix)
            }

            return bancos_pix
        } catch (ex: Exception){

            logger.error("Erro ao carregar as instituições bancárias do arquivo local.", ex)
            return mutableMapOf()
        }
    }

    private fun writeToFile(bancos: MutableMap<String, String>) {
        try {
            val writer = FileWriter("bancos_pix.txt", true)
            bancos.forEach { banco ->
                writer.write(banco.toString() + "\n")
            }
            writer.close()
        } catch (ex: Exception) {
            logger.error("Erro ao gravar instituicoes no arquivo", ex)
        }
    }
}