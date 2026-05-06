package fr.datasensai.blackhole.core.nlp

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class NounPhraseCandidateServiceTest {

    private val openNlpConfig = TestConfigs.openNlpConfig()

    private val tokenizerService = TokenizerService(
        openNlpTokenizerService = OpenNlpTokenizerService(openNlpConfig)
    )

    private val posTaggingService = PosTaggingService(
        tokenizerService = tokenizerService,
        openNlpConfig = openNlpConfig
    )

    private val normalizer = TextNormalizerService()

    private val service = NounPhraseCandidateService(
        normalizer = normalizer
    )

    @Test
    fun `should extract noun phrases from legal sentence`() {
        val text = "Proposition de loi de Mme Laure Miller visant à protéger durablement les jeunes majeurs"

        val posTokens = posTaggingService.tagText(text)

        val candidates = service.extract(posTokens)

        val labels = candidates.map { it.text }

        assertTrue(labels.contains("proposition de loi"))
        assertTrue(labels.contains("laure miller"))
        assertTrue(labels.contains("jeunes majeurs"))
    }
}