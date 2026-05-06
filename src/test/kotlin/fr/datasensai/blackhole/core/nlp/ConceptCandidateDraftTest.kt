package fr.datasensai.blackhole.core.nlp

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ConceptCandidateDraftTest {

    @Test
    fun `should create draft from noun phrase candidate`() {
        val candidate = NounPhraseCandidate(
            text = "jeunes majeurs",
            tokens = listOf(
                PosToken("jeunes", "ADJ"),
                PosToken("majeurs", "NOUN")
            )
        )

        val result = ConceptCandidateDraft.fromNounPhrase(candidate)

        assertEquals("jeunes majeurs", result.text)
        assertEquals("jeunes majeurs", result.normalizedText)
        assertEquals(ConceptSource.NOUN_PHRASE, result.source)
        assertEquals(2, result.tokens.size)
    }

    @Test
    fun `should create draft from ngram candidate`() {
        val candidate = NgramCandidate(
            text = "proposition de loi"
        )

        val result = ConceptCandidateDraft.fromNgram(candidate)

        assertEquals("proposition de loi", result.text)
        assertEquals("proposition de loi", result.normalizedText)
        assertEquals(ConceptSource.NGRAM, result.source)
    }

    @Test
    fun `should convert draft to concept candidate`() {
        val draft = ConceptCandidateDraft(
            text = "laure miller",
            normalizedText = "laure miller",
            source = ConceptSource.NOUN_PHRASE
        )

        val result = draft.toConceptCandidate(score = 6.0)

        assertEquals("laure miller", result.text)
        assertEquals("nlp", result.source)
        assertEquals(6.0, result.score)
    }
}