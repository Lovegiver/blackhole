package fr.datasensai.blackhole.registry.infrastructure

import fr.datasensai.blackhole.registry.domain.ContentHash
import fr.datasensai.blackhole.registry.domain.ContentRegistration
import fr.datasensai.blackhole.registry.domain.IdempotencyKey
import fr.datasensai.blackhole.registry.domain.RegisteredContent
import fr.datasensai.blackhole.registry.domain.RegistrationOutcome
import jakarta.enterprise.context.ApplicationScoped
import java.sql.ResultSet
import java.sql.SQLException
import javax.sql.DataSource

/** PostgreSQL adapter for the append-only content hash registry. */
@ApplicationScoped
class ContentRegistryRepository(
    private val dataSource: DataSource
) {

    fun registerOrFind(contentHash: ContentHash, idempotencyKey: IdempotencyKey): ContentRegistration = try {
        dataSource.connection.use connectionUse@{ connection ->
            val inserted = connection.prepareStatement(INSERT_CONTENT).use { statement ->
                statement.queryTimeout = QUERY_TIMEOUT_SECONDS
                statement.setString(1, contentHash.value)
                statement.setObject(2, idempotencyKey.value)
                statement.executeQuery().use { rows ->
                    if (rows.next()) rows.toRegisteredContent() else null
                }
            }
            if (inserted != null) {
                return@connectionUse ContentRegistration(
                    content = inserted,
                    outcome = RegistrationOutcome.CREATED
                )
            }

            val operation = connection.prepareStatement(FIND_BY_IDEMPOTENCY_KEY).use { statement ->
                statement.queryTimeout = QUERY_TIMEOUT_SECONDS
                statement.setObject(1, idempotencyKey.value)
                statement.executeQuery().use { rows ->
                    if (rows.next()) rows.toRegisteredContent() else null
                }
            }
            if (operation != null) {
                if (operation.contentHash != contentHash) throw IdempotencyKeyConflictException()
                return@connectionUse ContentRegistration(operation, RegistrationOutcome.IDEMPOTENT_REPLAY)
            }

            connection.prepareStatement(FIND_BY_CONTENT_HASH).use { statement ->
                statement.queryTimeout = QUERY_TIMEOUT_SECONDS
                statement.setString(1, contentHash.value)
                statement.executeQuery().use { rows ->
                    check(rows.next()) {
                        "A conflicting content hash must be visible after INSERT conflict resolution"
                    }
                    ContentRegistration(rows.toRegisteredContent(), RegistrationOutcome.KNOWN)
                }
            }
        }
    } catch (exception: SQLException) {
        throw ContentRegistryUnavailableException(exception)
    }

    private fun ResultSet.toRegisteredContent() = RegisteredContent(
        contentId = getObject("id", java.util.UUID::class.java),
        contentHash = ContentHash.parse(getString("content_hash").trimEnd()),
        idempotencyKey = IdempotencyKey.parse(getObject("idempotency_key", java.util.UUID::class.java).toString()),
        registeredAt = getObject("registered_at", java.time.OffsetDateTime::class.java)
    )

    private companion object {
        const val QUERY_TIMEOUT_SECONDS = 5

        const val INSERT_CONTENT = """
            INSERT INTO blackhole.content_registry (content_hash, idempotency_key)
            VALUES (?, ?)
            ON CONFLICT DO NOTHING
            RETURNING id, content_hash, idempotency_key, registered_at
        """

        const val FIND_BY_IDEMPOTENCY_KEY = """
            SELECT id, content_hash, idempotency_key, registered_at
            FROM blackhole.content_registry
            WHERE idempotency_key = ?
        """

        const val FIND_BY_CONTENT_HASH = """
            SELECT id, content_hash, idempotency_key, registered_at
            FROM blackhole.content_registry
            WHERE content_hash = ?
        """
    }
}

class ContentRegistryUnavailableException(cause: SQLException) :
    RuntimeException("Content registry database operation failed", cause)

class IdempotencyKeyConflictException : RuntimeException(
    "Idempotency-Key is already associated with another content hash"
)
