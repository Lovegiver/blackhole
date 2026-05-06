package fr.datasensai.blackhole.core.nlp

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class TokenFilterServiceTest {

    private val linguisticResourceService = LinguisticResourceService(
        nlpConfig = TestConfigs.nlpConfig()
    )

    private val service = TokenFilterService(
        linguisticResourceService = linguisticResourceService
    )

    @Test
    fun `should identify weak tokens`() {
        assertTrue(service.isWeakToken("mme"))
    }

    @Test
    fun `should not classify numeric tokens as weak by default`() {
        assertFalse(service.isWeakToken("2680"))
    }

    @Test
    fun `should identify significant tokens`() {
        assertTrue(service.isSignificantToken("proposition"))
        assertTrue(service.isSignificantToken("loi"))
        assertFalse(service.isSignificantToken("mme"))
    }

    @Test
    fun `should handle empty token`() {
        assertFalse(service.isSignificantToken(""))
    }

    @Test
    fun `should identify stopwords`() {
        assertTrue(service.isStopword("de"))
        assertFalse(service.isStopword("proposition"))
    }

    @Test
    fun `should identify list made only of stopwords`() {
        assertTrue(service.isAllStopwords(listOf("de", "la")))
        assertFalse(service.isAllStopwords(listOf("de", "loi")))
    }

    @Test
    fun `should identify at least one significant token`() {
        assertTrue(service.containsAtLeastOneSignificantToken(listOf("de", "loi")))
        assertFalse(service.containsAtLeastOneSignificantToken(listOf("mme")))
    }

}