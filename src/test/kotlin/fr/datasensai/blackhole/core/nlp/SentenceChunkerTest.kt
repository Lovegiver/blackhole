package fr.datasensai.blackhole.core.nlp

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class SentenceChunkerTest {

    private val linguisticResourceService = LinguisticResourceService(
        nlpConfig = TestConfigs.nlpConfig()
    )

    private val tokenFilterService = TokenFilterService(
        linguisticResourceService = linguisticResourceService
    )

    private val tokenQualificationService = TokenQualificationService(
        tokenFilterService = tokenFilterService
    )

    private val chunker = SentenceChunker(
        tokenQualificationService = tokenQualificationService
    )

    @Test
    fun `should split sentence into chunks`() {

        val sentence = Sentence(
            rawText = "Le texte présenté par Laure Miller entend réduire les ruptures de parcours.",
            tokens = listOf(
                PosToken("Le", "DET"),
                PosToken("texte", "NOUN"),
                PosToken("présenté", "VERB"),
                PosToken("par", "ADP"),
                PosToken("Laure", "PROPN"),
                PosToken("Miller", "PROPN"),
                PosToken("entend", "VERB"),
                PosToken("réduire", "VERB"),
                PosToken("les", "DET"),
                PosToken("ruptures", "NOUN"),
                PosToken("de", "ADP"),
                PosToken("parcours", "NOUN"),
                PosToken(".", "PUNCT")
            )
        )

        val result = chunker.chunk(sentence)

        assertEquals(1, result.size)

        assertEquals(
            "Le texte présenté par Laure Miller entend réduire les ruptures de parcours",
            result[0].text()
        )
    }
}