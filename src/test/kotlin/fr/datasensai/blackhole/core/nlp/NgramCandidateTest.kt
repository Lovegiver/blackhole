package fr.datasensai.blackhole.core.nlp

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class NgramCandidateTest {

    @Test
    fun `should fail when text is blank`() {
        assertThrows<IllegalArgumentException> {
            NgramCandidate("")
        }
    }

}