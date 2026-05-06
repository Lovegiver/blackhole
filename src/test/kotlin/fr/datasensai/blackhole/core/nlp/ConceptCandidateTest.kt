package fr.datasensai.blackhole.core.nlp

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ConceptCandidateTest {

    @Test
    fun `should fail when text is blank`() {
        assertThrows<IllegalArgumentException> {
            ConceptCandidate("", "nlp", 1.0)
        }
    }

    @Test
    fun `should fail when source is invalid`() {
        assertThrows<IllegalArgumentException> {
            ConceptCandidate("test", "invalid", 1.0)
        }
    }

    @Test
    fun `should fail when score is negative`() {
        assertThrows<IllegalArgumentException> {
            ConceptCandidate("test", "nlp", -1.0)
        }
    }

}