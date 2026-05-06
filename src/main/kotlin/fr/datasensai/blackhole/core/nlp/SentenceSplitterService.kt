package fr.datasensai.blackhole.core.nlp

import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class SentenceSplitterService(
    private val sentenceDetector: OpenNlpSentenceDetectorService,
    private val posTaggingService: PosTaggingService
) {

    fun split(text: String): List<Sentence> {
        return sentenceDetector
            .detect(text)
            .map { sentenceText ->
                Sentence(
                    rawText = sentenceText,
                    tokens = posTaggingService.tagText(sentenceText)
                )
            }
    }
}