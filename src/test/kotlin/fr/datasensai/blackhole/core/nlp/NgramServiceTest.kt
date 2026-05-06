package fr.datasensai.blackhole.core.nlp

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class NgramServiceTest {

    private val linguisticResourceService = LinguisticResourceService(
        nlpConfig = TestConfigs.nlpConfig()
    )

    private val tokenFilterService = TokenFilterService(
        linguisticResourceService = linguisticResourceService
    )

    private val tokenQualificationService = TokenQualificationService(
        tokenFilterService = tokenFilterService
    )

    private val ngramService = NgramService(
        tokenQualificationService = tokenQualificationService
    )

    @Test
    fun `should create ngrams from meaningful tokens`() {
        val tokens = listOf(
            PosToken("suivi", "NOUN"),
            PosToken("personnalisé", "ADJ"),
            PosToken("jeunes", "ADJ"),
            PosToken("majeurs", "NOUN")
        )

        val result = ngramService.generate(tokens, 2, 3)

        assertTrue(result.contains(NgramCandidate("suivi personnalisé")))
        assertTrue(result.contains(NgramCandidate("jeunes majeurs")))
        assertTrue(result.contains(NgramCandidate("suivi personnalisé jeunes")))
        assertTrue(result.contains(NgramCandidate("personnalisé jeunes majeurs")))
    }

    @Test
    fun `should reject ngrams made only of stopwords`() {
        val tokens = listOf(
            PosToken("de", "ADP"),
            PosToken("la", "DET"),
            PosToken("loi", "NOUN")
        )

        val result = ngramService.generate(tokens, 2, 2)

        assertFalse(result.contains(NgramCandidate("de la")))
        assertFalse(result.contains(NgramCandidate("la loi")))
    }

    @Test
    fun `should reject ngrams starting with stopword`() {
        val tokens = listOf(
            PosToken("de", "ADP"),
            PosToken("loi", "NOUN"),
            PosToken("constitutionnelle", "ADJ")
        )

        val result = ngramService.generate(tokens, 2, 2)

        assertFalse(result.contains(NgramCandidate("de loi")))
        assertTrue(result.contains(NgramCandidate("loi constitutionnelle")))
    }

    @Test
    fun `should reject ngrams ending with stopword`() {
        val tokens = listOf(
            PosToken("proposition", "NOUN"),
            PosToken("de", "ADP"),
            PosToken("loi", "NOUN")
        )

        val result = ngramService.generate(tokens, 2, 3)

        assertFalse(result.contains(NgramCandidate("proposition de")))
        assertTrue(result.contains(NgramCandidate("proposition de loi")))
    }

    @Test
    fun `should keep meaningful ngrams with internal stopwords`() {
        val tokens = listOf(
            PosToken("proposition", "NOUN"),
            PosToken("de", "ADP"),
            PosToken("loi", "NOUN"),
            PosToken("services", "NOUN"),
            PosToken("de", "ADP"),
            PosToken("l’État", "NOUN")
        )

        val result = ngramService.generate(tokens, 3, 3)

        assertTrue(result.contains(NgramCandidate("proposition de loi")))
        assertTrue(result.contains(NgramCandidate("services de l’État")))
        assertFalse(result.contains(NgramCandidate("loi services de")))
    }

    @Test
    fun `should reject ngrams containing punctuation only token`() {
        val tokens = listOf(
            PosToken("enfance", "NOUN"),
            PosToken(".", "PUNCT"),
            PosToken("Elle", "PRON")
        )

        val result = ngramService.generate(tokens, 2, 2)

        assertFalse(result.contains(NgramCandidate("enfance .")))
        assertFalse(result.contains(NgramCandidate(". Elle")))
    }

    @Test
    fun `should trim and ignore blank tokens`() {
        val tokens = listOf(
            PosToken(" ", "SPACE"),
            PosToken("proposition", "NOUN"),
            PosToken("de", "ADP"),
            PosToken("loi", "NOUN"),
            PosToken("", "SPACE")
        )

        val result = ngramService.generate(tokens, 2, 3)

        assertFalse(result.contains(NgramCandidate("proposition de")))
        assertFalse(result.contains(NgramCandidate("de loi")))
        assertTrue(result.contains(NgramCandidate("proposition de loi")))
    }

    @Test
    fun `should return empty list when tokens are empty`() {
        val result = ngramService.generate(
            tokens = emptyList(),
            minNgramSize = 2,
            maxNgramSize = 3
        )

        assertEquals(emptyList<NgramCandidate>(), result)
    }

    @Test
    fun `should return empty list when ngram size is greater than token count`() {
        val result = ngramService.generate(
            tokens = listOf(PosToken("loi", "NOUN")),
            minNgramSize = 2,
            maxNgramSize = 3
        )

        assertEquals(emptyList<NgramCandidate>(), result)
    }

    @Test
    fun `should reject invalid ngram sizes`() {
        val tokens = listOf(
            PosToken("proposition", "NOUN"),
            PosToken("de", "ADP"),
            PosToken("loi", "NOUN")
        )

        assertThrows<IllegalArgumentException> {
            ngramService.generate(tokens, 0, 3)
        }

        assertThrows<IllegalArgumentException> {
            ngramService.generate(tokens, 3, 2)
        }
    }
}