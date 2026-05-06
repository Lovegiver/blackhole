package fr.datasensai.blackhole.core.nlp

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ConceptSourceTest {

    @Test
    fun `should expose expected source values`() {
        assertEquals("nlp", ConceptSource.NOUN_PHRASE.value)
        assertEquals("ngram", ConceptSource.NGRAM.value)
        assertEquals("ner", ConceptSource.NER.value)
    }
}