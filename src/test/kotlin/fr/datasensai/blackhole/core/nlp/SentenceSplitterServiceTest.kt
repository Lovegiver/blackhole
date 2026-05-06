package fr.datasensai.blackhole.core.nlp

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class SentenceSplitterServiceTest {

    private val openNlpConfig = TestConfigs.openNlpConfig()

    private val tokenizerService = TokenizerService(
        openNlpTokenizerService = OpenNlpTokenizerService(openNlpConfig)
    )

    private val posTaggingService = PosTaggingService(
        tokenizerService = tokenizerService,
        openNlpConfig = openNlpConfig
    )

    private val sentenceDetector = OpenNlpSentenceDetectorService(
        openNlpConfig = openNlpConfig
    )

    private val service = SentenceSplitterService(
        sentenceDetector = sentenceDetector,
        posTaggingService = posTaggingService
    )

    @Test
    fun `should split text into sentences`() {

        val text = """
            La proposition de loi avance.
            Elle protège les jeunes majeurs.
        """.trimIndent()

        val result = service.split(text)

        assertEquals(2, result.size)

        assertEquals(
            "La proposition de loi avance.",
            result[0].text()
        )

        assertEquals(
            "Elle protège les jeunes majeurs.",
            result[1].text()
        )
    }
}