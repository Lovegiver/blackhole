package fr.datasensai.blackhole.core.nlp

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ConceptCandidateScorerTest {

    private val scorer = ConceptCandidateScorer(
        scoringConfig = TestConfigs.conceptScoringConfig()
    )

    @Test
    fun `should score noun phrase candidate`() {
        val draft = ConceptCandidateDraft(
            text = "proposition de loi",
            normalizedText = "proposition de loi",
            source = ConceptSource.NOUN_PHRASE,
            tokens = listOf(
                PosToken("proposition", "NOUN"),
                PosToken("de", "ADP"),
                PosToken("loi", "NOUN")
            )
        )

        val result = scorer.score(draft)

        assertEquals("proposition de loi", result.text)
        assertEquals("nlp", result.source)
        assertEquals(6.0, result.score)
    }

    @Test
    fun `should apply single token penalty`() {
        val draft = ConceptCandidateDraft(
            text = "loi",
            normalizedText = "loi",
            source = ConceptSource.NOUN_PHRASE,
            tokens = listOf(
                PosToken("loi", "NOUN")
            )
        )

        val result = scorer.score(draft)

        assertEquals("loi", result.text)
        assertEquals("nlp", result.source)
        assertEquals(4.0, result.score)
    }

    @Test
    fun `should score ngram candidate`() {
        val draft = ConceptCandidateDraft(
            text = "proposition de loi",
            normalizedText = "proposition de loi",
            source = ConceptSource.NGRAM
        )

        val result = scorer.score(draft)

        assertEquals("proposition de loi", result.text)
        assertEquals("ngram", result.source)
        assertEquals(4.0, result.score)
    }
}