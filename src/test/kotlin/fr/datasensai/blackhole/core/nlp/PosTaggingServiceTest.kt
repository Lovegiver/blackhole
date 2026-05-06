package fr.datasensai.blackhole.core.nlp

import fr.datasensai.blackhole.core.config.OpenNlpConfig
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class PosTaggingServiceTest {

    private val openNlpConfig = TestConfigs.openNlpConfig()

    private val tokenizerService = TokenizerService(
        openNlpTokenizerService = OpenNlpTokenizerService(openNlpConfig)
    )

    private val posTaggingService = PosTaggingService(
        tokenizerService = tokenizerService,
        openNlpConfig = openNlpConfig
    )

    @Test
    fun `should tag french text`() {
        val tagged = posTaggingService.tagText(
            "Proposition de loi de Mme Laure Miller"
        )

        assertEquals(7, tagged.size)
        assertTrue(tagged.any { it.token == "Proposition" })
        assertTrue(tagged.any { it.token == "loi" })
        assertTrue(tagged.any { it.token == "Laure" })
        assertTrue(tagged.any { it.token == "Miller" })
    }

    @Test
    fun `should fail when pos model not found`() {
        val badConfig = object : OpenNlpConfig {
            override fun tokenizerModelPath() = "models/opennlp-fr-ud-gsd-tokens-1.3-2.5.4.bin"
            override fun posModelPath() = "invalid.bin"
        }

        val tokenizer = TokenizerService(
            OpenNlpTokenizerService(TestConfigs.openNlpConfig())
        )

        val service = PosTaggingService(tokenizer, badConfig)

        assertThrows<IllegalStateException> {
            service.tagText("test")
        }
    }

}