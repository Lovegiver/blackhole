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
    fun `first registration is NEW and replay is stable KNOWN`() {
        val contentHash = hash('1')

        val first = put(contentHash)
        val replay = put(contentHash)

        assertEquals(201, first.status)
        assertEquals("NEW", first.body["result"].asText())
        assertEquals(200, replay.status)
        assertEquals("KNOWN", replay.body["result"].asText())
        assertEquals(first.body["content_id"].asText(), replay.body["content_id"].asText())
        assertEquals(first.body["registered_at"].asText(), replay.body["registered_at"].asText())
        assertEquals(contentHash, replay.body["content_hash"].asText())
        assertEquals(1, countRows(contentHash))
    }

    @Test
    fun `concurrent identical registrations produce exactly one NEW`() {
        val contentHash = hash('2')
        val callCount = 16
        val ready = CountDownLatch(callCount)
        val start = CountDownLatch(1)
        val executor = Executors.newFixedThreadPool(callCount)

        try {
            val futures = (1..callCount).map {
                executor.submit<RegistryHttpResponse> {
                    ready.countDown()
                    start.await()
                    put(contentHash)
                }
            }
            ready.await()
            start.countDown()
            val responses = futures.map { it.get() }

            assertEquals(1, responses.count { it.status == 201 })
            assertEquals(callCount - 1, responses.count { it.status == 200 })
            assertEquals(setOf("NEW", "KNOWN"), responses.map { it.body["result"].asText() }.toSet())
            assertEquals(1, responses.map { it.body["content_id"].asText() }.toSet().size)
            assertEquals(1, responses.map { it.body["registered_at"].asText() }.toSet().size)
            assertEquals(1, countRows(contentHash))
        } finally {
            executor.shutdownNow()
        }
    }

    @Test
    fun `concurrent distinct registrations remain distinct`() {
        val hashes = ('3'..'9').map(::hash)
        val ready = CountDownLatch(hashes.size)
        val start = CountDownLatch(1)
        val executor = Executors.newFixedThreadPool(hashes.size)

        try {
            val futures = hashes.map { contentHash ->
                executor.submit<RegistryHttpResponse> {
                    ready.countDown()
                    start.await()
                    put(contentHash)
                }
            }
            ready.await()
            start.countDown()
            val responses = futures.map { it.get() }

            assertTrue(responses.all { it.status == 201 })
            assertTrue(responses.all { it.body["result"].asText() == "NEW" })
            assertEquals(hashes.size, responses.map { it.body["content_id"].asText() }.toSet().size)
            hashes.forEach { assertEquals(1, countRows(it)) }
        } finally {
            executor.shutdownNow()
        }
    }

    @Test
    fun `invalid hash is rejected without insertion`() {
        val invalidHash = "A".repeat(64)
        val before = countAllRows()

        val response = put(invalidHash)

        assertEquals(400, response.status)
        assertEquals("INVALID_CONTENT_HASH", response.body["code"].asText())
        assertEquals(before, countAllRows())
    }

    @Test
    fun `PostgreSQL uniqueness and runtime least privilege are effective`() {
        val contentHash = hash('a')
        assertEquals(201, put(contentHash).status)

        dataSource.connection.use { connection ->
            val duplicate = assertThrows(SQLException::class.java) {
                connection.prepareStatement(
                    "INSERT INTO blackhole.content_registry (content_hash) VALUES (?)"
                ).use { statement ->
                    statement.setString(1, contentHash)
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
        val resultEnum = specification["components"]["schemas"]["ContentRegistryResponse"]
            .get("properties").get("result").get("enum")
        val results = resultEnum.map(JsonNode::asText)
        assertEquals(listOf("NEW", "KNOWN"), results)
    }

    private fun put(contentHash: String): RegistryHttpResponse {
        val request = HttpRequest.newBuilder(
            baseUri.resolve("v1/content-registry/$contentHash")
        ).PUT(HttpRequest.BodyPublishers.noBody()).build()
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

    private data class RegistryHttpResponse(
        val status: Int,
        val body: JsonNode
    )
}
