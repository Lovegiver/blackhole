package fr.datasensai.blackhole.core.nlp

import fr.datasensai.blackhole.core.config.NlpConfig
import jakarta.enterprise.context.ApplicationScoped
import java.io.BufferedReader
import java.io.InputStreamReader

@ApplicationScoped
class LinguisticResourceService(
    private val nlpConfig: NlpConfig
) {

    fun loadWordSet(fileName: String): Set<String> {
        val path = "${nlpConfig.resources().basePath()}/${nlpConfig.language()}/$fileName"

        val inputStream = Thread.currentThread()
            .contextClassLoader
            .getResourceAsStream(path)
            ?: throw IllegalArgumentException("Resource not found: $path")

        return BufferedReader(InputStreamReader(inputStream))
            .lines()
            .map { it.trim().lowercase() }
            .filter { it.isNotEmpty() }
            .toList()
            .toSet()
    }
}