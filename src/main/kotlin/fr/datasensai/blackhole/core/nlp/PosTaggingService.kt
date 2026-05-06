package fr.datasensai.blackhole.core.nlp

import fr.datasensai.blackhole.core.config.OpenNlpConfig
import jakarta.enterprise.context.ApplicationScoped
import opennlp.tools.postag.POSModel
import opennlp.tools.postag.POSTaggerME

@ApplicationScoped
class PosTaggingService(
    private val tokenizerService: TokenizerService,
    private val openNlpConfig: OpenNlpConfig
) {

    private val tagger: POSTaggerME by lazy {
        val stream = Thread.currentThread()
            .contextClassLoader
            .getResourceAsStream(openNlpConfig.posModelPath())
            ?: error("OpenNLP POS model not found: ${openNlpConfig.posModelPath()}")

        POSTaggerME(POSModel(stream))
    }

    fun tagText(text: String): List<PosToken> {
        val tokens = tokenizerService.tokenize(text)
        val tags = tagger.tag(tokens.toTypedArray())

        return tokens.zip(tags).map { (token, tag) ->
            PosToken(
                token = token,
                pos = tag
            )
        }
    }
}