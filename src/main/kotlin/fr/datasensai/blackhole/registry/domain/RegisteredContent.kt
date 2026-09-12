package fr.datasensai.blackhole.registry.domain

import java.time.OffsetDateTime
import java.util.UUID

data class RegisteredContent(
    val contentId: UUID,
    val contentHash: ContentHash,
    val idempotencyKey: IdempotencyKey,
    val registeredAt: OffsetDateTime
)

enum class RegistrationResult {
    NEW,
    KNOWN
}

data class ContentRegistration(
    val content: RegisteredContent,
    val outcome: RegistrationOutcome
) {
    val result: RegistrationResult
        get() = if (outcome == RegistrationOutcome.KNOWN) RegistrationResult.KNOWN else RegistrationResult.NEW
}

enum class RegistrationOutcome {
    CREATED,
    IDEMPOTENT_REPLAY,
    KNOWN
}
