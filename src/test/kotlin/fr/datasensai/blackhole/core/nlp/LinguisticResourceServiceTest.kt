package fr.datasensai.blackhole.core.nlp

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class LinguisticResourceServiceTest {

    private val service = LinguisticResourceService(
        nlpConfig = TestConfigs.nlpConfig()
    )

    @Test
    fun `should load word set from resources`() {
        val result = service.loadWordSet("weak-tokens-fr.txt")

        assertEquals(true, result.isNotEmpty())
    }

    @Test
    fun `should fail when resource does not exist`() {
        val exception = assertThrows<IllegalArgumentException> {
            service.loadWordSet("missing-file.txt")
        }

        assertEquals(true, exception.message!!.contains("missing-file.txt"))
    }
}