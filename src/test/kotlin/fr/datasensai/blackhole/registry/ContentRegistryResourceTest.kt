package fr.datasensai.blackhole.registry

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import io.quarkus.test.common.http.TestHTTPResource
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.common.QuarkusTestResource
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.sql.SQLException
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import javax.sql.DataSource

@QuarkusTest
@QuarkusTestResource(
    value = PostgresContentRegistryTestResource::class,
    restrictToAnnotatedClass = true
)
class ContentRegistryResourceTest {

    @TestHTTPResource("/")
    lateinit var baseUri: URI

    @Inject
    lateinit var objectMapper: ObjectMapper

    @Inject
    lateinit var dataSource: DataSource

    private val httpClient = HttpClient.newHttpClient()

    @Test
    fun `first registration and same operation replay are stable NEW`() {
        val contentHash = hash('1')
        val idempotencyKey = key(1)

        val first = put(contentHash, idempotencyKey)
        val replay = put(contentHash, idempotencyKey)

        assertEquals(201, first.status)
        assertEquals("NEW", first.body["result"].asText())
        assertEquals(200, replay.status)
        assertEquals("NEW", replay.body["result"].asText())
        assertEquals(first.body["content_id"].asText(), replay.body["content_id"].asText())
        assertEquals(first.body["registered_at"].asText(), replay.body["registered_at"].asText())
        assertEquals(contentHash, replay.body["content_hash"].asText())
        assertEquals(1, countRows(contentHash))
    }

    @Test
    fun `another operation for the same hash is KNOWN`() {
        val contentHash = hash('b')
        val first = put(contentHash, key(2))
        val otherOperation = put(contentHash, key(3))

        assertEquals(201, first.status)
        assertEquals("NEW", first.body["result"].asText())
        assertEquals(200, otherOperation.status)
        assertEquals("KNOWN", otherOperation.body["result"].asText())
        assertEquals(first.body["content_id"].asText(), otherOperation.body["content_id"].asText())
        assertEquals(first.body["registered_at"].asText(), otherOperation.body["registered_at"].asText())
    }

    @Test
    fun `same key for another hash is conflict`() {
        val idempotencyKey = key(4)
        assertEquals(201, put(hash('c'), idempotencyKey).status)

        val conflict = put(hash('d'), idempotencyKey)

        assertEquals(409, conflict.status)
        assertEquals("IDEMPOTENCY_KEY_CONFLICT", conflict.body["code"].asText())
        assertEquals(0, countRows(hash('d')))
    }

    @Test
    fun `concurrent identical operation produces one creation and only NEW replays`() {
        val contentHash = hash('2')
        val idempotencyKey = key(5)
        val callCount = 16
        val ready = CountDownLatch(callCount)
        val start = CountDownLatch(1)
        val executor = Executors.newFixedThreadPool(callCount)

        try {
            val futures = (1..callCount).map {
                executor.submit<RegistryHttpResponse> {
                    ready.countDown()
                    start.await()
                    put(contentHash, idempotencyKey)
                }
            }
            ready.await()
            start.countDown()
            val responses = futures.map { it.get() }

            assertEquals(1, responses.count { it.status == 201 })
            assertEquals(callCount - 1, responses.count { it.status == 200 })
            assertEquals(setOf("NEW"), responses.map { it.body["result"].asText() }.toSet())
            assertEquals(1, responses.map { it.body["content_id"].asText() }.toSet().size)
            assertEquals(1, responses.map { it.body["registered_at"].asText() }.toSet().size)
            assertEquals(1, countRows(contentHash))
        } finally {
            executor.shutdownNow()
        }
    }

    @Test
    fun `concurrent distinct operations for the same hash produce one NEW`() {
        val contentHash = hash('3')
        val callCount = 12
        val ready = CountDownLatch(callCount)
        val start = CountDownLatch(1)
        val executor = Executors.newFixedThreadPool(callCount)

        try {
            val futures = (1..callCount).map { index ->
                executor.submit<RegistryHttpResponse> {
                    ready.countDown()
                    start.await()
                    put(contentHash, key(100 + index))
                }
            }
            ready.await()
            start.countDown()
            val responses = futures.map { it.get() }

            assertEquals(1, responses.count { it.status == 201 && it.body["result"].asText() == "NEW" })
            assertEquals(callCount - 1, responses.count { it.status == 200 && it.body["result"].asText() == "KNOWN" })
            assertEquals(1, responses.map { it.body["content_id"].asText() }.toSet().size)
            assertEquals(1, countRows(contentHash))
        } finally {
            executor.shutdownNow()
        }
    }

    @Test
    fun `concurrent different hashes for the same key produce one NEW and conflicts`() {
        val hashes = ('4'..'9').map(::hash)
        val idempotencyKey = key(200)
        val ready = CountDownLatch(hashes.size)
        val start = CountDownLatch(1)
        val executor = Executors.newFixedThreadPool(hashes.size)

        try {
            val futures = hashes.map { contentHash ->
                executor.submit<RegistryHttpResponse> {
                    ready.countDown()
                    start.await()
                    put(contentHash, idempotencyKey)
                }
            }
            ready.await()
            start.countDown()
            val responses = futures.map { it.get() }

            assertEquals(1, responses.count { it.status == 201 && it.body["result"].asText() == "NEW" })
            assertEquals(hashes.size - 1, responses.count { it.status == 409 })
            assertEquals(1, hashes.sumOf(::countRows))
        } finally {
            executor.shutdownNow()
        }
    }

    @Test
    fun `invalid hash is rejected without insertion`() {
        val invalidHash = "A".repeat(64)
        val before = countAllRows()

        val response = put(invalidHash, key(300))

        assertEquals(400, response.status)
        assertEquals("INVALID_CONTENT_HASH", response.body["code"].asText())
        assertEquals(before, countAllRows())
    }

    @Test
    fun `missing or invalid idempotency key is rejected without insertion`() {
        val missingHash = hash('e')
        val invalidHash = hash('f')
        val before = countAllRows()

        val missing = put(missingHash, null)
        val invalid = put(invalidHash, "not-a-uuid")

        assertEquals(400, missing.status)
        assertEquals("INVALID_IDEMPOTENCY_KEY", missing.body["code"].asText())
        assertEquals(400, invalid.status)
        assertEquals("INVALID_IDEMPOTENCY_KEY", invalid.body["code"].asText())
        assertEquals(before, countAllRows())
    }

    @Test
    fun `PostgreSQL uniqueness and runtime least privilege are effective`() {
        val contentHash = hash('a')
        assertEquals(201, put(contentHash, key(400)).status)

        dataSource.connection.use { connection ->
            val duplicate = assertThrows(SQLException::class.java) {
                connection.prepareStatement(
                    "INSERT INTO blackhole.content_registry (content_hash, idempotency_key) VALUES (?, ?)"
                ).use { statement ->
                    statement.setString(1, contentHash)
                    statement.setObject(2, UUID.fromString(key(401)))
                    statement.executeUpdate()
                }
            }
            assertEquals("23505", duplicate.sqlState)
        }

        assertPrivilegeDenied("UPDATE blackhole.content_registry SET content_hash = content_hash")
        assertPrivilegeDenied("DELETE FROM blackhole.content_registry")
        assertPrivilegeDenied("CREATE TABLE blackhole.forbidden_table (id integer)")
        assertEquals(1, countRows(contentHash))
    }

    @Test
    fun `OpenAPI preserves the bounded contract`() {
        val request = HttpRequest.newBuilder(baseUri.resolve("q/openapi?format=json")).GET().build()
        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())

        assertEquals(200, response.statusCode())
        val specification = objectMapper.readTree(response.body())
        val operation = specification["paths"]["/v1/content-registry/{content_hash}"]["put"]
        assertNotEquals(null, operation)
        assertNotEquals(null, operation["responses"]["200"])
        assertNotEquals(null, operation["responses"]["201"])
        assertNotEquals(null, operation["responses"]["409"])
        val idempotencyHeader = operation["parameters"].first { it["name"].asText() == "Idempotency-Key" }
        assertTrue(idempotencyHeader["required"].asBoolean())
        val resultEnum = specification["components"]["schemas"]["ContentRegistryResponse"]
            .get("properties").get("result").get("enum")
        val results = resultEnum.map(JsonNode::asText)
        assertEquals(listOf("NEW", "KNOWN"), results)
    }

    private fun put(contentHash: String, idempotencyKey: String?): RegistryHttpResponse {
        val builder = HttpRequest.newBuilder(
            baseUri.resolve("v1/content-registry/$contentHash")
        )
        if (idempotencyKey != null) builder.header("Idempotency-Key", idempotencyKey)
        val request = builder.PUT(HttpRequest.BodyPublishers.noBody()).build()
        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        return RegistryHttpResponse(
            status = response.statusCode(),
            body = objectMapper.readTree(response.body())
        )
    }

    private fun countRows(contentHash: String): Int = dataSource.connection.use { connection ->
        connection.prepareStatement(
            "SELECT count(*) FROM blackhole.content_registry WHERE content_hash = ?"
        ).use { statement ->
            statement.setString(1, contentHash)
            statement.executeQuery().use { rows ->
                rows.next()
                rows.getInt(1)
            }
        }
    }

    private fun countAllRows(): Int = dataSource.connection.use { connection ->
        connection.createStatement().use { statement ->
            statement.executeQuery("SELECT count(*) FROM blackhole.content_registry").use { rows ->
                rows.next()
                rows.getInt(1)
            }
        }
    }

    private fun assertPrivilegeDenied(sql: String) {
        dataSource.connection.use { connection ->
            val exception = assertThrows(SQLException::class.java) {
                connection.createStatement().use { statement -> statement.execute(sql) }
            }
            assertEquals("42501", exception.sqlState)
        }
    }

    private fun hash(character: Char): String = character.toString().repeat(64)

    private fun key(value: Int): String = "00000000-0000-4000-8000-${value.toString().padStart(12, '0')}"

    private data class RegistryHttpResponse(
        val status: Int,
        val body: JsonNode
    )
}
