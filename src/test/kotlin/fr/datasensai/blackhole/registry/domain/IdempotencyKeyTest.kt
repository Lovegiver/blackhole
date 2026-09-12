package fr.datasensai.blackhole.registry.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class IdempotencyKeyTest {

    @Test
    fun `canonical UUID is accepted as an opaque key`() {
        val raw = "550e8400-e29b-41d4-a716-446655440000"
        assertEquals(raw, IdempotencyKey.parse(raw).value.toString())
    }

    @Test
    fun `missing malformed or noncanonical UUID is rejected`() {
        assertThrows(InvalidIdempotencyKeyException::class.java) { IdempotencyKey.parse(null) }
        assertThrows(InvalidIdempotencyKeyException::class.java) { IdempotencyKey.parse("not-a-uuid") }
        assertThrows(InvalidIdempotencyKeyException::class.java) { IdempotencyKey.parse("1-1-1-1-1") }
    }
}
