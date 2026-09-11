package fr.datasensai.blackhole.registry.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class ContentHashTest {

    @Test
    fun `accepts exactly one lowercase SHA-256`() {
        val rawHash = "a".repeat(64)

        assertEquals(rawHash, ContentHash.parse(rawHash).value)
    }

    @Test
    fun `rejects malformed hashes`() {
        listOf(
            "a".repeat(63),
            "a".repeat(65),
            "A".repeat(64),
            "g".repeat(64)
        ).forEach { rawHash ->
            assertThrows(InvalidContentHashException::class.java) {
                ContentHash.parse(rawHash)
            }
        }
    }
}
