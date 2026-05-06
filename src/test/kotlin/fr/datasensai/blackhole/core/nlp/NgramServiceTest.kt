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

    private val service = NgramService(
        tokenFilterService = tokenFilterService
    )

    @Test
    fun `should create ngrams from meaningful tokens`() {
        val tokens = listOf("suivi", "personnalisé", "jeunes", "majeurs")

        val result = service.generate(
            tokens = tokens,
            minNgramSize = 2,
            maxNgramSize = 3
        )

        assertTrue(result.contains(NgramCandidate("suivi personnalisé")))
        assertTrue(result.contains(NgramCandidate("jeunes majeurs")))
        assertTrue(result.contains(NgramCandidate("suivi personnalisé jeunes")))
        assertTrue(result.contains(NgramCandidate("personnalisé jeunes majeurs")))
    }

    @Test
    fun `should reject ngrams made only of stopwords`() {
        val tokens = listOf("de", "la", "loi")

        val result = service.generate(
            tokens = tokens,
            minNgramSize = 2,
            maxNgramSize = 2
        )

        assertFalse(result.contains(NgramCandidate("de la")))
        assertFalse(result.contains(NgramCandidate("la loi")))
    }

    @Test
    fun `should reject ngrams starting with stopword`() {
        val tokens = listOf("de", "loi", "constitutionnelle")

        val result = service.generate(
            tokens = tokens,
            minNgramSize = 2,
            maxNgramSize = 2
        )

        assertFalse(result.contains(NgramCandidate("de loi")))
        assertTrue(result.contains(NgramCandidate("loi constitutionnelle")))
    }

    @Test
    fun `should reject ngrams ending with stopword`() {
        val tokens = listOf("proposition", "de", "loi")

        val result = service.generate(
            tokens = tokens,
            minNgramSize = 2,
            maxNgramSize = 3
        )

        assertFalse(result.contains(NgramCandidate("proposition de")))
        assertTrue(result.contains(NgramCandidate("proposition de loi")))
    }

    @Test
    fun `should keep meaningful ngrams with internal stopwords`() {
        val tokens = listOf("proposition", "de", "loi", "services", "de", "l’État")

        val result = service.generate(
            tokens = tokens,
            minNgramSize = 3,
            maxNgramSize = 3
        )

        assertTrue(result.contains(NgramCandidate("proposition de loi")))
        assertTrue(result.contains(NgramCandidate("services de l’État")))
        assertFalse(result.contains(NgramCandidate("loi services de")))
    }

    @Test
    fun `should reject ngrams containing punctuation only token`() {
        val tokens = listOf("enfance", ".", "Elle")

        val result = service.generate(
            tokens = tokens,
            minNgramSize = 2,
            maxNgramSize = 2
        )

        assertFalse(result.contains(NgramCandidate("enfance .")))
        assertFalse(result.contains(NgramCandidate(". Elle")))
    }

    @Test
    fun `should trim and ignore blank tokens`() {
        val tokens = listOf(" ", "proposition", "de", "loi", "")

        val result = service.generate(
            tokens = tokens,
            minNgramSize = 2,
            maxNgramSize = 3
        )

        assertFalse(result.contains(NgramCandidate("proposition de")))
        assertFalse(result.contains(NgramCandidate("de loi")))
        assertTrue(result.contains(NgramCandidate("proposition de loi")))
    }

    @Test
    fun `should return empty list when tokens are empty`() {
        val result = service.generate(
            tokens = emptyList(),
            minNgramSize = 2,
            maxNgramSize = 3
        )

        assertEquals(emptyList<NgramCandidate>(), result)
    }

    @Test
    fun `should return empty list when ngram size is greater than token count`() {
        val result = service.generate(
            tokens = listOf("loi"),
            minNgramSize = 2,
            maxNgramSize = 3
        )

        assertEquals(emptyList<NgramCandidate>(), result)
    }

    @Test
    fun `should reject invalid ngram sizes`() {
        val tokens = listOf("proposition", "de", "loi")

        assertThrows<IllegalArgumentException> {
            service.generate(
                tokens = tokens,
                minNgramSize = 0,
                maxNgramSize = 3
            )
        }

        assertThrows<IllegalArgumentException> {
            service.generate(
                tokens = tokens,
                minNgramSize = 3,
                maxNgramSize = 2
            )
        }
    }
}