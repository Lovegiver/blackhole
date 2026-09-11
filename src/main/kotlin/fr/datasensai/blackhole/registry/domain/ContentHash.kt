package fr.datasensai.blackhole.registry.domain

/**
 * SHA-256 identifier of canonical article content.
 *
 * Black Hole deliberately knows only this stable fingerprint; it does not
 * carry an acquisition occurrence or an analysis state.
 */
@JvmInline
value class ContentHash private constructor(val value: String) {

    companion object {
        private val SHA_256_HEX = Regex("^[0-9a-f]{64}$")

        fun parse(rawValue: String): ContentHash {
            if (!SHA_256_HEX.matches(rawValue)) {
                throw InvalidContentHashException()
            }
            return ContentHash(rawValue)
        }
    }
}

class InvalidContentHashException : IllegalArgumentException(
    "content_hash must be exactly 64 lowercase hexadecimal characters"
)
