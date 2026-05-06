package fr.datasensai.blackhole.core.nlp

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class TokenizerServiceTest {

    private val tokenizer = TokenizerService(
        openNlpTokenizerService = OpenNlpTokenizerService(
            openNlpConfig = TestConfigs.openNlpConfig()
        )
    )

    @Test
    fun `should preserve semantic tokens`() {
        val tokens = tokenizer.tokenize(
            "Proposition de loi de Mme Laure Miller"
        )

        assertTrue(tokens.contains("de"))
        assertTrue(tokens.contains("Mme"))
        assertTrue(tokens.contains("Laure"))
        assertTrue(tokens.contains("Miller"))
    }
}