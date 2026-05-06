package fr.datasensai.blackhole.core.nlp

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TextNormalizerServiceTest {

    private val service = TextNormalizerService()

    @Test
    fun `should normalize concept label`() {
        val result = service.normalizeConceptLabel("  Proposition   de   Loi  ")

        assertEquals("proposition de loi", result)
    }

    @Test
    fun `should normalize apostrophe`() {
        val result = service.normalizeConceptLabel("l’intérêt général")

        assertEquals("l'intérêt général", result)
    }

    @Test
    fun `should normalize key by removing accents`() {
        val result = service.normalizeKey("Élévation")

        assertEquals("elevation", result)
    }

    @Test
    fun `should normalize key with spaces and accents`() {
        val result = service.normalizeKey("  Jeunes   Majeurs Étrangers  ")

        assertEquals("jeunes majeurs etrangers", result)
    }
}