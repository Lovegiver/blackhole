package fr.datasensai.blackhole.core.nlp

import fr.datasensai.blackhole.core.config.OpenNlpConfig
import jakarta.enterprise.context.ApplicationScoped
import opennlp.tools.sentdetect.SentenceDetectorME
import opennlp.tools.sentdetect.SentenceModel

@ApplicationScoped
class OpenNlpSentenceDetectorService(
    private val openNlpConfig: OpenNlpConfig
) {

    private val detector: SentenceDetectorME by lazy {
        val stream = Thread.currentThread()
            .contextClassLoader
            .getResourceAsStream(openNlpConfig.sentenceModelPath())
            ?: error("OpenNLP sentence model not found")

        SentenceDetectorME(SentenceModel(stream))
    }

    fun detect(text: String): List<String> {
        if (text.isBlank()) {
            return emptyList()
        }

        return detector
            .sentDetect(text)
            .map { it.trim() }
            .filter { it.isNotBlank() }
    }
}