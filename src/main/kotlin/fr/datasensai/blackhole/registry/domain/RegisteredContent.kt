package fr.datasensai.blackhole.registry.domain

import java.time.OffsetDateTime
import java.util.UUID

data class RegisteredContent(
    val contentId: UUID,
    val contentHash: ContentHash,
    val registeredAt: OffsetDateTime
)

enum class RegistrationResult {
    NEW,
    KNOWN
}

data class ContentRegistration(
    val content: RegisteredContent,
    val result: RegistrationResult
)
