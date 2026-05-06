package fr.datasensai.blackhole.core.nlp

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ConceptCandidateMergerTest {

    private val nlpConfig = TestConfigs.nlpConfig()
    private val linguisticResourceService = LinguisticResourceService(nlpConfig)
    private val tokenFilterService = TokenFilterService(linguisticResourceService)

    private val merger = ConceptCandidateMerger(
        tokenFilterService = tokenFilterService,
        textNormalizerService = TextNormalizerService()
    )

    @Test
    fun `should merge candidates with same normalized text and same source`() {
        val candidates = listOf(
            ConceptCandidateDraft(
                text = "proposition de loi",
                normalizedText = "proposition de loi",
                source = ConceptSource.NOUN_PHRASE,
                occurrences = 1
            ),
            ConceptCandidateDraft(
                text = "Proposition de loi",
                normalizedText = "proposition de loi",
                source = ConceptSource.NOUN_PHRASE,
                occurrences = 1
            )
        )

        val result = merger.merge(candidates)

        assertEquals(1, result.size)
        assertEquals("proposition de loi", result[0].normalizedText)
        assertEquals(2, result[0].occurrences)
    }

    @Test
    fun `should merge candidates with same normalized text even when sources differ`() {
        val result = merger.merge(
            listOf(
                ConceptCandidateDraft(
                    text = "proposition de loi",
                    normalizedText = "proposition de loi",
                    source = ConceptSource.NOUN_PHRASE
                ),
                ConceptCandidateDraft(
                    text = "proposition de loi",
                    normalizedText = "proposition de loi",
                    source = ConceptSource.NGRAM
                )
            )
        )

        assertEquals(1, result.size)
        assertEquals("proposition de loi", result[0].text)
        assertEquals(ConceptSource.NOUN_PHRASE, result[0].source)
        assertEquals(2, result[0].occurrences)
    }
}