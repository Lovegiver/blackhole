package fr.datasensai.blackhole.core.nlp

import fr.datasensai.blackhole.core.config.OpenNlpConfig
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class OpenNlpTokenizerServiceTest {

    private val service = OpenNlpTokenizerService(
        openNlpConfig = TestConfigs.openNlpConfig()
    )

    @Test
    fun `should tokenize french institutional title`() {
        val text = "N° 2680 - Proposition de loi de Mme Laure Miller visant à protéger durablement les jeunes majeurs"

        val tokens = service.tokenize(text)

        assertEquals(
            listOf(
                "N°", "2680", "-", "Proposition", "de", "loi", "de",
                "Mme", "Laure", "Miller", "visant", "à", "protéger",
                "durablement", "les", "jeunes", "majeurs"
            ),
            tokens
        )
    }

    @Test
    fun `should fail when model not found`() {
        val badConfig = object : OpenNlpConfig {
            override fun tokenizerModelPath() = "invalid/path.bin"
            override fun posModelPath() = ""
            override fun sentenceModelPath() = ""
        }

        val service = OpenNlpTokenizerService(badConfig)

        assertThrows<IllegalStateException> {
            service.tokenize("test")
        }
    }
}