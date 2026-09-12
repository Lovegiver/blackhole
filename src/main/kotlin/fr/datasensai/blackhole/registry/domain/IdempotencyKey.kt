package fr.datasensai.blackhole.registry.domain

import java.util.UUID

/** Opaque, canonical UUID identifying one caller operation independently of Acquisition. */
@JvmInline
value class IdempotencyKey private constructor(val value: UUID) {

    companion object {
        fun parse(rawValue: String?): IdempotencyKey {
            if (rawValue == null) throw InvalidIdempotencyKeyException()
            val parsed = try {
                UUID.fromString(rawValue)
            } catch (_: IllegalArgumentException) {
                throw InvalidIdempotencyKeyException()
            }
            if (!parsed.toString().equals(rawValue, ignoreCase = true)) {
                throw InvalidIdempotencyKeyException()
            }
            return IdempotencyKey(parsed)
        }
    }
}

class InvalidIdempotencyKeyException : IllegalArgumentException(
    "Idempotency-Key must be a canonical UUID"
)
