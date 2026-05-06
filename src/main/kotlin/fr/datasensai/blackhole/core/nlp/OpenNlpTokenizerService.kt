package fr.datasensai.blackhole.core.nlp

import fr.datasensai.blackhole.core.config.OpenNlpConfig
import jakarta.enterprise.context.ApplicationScoped
import opennlp.tools.tokenize.TokenizerME
import opennlp.tools.tokenize.TokenizerModel

@ApplicationScoped
class OpenNlpTokenizerService(
    private val openNlpConfig: OpenNlpConfig
) {

    private val tokenizer: TokenizerME by lazy {
        val stream = Thread.currentThread()
            .contextClassLoader
            .getResourceAsStream(openNlpConfig.tokenizerModelPath())
            ?: error("OpenNLP tokenizer model not found: ${openNlpConfig.tokenizerModelPath()}")

        TokenizerME(TokenizerModel(stream))
    }

    fun tokenize(text: String): List<String> {
        return tokenizer.tokenize(text).toList()
    }
}